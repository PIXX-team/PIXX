package com.ssafy.fourcut.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
}