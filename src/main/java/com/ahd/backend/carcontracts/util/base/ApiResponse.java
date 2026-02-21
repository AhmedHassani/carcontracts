package com.ahd.backend.carcontracts.util.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private Integer code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Pagination pagination;
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm",
            timezone = "Asia/Baghdad")
    private Instant date = Instant.now();

public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(HttpStatus.OK.name())
                .code(HttpStatus.OK.value())
                .data(data)
                .date(Instant.now())
                .build();
    }

    public static <T> ApiResponse<List<T>> success(Page<T> page) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .message(HttpStatus.OK.name())
                .code(HttpStatus.OK.value())
                .data(page.getContent())
                .pagination(new Pagination(page))
                .date(Instant.now())
                .build();
    }

    public static ApiResponse<Void> successMessage(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .code(HttpStatus.OK.value())
                .date(Instant.now())
                .build();
    }

    public static ApiResponse<Void> errorMessage(String message, HttpStatus status) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .code(status.value())
                .date(Instant.now())
                .build();
    }
}
