package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedImageResponse {
    private Integer imageId;
    private String imageUrl;
    private String imageType;      // enum.name()
}
