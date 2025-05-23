package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BrandFeedResponseDto {
    private String brandName;             // 브랜드명
    private List<BrandFeedItemDto> brandFeedList;
}