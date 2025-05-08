package com.ssafy.fourcut.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;
    private final String bucketName = "filmmoa"; // 너희 S3 버킷 이름

    public String upload(int userId, InputStream inputStream, String originalFilename, String contentType) {
        try {
            String datePath = LocalDate.now().toString().replace("-", "/"); // 2025/04/29
            String s3Key = "users/" + userId + "/" + datePath + "/" + originalFilename;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            amazonS3.putObject(bucketName, s3Key, inputStream, metadata);
            return s3Key;
        } catch (Exception e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

}
