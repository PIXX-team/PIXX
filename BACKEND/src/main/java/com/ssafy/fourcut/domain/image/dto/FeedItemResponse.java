package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 날짜별 사진 보기 응답 값
 */

@Getter
@AllArgsConstructor
public class FeedItemResponse {
    private Integer feedId;
    private String feedThumbnailImgUrl;
    private Boolean feedFavorite;
}
