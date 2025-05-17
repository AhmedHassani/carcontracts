package com.ahd.backend.carcontracts.util.base;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response
    ) {
        if (body instanceof ApiResponse) {
            return body;
        }
        int rawStatus = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse) {
            rawStatus = ((ServletServerHttpResponse) response)
                    .getServletResponse()
                    .getStatus();
        }
        HttpStatus status = HttpStatus.resolve(rawStatus);
        if (status != null && status.is2xxSuccessful()) {
            return new ApiResponse<>(
                    true,
                    "OK",
                    rawStatus,
                    body
            );
        }
        return body;
    }
}
