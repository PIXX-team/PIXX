// src/main/java/com/ssafy/fourcut/domain/image/service/FeedService.java
package com.ssafy.fourcut.domain.image.service;

import com.amazonaws.services.cloudfront.model.EntityNotFoundException;
import com.ssafy.fourcut.domain.image.dto.*;
import com.ssafy.fourcut.domain.image.entity.*;
import com.ssafy.fourcut.domain.image.entity.enums.ImageType;
import com.ssafy.fourcut.domain.image.repository.*;
import com.ssafy.fourcut.domain.user.repository.UserRepository;
import com.ssafy.fourcut.global.s3.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.filter;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final BrandRepository brandRepository;
    private final HashtagRepository hashtagRepository;
    private final UserRepository userRepository;
    private final CloudFrontService cloudFrontService;
    private final AlbumRepository albumRepository;
    private final StoreRepository storeRepository;
    private final S3Uploader s3Uploader;

    /** 0,1: 페이징 정렬된 단일 리스트 (unchanged) */
    public List<FeedItemResponse> getFeedsSorted(
            Integer userId, int type, int page, int size
    ) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Feed> feedPage = (type == 0)
                ? feedRepository.findByUserUserIdOrderByFeedDateDesc(userId, pr)
                : feedRepository.findByUserUserIdOrderByFeedDateAsc(userId, pr);

        return feedPage.stream()
                .map(this::toItem)
                .collect(Collectors.toList());
    }

    /** 2: 브랜드별 최신 2개 피드만 그룹화하여 반환 */
    public FeedByBrandResponse getFeedsGroupedByBrand(Integer userId) {
        // (1) 브랜드명 리스트
        List<String> brands = feedRepository.findDistinctBrandNamesByUser(userId);

        // (2) 브랜드별로 Top2 피드 조회
        List<BrandGroupResponse> brandList = brands.stream()
                .map(brandName -> {
                    List<Feed> feeds = feedRepository
                            .findTop2ByUserUserIdAndBrandBrandNameOrderByFeedDateDesc(userId, brandName);
                    List<FeedItemResponse> items = feeds.stream()
                            .map(this::toItem)
                            .collect(Collectors.toList());
                    return new BrandGroupResponse(brandName, items);
                })
                .collect(Collectors.toList());

        return new FeedByBrandResponse(brandList);
    }

    /** Feed → FeedItemResponse 변환 공통 */
    private FeedItemResponse toItem(Feed feed) {
        String thumbUrl = feed.getImages().stream()
                // 1) 썸네일 플래그가 true 인 이미지 우선
                .filter(Image::getIsThumbnail)
                .findFirst()
                .map(Image::getImageUrl)
                // 2) 없으면 기존 로직: IMAGE 타입 중 ID 최소값
                .orElseGet(() -> feed.getImages().stream()
                        .filter(img -> ImageType.IMAGE.equals(img.getImageType()))
                        .min(Comparator.comparing(Image::getImageId))
                        .map(Image::getImageUrl)
                        .orElse("")
                );

        // 3) URL → 서명된 CloudFront URL
        String signedThumb = thumbUrl.isEmpty()
                ? ""
                : cloudFrontService.generateSignedCloudFrontUrl(thumbUrl, "get");

        return new FeedItemResponse(
                feed.getFeedId(),
                signedThumb,
                feed.getFeedFavorite()
        );
    }

    /**
     * 단일 피드 상세 조회
     */
    public FeedDetailResponse getFeedDetail(Integer userId, Integer feedId) {
        Feed feed = feedRepository.findWithDetailsByFeedId(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다. id=" + feedId));

        List<FeedImageResponse> images = feed.getImages().stream()
                // IMAGE 타입이면 false, 나머지는 true → false가 앞에 오므로 IMAGE가 제일 앞
                .sorted(Comparator.comparing(img -> img.getImageType() != ImageType.IMAGE))
                .map(img -> {
                    String signed = cloudFrontService.generateSignedCloudFrontUrl(img.getImageUrl(), "get");
                    return new FeedImageResponse(
                            img.getImageId(),
                            signed,
                            img.getImageType().name()
                    );
                })
                .collect(Collectors.toList());

        List<String> hashtags = feed.getHashFeeds().stream()
                .map(hf -> hf.getHashtag().getHashtagContent())
                .collect(Collectors.toList());

        return new FeedDetailResponse(
                images,
                feed.getFeedTitle(),
                hashtags,
                feed.getFeedMemo(),
                feed.getBrand().getBrandName(),
                feed.getFeedLocation(),
                feed.getFeedDate(),
                feed.getFeedFavorite()
        );
    }

    /**
     * feedId 경로 + body 를 이용해 피드 정보를 업데이트합니다.
     */
    @Transactional
    public void updateFeed(Integer userId, Integer feedId, UpdateFeedRequest req) {
        // 1) 유효한 사용자 확인 (선택)
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        // 2) 피드 조회 & 소유권 검증
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다. id=" + feedId));
        if (!feed.getUser().getUserId().equals(userId)) {
            throw new EntityNotFoundException("해당 사용자의 피드가 아닙니다. id=" + feedId);
        }

        // 3) 기본 필드 업데이트
        feed.setFeedTitle(req.getFeedTitle());
        feed.setFeedDate(req.getFeedDate());
        feed.setFeedLocation(req.getLocation());
        feed.setFeedMemo(req.getFeedMemo());

        // 4) 브랜드 변경
        Brand brand = brandRepository.findByBrandName(req.getBrandName())
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 브랜드입니다. name=" + req.getBrandName()));
        feed.setBrand(brand);

        // 5) 해시태그 업데이트 (cascade, orphanRemoval = true)
        feed.getHashFeeds().clear();
        for (String tagStr : req.getHashtags()) {
            // content 컬럼으로 조회
            Hashtag ht = hashtagRepository.findByHashtagContent(tagStr)
                    // 없으면 content 필드에 tagStr 세팅해서 저장
                    .orElseGet(() -> hashtagRepository.save(
                            Hashtag.builder()
                                    .hashtagContent(tagStr)
                                    .build()
                    ));
            HashFeed hf = HashFeed.builder()
                    .feed(feed)
                    .hashtag(ht)
                    .build();
            feed.getHashFeeds().add(hf);
        }
    }

    /**
     * feed.favorite 토글 + album 재배치
     */
    @Transactional
    public ToggleFavoriteResponse toggleFavorite(Integer userId, Integer feedId) {
        // 1) 사용자 검증
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        // 2) 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("피드를 찾을 수 없습니다. id=" + feedId));

        // 3) 좋아요 상태 반전
        Boolean current = feed.getFeedFavorite();
        Boolean updated = (current == null) ? Boolean.TRUE : !current;
        feed.setFeedFavorite(updated);

        // 4) 새 상태에 따라 앨범 이동
//        Album targetAlbum = updated
//                // 좋아요(true) → favorite_album=true 인 앨범으로
//                ? albumRepository.findByUserUserIdAndFavoriteAlbumTrue(userId)
//                .orElseThrow(() -> new EntityNotFoundException("favorite_album=true 인 앨범이 없습니다."))
//                // 좋아요(false) → default_album=true 인 앨범으로
//                : albumRepository.findByUserUserIdAndDefaultAlbumTrue(userId)
//                .orElseThrow(() -> new EntityNotFoundException("default_album=true 인 앨범이 없습니다."));
//        feed.setAlbum(targetAlbum);

        // 5) 변경 내용은 트랜잭션 커밋 시 자동 반영
        return new ToggleFavoriteResponse(feedId, updated);
    }

    // src/main/java/com/ssafy/fourcut/domain/image/service/FeedService.java
    @Transactional
    public void deleteFeeds(Integer userId, List<Integer> feedIds) {
        //피드들 조회
        List<Feed> feeds = feedRepository.findAllById(feedIds);

        // S3에 있는 파일 삭제
        for(Feed feed : feeds) {
            List<Image> images = storeRepository.findByFeedFeedId(feed.getFeedId());
            for(Image image : images) {
                s3Uploader.delete(image.getImageUrl());
            }
        }

        //삭제
        feedRepository.deleteAll(feeds);
    }
}

