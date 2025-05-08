package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class FeedByBrandResponse {
    private List<BrandGroupResponse> brandList;
}
