package com.ssafy.fourcut.domain.image.service;

import com.ssafy.fourcut.domain.image.dto.FileUploadRequestDto;
import com.ssafy.fourcut.domain.image.entity.Album;
import com.ssafy.fourcut.domain.image.entity.Brand;
import com.ssafy.fourcut.domain.image.entity.Feed;
import com.ssafy.fourcut.domain.image.dto.QRUploadRequestDto;
import com.ssafy.fourcut.domain.image.entity.Image;
import com.ssafy.fourcut.domain.image.entity.enums.ImageType;
import com.ssafy.fourcut.domain.image.repository.AlbumRepository;
import com.ssafy.fourcut.domain.image.repository.BrandRepository;
import com.ssafy.fourcut.domain.image.repository.FeedRepository;
import com.ssafy.fourcut.domain.image.repository.StoreRepository;
import com.ssafy.fourcut.domain.user.entity.User;
import com.ssafy.fourcut.domain.user.repository.UserRepository;
import com.ssafy.fourcut.global.s3.S3Uploader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final AlbumRepository albumRepository;
    private final BrandRepository brandRepository;
    private final StoreRepository storeRepository;
    private final S3Uploader s3Uploader;

    private static final Logger log = LoggerFactory.getLogger(StoreService.class);

    /*
     * feed 테이블을 새로 만든다.
     */
    @Transactional
    public int createFeed(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId가 존재하지 않습니다."));
        log.info("userId : " + user.getUserId());
        Album album = albumRepository.findByUser_UserIdAndDefaultAlbumTrue(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Default album not found"));
        log.info("album : " + album.getAlbumId());
        Brand brand = brandRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Brand not found"));
        Feed feed = Feed.builder()
                .user(user)
                .album(album)
                .brand(brand)
                .feedFavorite(false)
                .feedLocation("위치를 입력해주세요!")
                .feedMemo("메모를 작성해주세요!")
                .feedPopulation(null)
                .feedTitle("제목을 작성해주세요!")
                .build();

        Feed savedFeed = feedRepository.save(feed);
        return savedFeed.getFeedId();
    }

    /*
     * 각 브랜드에 맞춰, brandId를 수정한다.
     */
    private void updateBrand(Feed feed, int brandId, String logMessage) {
        log.info(logMessage);
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalStateException("Brand " + brandId + " not found"));
        feed.setBrand(brand);
        feedRepository.save(feed);
    }

    /*
     * 받은 URL을 크롤링하여 파일들을 다운로드한다.
     */
    @Transactional
    public void CrawlUploadAndSave(QRUploadRequestDto request) {
        try {
            Feed feed = feedRepository.findById(request.getFeedId())
                    .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

            // 브랜드 별 크롤링 메서드 호출
            if (request.getPageUrl().contains("monomansion.net")) {
                updateBrand(feed, 2, "모노맨션 크롤링");
                crawlMonomansion(request);
            } else if (request.getPageUrl().contains("haru7")) {
                updateBrand(feed, 3, "하루필름 크롤링");
                crawlharu(request);
            } else if (request.getPageUrl().contains("seobuk.kr")) {
                updateBrand(feed, 4, "포토이즘 크롤링");
                crawlPhotoism(request);
            } else {
                throw new IllegalArgumentException("지원하지 않는 브랜드입니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("크롤링 및 다운로드 실패", e);
        }
    }

    // 모노맨션 크롤링
    private void crawlMonomansion(QRUploadRequestDto request) throws Exception {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        Document doc = Jsoup.connect(request.getPageUrl()).get();
        Elements links = doc.select("a");

        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.contains("download.php")) {
                QRuploadAndSave(request.getUserId(), href, feed);
            }
        }
    }
    
    // 하루필름 크롤링
    private void crawlharu(QRUploadRequestDto request) throws Exception {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        Document doc = Jsoup.connect(request.getPageUrl()).get();
        Elements links = doc.select("a");

        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.contains("base_api")) {
                QRuploadAndSave(request.getUserId(), href, feed);
            }
        }
    }

    // 포토이즘 크롤링
    private void crawlPhotoism(QRUploadRequestDto request) throws Exception {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        // 1. uid 파라미터 추출
        String uid = extractUidFromUrl(request.getPageUrl());
        log.info("uid : " + uid);

        // 2. POST 요청 보내기
        URL apiUrl = new URL("https://cmsapi.seobuk.kr/v1/etc/seq/resource");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = "{\"uid\":\"" + uid + "\"}";
        log.info("payload : " + payload);
        conn.getOutputStream().write(payload.getBytes());

        // 3. 응답 JSON 파싱
        String response = new String(conn.getInputStream().readAllBytes());
        JSONObject json = new JSONObject(response);
        JSONObject fileInfo = json.getJSONObject("content").getJSONObject("fileInfo");

        // 4. path 추출
        String imagePath = fileInfo.getJSONObject("picFile").getString("path");
        String videoPath = fileInfo.getJSONObject("vidFile").getString("path");

        log.info("포토이즘 ImagePath : " + imagePath);
        log.info("포토이즘 VideoPath : " +  videoPath);

        // 5. S3 업로드 및 DB 저장
        QRuploadAndSave(request.getUserId(), imagePath, feed);
        QRuploadAndSave(request.getUserId(), videoPath, feed);
    }

    private String extractUidFromUrl(String url) {
        int idx = url.indexOf("u=");
        if (idx == -1) throw new IllegalArgumentException("uid 파라미터가 없습니다.");
        return url.substring(idx + 2);
    }

    /*
     * QR 관련 S3 및 DB 저장 메서드
     * S3 업로드 및 DB 저장을 한다.
     */
    private void QRuploadAndSave(int userId, String fileUrl, Feed feed) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream()) {

                String contentType = connection.getContentType();

                // 2. contentType이 부정확한 경우 확장자 추출해서 보정
                String extension;
                if ("application/octet-stream".equals(contentType)) {
                    extension = getExtensionFromUrl(fileUrl);
                    contentType = detectContentTypeFromExtension(extension);
                } else {
                    extension = getExtensionByContentType(contentType);
                }

//                String extension = getExtensionByContentType(contentType);
                String originalFilename = UUID.randomUUID().toString() + extension;
                String s3Key = s3Uploader.upload(userId, inputStream, originalFilename, contentType);

                storeRepository.save(
                        Image.builder()
                                .feed(feed)
                                .imageUrl(s3Key)
                                .imageType(detectImageType(contentType))
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드 및 업로드 실패", e);
        }
    }

    private String getExtensionByContentType(String contentType) {
        if (contentType == null) return "";

        switch (contentType) {
            case "image/jpeg": return ".jpg";
            case "image/png": return ".png";
            case "image/gif": return ".gif";
            case "video/mp4": return ".mp4";
            default: return ".bin"; // 알 수 없는 경우
        }
    }

    private ImageType detectImageType(String contentType) {
        if (contentType.startsWith("image/")) {
            if (contentType.equals("image/gif")) return ImageType.GIF;
            else return ImageType.IMAGE;
        }
        if (contentType.startsWith("video/")) return ImageType.VIDEO;
        throw new IllegalArgumentException("알 수 없는 파일 타입: " + contentType);
    }


    // 포토이즘 관련
    private String getExtensionFromUrl(String fileUrl) {
        if (fileUrl.endsWith(".jpg")) return ".jpg";
        if (fileUrl.endsWith(".png")) return ".png";
        if (fileUrl.endsWith(".gif")) return ".gif";
        if (fileUrl.endsWith(".mp4")) return ".mp4";
        return ".bin";
    }

    private String detectContentTypeFromExtension(String extension) {
        switch (extension) {
            case ".jpg": return "image/jpeg";
            case ".png": return "image/png";
            case ".gif": return "image/gif";
            case ".mp4": return "video/mp4";
            default: return "application/octet-stream";
        }
    }

    public void uploadMediaFile(FileUploadRequestDto request, List<MultipartFile> files) {
        int userId = request.getUserId();

        // Feed 엔티티 조회 또는 생성 필요
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid feedId: " + request.getFeedId()));

        for (MultipartFile file : files) {
            try (InputStream inputStream = file.getInputStream()) {
                String contentType = file.getContentType();
                String extension = getExtensionByContentType(contentType);

                String originalFilename = UUID.randomUUID().toString() + extension;
                String s3Key = s3Uploader.upload(userId, inputStream, originalFilename, contentType);

                storeRepository.save(
                        Image.builder()
                                .feed(feed)
                                .imageUrl(s3Key)
                                .imageType(detectImageType(contentType)) // photo, gif, video 등
                                .createdAt(LocalDateTime.now())
                                .build()
                );

            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }
    }
}