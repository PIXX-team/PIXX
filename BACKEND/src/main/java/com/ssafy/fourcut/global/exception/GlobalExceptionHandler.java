package com.ssafy.fourcut.global.exception;

import com.ssafy.fourcut.domain.user.exception.UserNotFoundException;
import com.ssafy.fourcut.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse<Object> body = ApiResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Object>> handleKakaoApiError(HttpClientErrorException ex) {
        ApiResponse<Object> body = ApiResponse.builder()
                .status(ex.getStatusCode().value())
                .message("카카오 API 요청 실패: " + ex.getResponseBodyAsString())
                .data(null)
                .build();
        return new ResponseEntity<>(body, ex.getStatusCode());
    }

}

