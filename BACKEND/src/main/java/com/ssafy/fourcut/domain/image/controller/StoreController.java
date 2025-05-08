package com.ssafy.fourcut.domain.image.controller;

import com.ssafy.fourcut.domain.image.dto.StoreRequestDto;
import com.ssafy.fourcut.domain.image.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/photos/upload")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping("/qr")
    public void uploadQr(@RequestBody StoreRequestDto request) {

        System.out.println(request.getUserId() + " " + request.getPageUrl());

        // 새로운 feed 생성
        int feedId = storeService.createFeed(request);
        request.setFeedId(feedId);
        // 크롤링을 하여, 파일들 저장 및 DB에 데이터 삽입
        storeService.CrawlUploadAndSave(request);
    }
}
