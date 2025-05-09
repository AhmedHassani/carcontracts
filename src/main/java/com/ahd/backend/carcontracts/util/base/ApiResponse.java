package com.ahd.backend.carcontracts.util.base;

import lombok.*;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private Integer code;
    private T data;
    private Instant timestamp = Instant.now();

    public ApiResponse(boolean success, String message, Integer code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
    }
}