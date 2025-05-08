package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class DeleteAlbumRequest {
    /** 삭제할 앨범 ID */
    private Integer albumId;
}
