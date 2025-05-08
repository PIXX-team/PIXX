package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponse {
    private Integer albumId;
    private String albumName;
    // 앨범 속 피드 중 생성일이 가장 과거인 날짜
    private LocalDateTime albumDate;
}
