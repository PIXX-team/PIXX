package com.ssafy.fourcut.domain.image.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class UpdateFeedRequest {
    private Integer feedId;
    private String feedTitle;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime feedDate;
    private String location;
    private String brandName;
    private String feedMemo;
    private List<String> hashtags;
}
