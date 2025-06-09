package com.ahd.backend.carcontracts.util.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm",
            timezone = "Asia/Baghdad")
    private Instant date = Instant.now();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pagination pagination;

    public ApiResponse(boolean success, String message, Integer code, T data) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
    }
}