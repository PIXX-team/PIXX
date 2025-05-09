package com.ssafy.fourcut.domain.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CustomExceptions {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class MissingCodeException extends RuntimeException {
        public MissingCodeException() {
            super("code 파라미터를 입력해 주세요.");
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class KakaoApiException extends RuntimeException {
        public KakaoApiException(String detail) {
            super("카카오 API 요청 실패: " + detail);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class JwtInvalidException extends RuntimeException {
        public JwtInvalidException() {
            super("유효하지 않은 토큰입니다.");
        }
    }

}