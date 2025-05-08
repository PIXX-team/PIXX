package com.ssafy.fourcut.domain.image.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class FeedDetailResponse {
    private List<FeedImageResponse> feedList;
    private String feedTitle;
    private List<String> feedHashtags;
    private String feedMemo;
    private String brandName;
    private String feedLocation;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime feedDate;
    private Boolean feedFavorite;
}
