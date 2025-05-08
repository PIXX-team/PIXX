package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor
public class AlbumDetailResponse {
    private Integer albumId;
    private String albumName;
    private LocalDateTime albumDate;       // oldest feedDate in the album
    private String albumMemo;
    private List<FeedItemResponse> albumFeedList;
}
