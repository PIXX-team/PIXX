package com.ssafy.fourcut.domain.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super("사용자를 찾을 수 없습니다: userId=" + userId);
    }

    public UserNotFoundException(Long kakaoId) {
        super("등록된 사용자가 없습니다: kakaoId=" + kakaoId);
    }
}
