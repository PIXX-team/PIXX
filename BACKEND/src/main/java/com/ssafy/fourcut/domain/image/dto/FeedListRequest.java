// src/main/java/com/ssafy/fourcut/domain/image/dto/FeedListRequest.java
package com.ssafy.fourcut.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class FeedListRequest {
    /** 0: 최신순, 1: 오래된 순, 2: 브랜드 순 */
    private int type;
    /** 0부터 시작하는 페이지 번호 (default 0) */
    private int page = 0;
    /** 한 페이지에 몇 개씩 가져올지 (default 20) */
    private int size = 20;
}
