package com.ssafy.fourcut.domain.image.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DeletePhotoRequest {
    /** 삭제할 사진(Feed) 이 원래 속해 있던 앨범 ID (안 써도 무방) */
    private Integer albumId;
    /** 삭제할 피드 ID 리스트 */
    private List<Integer> imageList;
}
