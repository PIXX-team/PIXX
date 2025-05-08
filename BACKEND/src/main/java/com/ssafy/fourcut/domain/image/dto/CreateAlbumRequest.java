package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class CreateAlbumRequest {
    private String albumTitle; /** 새로 만들 앨범 제목 */
    private List<Integer> imageList;  /** 이 앨범에 담을 feed ID 리스트 */
}
