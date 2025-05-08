package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class BrandGroupResponse {
    private String brandName;
    private List<FeedItemResponse> feeds;
}
