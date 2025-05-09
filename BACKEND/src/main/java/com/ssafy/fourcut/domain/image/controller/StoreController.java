package com.ssafy.fourcut.domain.image.controller;

import com.ssafy.fourcut.domain.image.dto.FileUploadRequestDto;
import com.ssafy.fourcut.domain.image.dto.QRUploadRequestDto;
import com.ssafy.fourcut.domain.image.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/photos/upload")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /*
     * QR 업로드
     */
    @PostMapping("/qr")
    public void uploadQr(
            Principal principal,
            @RequestBody QRUploadRequestDto request) {
        log.info("/api/v1/photos/upload/qr");
        log.info("uploadQr request - userId: {}, pageUrl: {}", request.getUserId(), request.getPageUrl());
        log.info("userId : " + principal.getName());

        // userId 넣기
        request.setUserId(Integer.parseInt(principal.getName()));

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
            Principal principal,
            @RequestPart("files") List<MultipartFile> files) {
        log.info("/api/v1/photos/upload/file");
        log.info("userId : " + principal.getName());

        FileUploadRequestDto request = new FileUploadRequestDto();

        // userId 넣기
        request.setUserId(Integer.parseInt(principal.getName()));

        // 새로운 feed 생성
        int feedId = storeService.createFeed(request.getUserId());
        request.setFeedId(feedId);

        log.info("userId: {}, feedId: {}", request.getUserId(), request.getFeedId());
        log.info("업로드된 파일 수: {}", files.size());
        storeService.uploadMediaFile(request, files);
    }
}
