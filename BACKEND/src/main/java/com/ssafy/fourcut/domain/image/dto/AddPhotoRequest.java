package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class AddPhotoRequest {
    /** 사진을 추가할 앨범 ID */
    private Integer albumId;
    /** 이 앨범에 추가할 feed ID 리스트 */
    private List<Integer> imageList;
}
