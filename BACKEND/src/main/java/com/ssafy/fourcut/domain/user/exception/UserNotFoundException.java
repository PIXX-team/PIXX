package com.ssafy.fourcut.domain.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super("사용자를 찾을 수 없습니다.");
    }
}