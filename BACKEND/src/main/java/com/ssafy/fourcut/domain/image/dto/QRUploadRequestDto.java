package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QRUploadRequestDto {
    private int userId;
    private int feedId;
    private String pageUrl;
}
