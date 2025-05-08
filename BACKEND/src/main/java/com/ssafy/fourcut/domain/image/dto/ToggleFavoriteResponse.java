package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToggleFavoriteResponse {
    private Integer feedId;
    private Boolean isFavorite;
}
