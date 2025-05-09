package com.ssafy.fourcut.domain.image.controller;

import com.ssafy.fourcut.domain.image.dto.FileUploadRequestDto;
import com.ssafy.fourcut.domain.image.dto.QRUploadRequestDto;
import com.ssafy.fourcut.domain.image.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/photos/upload")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    /*
     * QR 업로드
     */
    @PostMapping("/qr")
    public void uploadQr(@RequestBody QRUploadRequestDto request) {
        log.info("/api/v1/photos/upload/qr");
        log.info("uploadQr request - userId: {}, pageUrl: {}", request.getUserId(), request.getPageUrl());

        // 새로운 feed 생성
        int feedId = storeService.createFeed(request.getUserId());
        request.setFeedId(feedId);

        // 크롤링을 하여, 파일들 저장 및 DB에 데이터 삽입
        storeService.CrawlUploadAndSave(request);
    }

    /*
     * 파일 업로드
     */
    @PostMapping("/file")
    public void uploadFile(
            @RequestPart("dto") FileUploadRequestDto request,
            @RequestPart("files") List<MultipartFile> files) {
        log.info("/api/v1/photos/upload/file");

        // 새로운 feed 생성
        int feedId = storeService.createFeed(request.getUserId());
        request.setFeedId(feedId);

        log.info("userId: {}, feedId: {}", request.getUserId(), request.getFeedId());
        log.info("업로드된 파일 수: {}", files.size());
        storeService.uploadMediaFile(request, files);
    }
}
