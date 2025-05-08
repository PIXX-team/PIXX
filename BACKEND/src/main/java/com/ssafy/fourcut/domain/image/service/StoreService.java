package com.ssafy.fourcut.domain.image.service;

import com.ssafy.fourcut.domain.image.entity.Album;
import com.ssafy.fourcut.domain.image.entity.Brand;
import com.ssafy.fourcut.domain.image.entity.Feed;
import com.ssafy.fourcut.domain.image.dto.StoreRequestDto;
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
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
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

    /*
     * feed 테이블을 새로 만든다.
     */
    @Transactional
    public int createFeed(StoreRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 userId가 존재하지 않습니다."));
        Album album = albumRepository.findByUser_UserIdAndDefaultAlbumTrue(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Default album not found"));
        System.out.println("album :: " + album.getAlbumId());
        Brand brand = brandRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Brand not found"));
        Feed feed = Feed.builder()
                .user(user)
                .album(album)
                .brand(brand)
                .feedFavorite(false)
                .feedLocation("인원을 입력해주세요")
                .feedMemo("메모를 작성해주세요")
                .feedPopulation(null)
                .feedTitle("제목을 작성해주세요")
                .build();

        Feed savedFeed = feedRepository.save(feed);
        return savedFeed.getFeedId();
    }

    /*
     * 받은 URL을 크롤링하여 파일들을 다운로드한다.
     */
    @Transactional
    public void CrawlUploadAndSave(StoreRequestDto request) {
        try {

            // 브랜드 별 크롤링 메서드 호출
            if (request.getPageUrl().contains("monomansion.net")) {
                crawlMonomansion(request);
            } else if (request.getPageUrl().contains("haru7")) {
                crawlharu(request);
            } else if (request.getPageUrl().contains("seobuk.kr")) {
                crawlPhotoism(request);
            } else {
                throw new IllegalArgumentException("지원하지 않는 브랜드입니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("크롤링 및 다운로드 실패", e);
        }
    }

    // 모노맨션 크롤링
    private void crawlMonomansion(StoreRequestDto request) throws Exception {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        Document doc = Jsoup.connect(request.getPageUrl()).get();
        Elements links = doc.select("a");

        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.contains("download.php")) {
                uploadAndSave(request.getUserId(), href, feed);
            }
        }
    }
    
    // 하루필름 크롤링
    private void crawlharu(StoreRequestDto request) throws Exception {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        Document doc = Jsoup.connect(request.getPageUrl()).get();
        Elements links = doc.select("a");

        for (Element link : links) {
            String href = link.absUrl("href");
            if (href.contains("base_api")) {
                uploadAndSave(request.getUserId(), href, feed);
            }
        }
    }

    // 포토이즘 크롤링
    private void crawlPhotoism(StoreRequestDto request) throws Exception {
        System.out.println("photoism");
        System.out.println(request.getUserId() + " " + request.getPageUrl() + " " + request.getFeedId());
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new IllegalArgumentException("Feed를 찾을 수 없습니다."));

        // 1. uid 파라미터 추출
        String uid = extractUidFromUrl(request.getPageUrl());
        System.out.println("uid: " + uid);

        // 2. POST 요청 보내기
        URL apiUrl = new URL("https://cmsapi.seobuk.kr/v1/etc/seq/resource");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = "{\"uid\":\"" + uid + "\"}";
        System.out.println("payload: " + payload);
        conn.getOutputStream().write(payload.getBytes());

        // 3. 응답 JSON 파싱
        String response = new String(conn.getInputStream().readAllBytes());
        JSONObject json = new JSONObject(response);
        JSONObject fileInfo = json.getJSONObject("content").getJSONObject("fileInfo");

        // 4. path 추출
        String imagePath = fileInfo.getJSONObject("picFile").getString("path");
        String videoPath = fileInfo.getJSONObject("vidFile").getString("path");

        System.out.println("imagePath : " + imagePath);
        System.out.println("videoPath : " + videoPath);

        // 5. S3 업로드 및 DB 저장
        uploadAndSave(request.getUserId(), imagePath, feed);
        uploadAndSave(request.getUserId(), videoPath, feed);
    }

    private String extractUidFromUrl(String url) {
        int idx = url.indexOf("u=");
        if (idx == -1) throw new IllegalArgumentException("uid 파라미터가 없습니다.");
        return url.substring(idx + 2);
    }

    /*
     * S3 업로드 및 DB 저장을 한다.
     */
    private void uploadAndSave(int userId, String fileUrl, Feed feed) {
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

                System.out.println("contentType: " + contentType);
                System.out.println("fileUrl: " + fileUrl);

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
}