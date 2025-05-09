package com.ssafy.fourcut.domain.image.controller;

import com.ssafy.fourcut.domain.image.entity.Image;
import com.ssafy.fourcut.domain.image.repository.StoreRepository;
import com.ssafy.fourcut.domain.image.service.CloudFrontService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class FileDownloadController {
    private final CloudFrontService cloudFrontService;
    private final StoreRepository storeRepository;

    @GetMapping("/download/{imageId}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable int imageId) {
        log.info("/api/v1/photos/download/" + imageId);
        Image image = storeRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 imageId가 존재하지 않습니다."));

        String s3Key = image.getImageUrl();
        String signedUrl = cloudFrontService.generateSignedCloudFrontUrl(s3Key, "download");

        Map<String, String> response = new HashMap<>();
        response.put("signedUrl", signedUrl);

        return ResponseEntity.ok(response);
    }
}