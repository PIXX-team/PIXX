package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRequestDto {
    private int userId;
    private int feedId;
    private String pageUrl; // QR 찍은 메인 URL
}
