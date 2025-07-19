package com.ahd.backend.carcontracts.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /* ─────────── AUTH / RSE ─────────── */

    @ExceptionHandler({ ResponseStatusException.class, AuthenticationException.class })
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exception ex) {
        HttpStatus status;
        String message;
        if (ex instanceof ResponseStatusException rse) {
            status  = (HttpStatus) rse.getStatusCode();
            message = rse.getReason();
        } else {
            status  = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage();
        }
        return buildResponse(status, message, null);
    }

    /* ─────────── 404 / 400 ─────────── */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /* ─────────── BEAN-VALIDATION ─────────── */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Invalid input");
        return buildResponse(HttpStatus.BAD_REQUEST, msg, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /* ─────────── UNIQUE-CONSTRAINT / 409 ─────────── */

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(),
                Map.of(ex.getField(), ex.getValue()));
    }

    /** Fallback when service forgot to pre-check and DB threw the error */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrity(DataIntegrityViolationException ex) {
        String msg = "Unique field already exists";
        return buildResponse(HttpStatus.CONFLICT, msg, null);
    }

    /* ─────────── CATCH-ALL ─────────── */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error: " + ex.getMessage(), null);
    }

    /* ─────────── builder ─────────── */

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           Map<String, Object> details) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .code(status.value())
                .message(message)
                .date(Instant.now())
                .build();
        return new ResponseEntity<>(error, status);
    }
}
