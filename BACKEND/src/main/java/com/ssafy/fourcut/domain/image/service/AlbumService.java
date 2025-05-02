// src/main/java/com/ssafy/fourcut/domain/image/service/AlbumService.java
package com.ssafy.fourcut.domain.image.service;

import com.ssafy.fourcut.domain.image.dto.*;
import com.ssafy.fourcut.domain.image.entity.*;
import com.ssafy.fourcut.domain.image.repository.AlbumRepository;
import com.ssafy.fourcut.domain.image.repository.FeedRepository;
import com.ssafy.fourcut.domain.user.entity.User;
import com.ssafy.fourcut.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final FeedRepository feedRepository;

    public FeedAlbumResponse getFeedAlbum(Integer userId) {
        List<Album> albums = albumRepository.findByUserUserId(userId);

        List<AlbumResponse> dtoList = albums.stream()
                .map(album -> {
                    // feeds 컬렉션에서 가장 오래된 feedDate 뽑기
                    LocalDateTime oldestDate = album.getFeeds().stream()
                            .map(Feed::getFeedDate)
                            .min(Comparator.naturalOrder())
                            .orElse(null);

                    return new AlbumResponse(
                            album.getAlbumId(),
                            album.getAlbumName(),
                            oldestDate
                    );
                })
                .collect(Collectors.toList());

        return new FeedAlbumResponse(dtoList);
    }

    public AlbumDetailResponse getAlbumDetail(
            Integer userId,
            Integer albumId,
            int type,
            int page,
            int size
    ) {
        // 1) 앨범 존재 및 소유자 확인
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범이 없습니다. id=" + albumId));
        if (!album.getUser().getUserId().equals(userId)) {
            throw new EntityNotFoundException("해당 사용자의 앨범이 아닙니다.");
        }

        // 2) albumDate 계산 (가장 오래된 피드 한 건)
        Feed oldestFeed = feedRepository
                .findTop1ByAlbumAlbumIdOrderByFeedDateAsc(albumId);
        LocalDateTime albumDate = (oldestFeed != null)
                ? oldestFeed.getFeedDate()
                : null;

        // 3) 페이지 요청 생성
        PageRequest pr = PageRequest.of(page, size);

        // 4) 피드 페이징 조회 (정렬: type==0 최신, type==1 오래된)
        Page<Feed> feedPage = (type == 0)
                ? feedRepository.findByAlbumAlbumIdOrderByFeedDateDesc(albumId, pr)
                : feedRepository.findByAlbumAlbumIdOrderByFeedDateAsc(albumId, pr);

        // 5) Feed → FeedItemResponse 매핑
        List<FeedItemResponse> feedItems = feedPage.stream()
                .map(f -> {
                    String thumb = f.getImages().stream()
                            .findFirst()
                            .map(Image::getImageUrl)
                            .orElse("");
                    return new FeedItemResponse(
                            f.getFeedId(),
                            thumb,
                            f.getFeedFavorite()
                    );
                })
                .collect(Collectors.toList());

        return new AlbumDetailResponse(
                album.getAlbumId(),
                album.getAlbumName(),
                albumDate,
                album.getAlbumMemo(),
                feedItems
        );
    }
    /**
     * 새 앨범을 만들고, 전달받은 feedId 리스트의 피드들을
     * 모두 해당 앨범으로 할당한 뒤 생성된 albumId를 반환합니다.
     */
    @Transactional
    public CreateAlbumResponse createAlbum(Integer userId, String albumTitle, List<Integer> feedIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        final Album savedAlbum = albumRepository.save(
                Album.builder()
                        .albumName(albumTitle)
                        .albumMemo("")
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .defaultAlbum(false)
                        .build()
        );

        if (feedIds != null && !feedIds.isEmpty()) {
            List<Feed> feeds = feedRepository.findAllById(feedIds);
            feeds.forEach(f -> f.setAlbum(savedAlbum));
            feedRepository.saveAll(feeds);
        }

        return new CreateAlbumResponse(savedAlbum.getAlbumId());
    }

    /**
     * 앨범 이름/메모 수정
     */
    @Transactional
    public void updateAlbum(Integer userId, UpdateAlbumRequest req) {
        // 1) 사용자 확인 (필요 시)
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        // 2) 앨범 조회 및 소유권 검증
        Album album = albumRepository.findById(req.getAlbumId())
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. id=" + req.getAlbumId()));
        if (!album.getUser().getUserId().equals(userId)) {
            throw new EntityNotFoundException("해당 사용자의 앨범이 아닙니다. id=" + req.getAlbumId());
        }

        // 3) 필드 갱신 (영속성 컨텍스트가 끝나면 자동 반영)
        album.setAlbumName(req.getAlbumName());
        album.setAlbumMemo(req.getAlbumMemo());
    }

    @Transactional
    public void deleteAlbum(Integer userId, Integer albumId) {
        // 1) 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        // 2) 삭제할 앨범 조회
        Album targetAlbum = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("앨범을 찾을 수 없습니다. id=" + albumId));

        // 3) 기본 앨범 조회
        Album defaultAlbum = albumRepository
                .findByUserUserIdAndDefaultAlbumTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("기본 앨범이 존재하지 않습니다. userId=" + userId));

        // 4) 대상 앨범에 속한 피드들 모두 기본 앨범으로 이동
        List<Feed> feeds = targetAlbum.getFeeds();
        feeds.forEach(f -> f.setAlbum(defaultAlbum));
        feedRepository.saveAll(feeds);

        // 5) 앨범 삭제
        albumRepository.delete(targetAlbum);
    }

    /**
     * 기존 피드들을 지정 앨범에 할당합니다.
     */
    @Transactional
    public void addPhotosToAlbum(Integer userId, AddPhotoRequest req) {
        // 1) 앨범 존재 확인
        Album album = albumRepository.findById(req.getAlbumId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "앨범을 찾을 수 없습니다. id=" + req.getAlbumId()
                ));

        // 2) 해당 feed들 조회 후 album 할당
        List<Feed> feeds = feedRepository.findAllById(req.getImageList());
        feeds.forEach(f -> f.setAlbum(album));
        feedRepository.saveAll(feeds);
    }

    /**
     * 앨범에서 사진 삭제:
     * 삭제된 사진(Feed)은 사용자의 defaultAlbum으로 이동
     */
    @Transactional
    public void deletePhotosFromAlbum(Integer userId, DeletePhotoRequest req) {
        // 1) 기본 앨범 찾기
        Album defaultAlbum = albumRepository
                .findByUserUserIdAndDefaultAlbumTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "기본 앨범이 존재하지 않습니다. userId=" + userId
                ));

        // 2) 삭제할 피드들 조회 및 앨범 재설정
        List<Feed> feeds = feedRepository.findAllById(req.getImageList());
        feeds.forEach(f -> f.setAlbum(defaultAlbum));

        // 3) 변경 저장
        feedRepository.saveAll(feeds);
    }
}
