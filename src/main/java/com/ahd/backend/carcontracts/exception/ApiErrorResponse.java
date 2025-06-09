package com.ahd.backend.carcontracts.exception;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private Integer code;
    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm",
            timezone = "Asia/Baghdad")
    private Instant date = Instant.now();
}
