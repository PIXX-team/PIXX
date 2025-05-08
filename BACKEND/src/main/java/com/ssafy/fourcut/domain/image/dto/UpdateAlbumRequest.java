package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class UpdateAlbumRequest {
    /** 수정할 앨범 ID */
    private Integer albumId;
    /** 수정할 앨범 이름 */
    private String albumName;
    /** 수정할 앨범 메모 */
    private String albumMemo;
}
