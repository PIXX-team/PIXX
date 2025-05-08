// src/main/java/com/ssafy/fourcut/domain/image/dto/FeedAlbumResponse.java
package com.ssafy.fourcut.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedAlbumResponse {
    private List<AlbumResponse> albumList;
}
