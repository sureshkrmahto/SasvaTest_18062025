package com.acme.javamigrator.server.config;

import com.acme.javamigrator.server.dto.CommonDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonDtos.ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        CommonDtos.ApiResponse<Object> res = new CommonDtos.ApiResponse<>();
        res.success = false;
        res.error = new CommonDtos.ApiError();
        res.error.code = "INVALID_REQUEST";
        res.error.message = ex.getMessage();
        res.error.details = ex.getMessage();
        res.error.correlation_id = UUID.randomUUID().toString();
        res.timestamp = Instant.now();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDtos.ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        CommonDtos.ApiResponse<Object> res = new CommonDtos.ApiResponse<>();
        res.success = false;
        res.error = new CommonDtos.ApiError();
        res.error.code = "INVALID_REQUEST";
        res.error.message = "Request body validation failed";
        res.error.validation_errors = ex.getBindingResult().getFieldErrors().stream().map(fe -> {
            CommonDtos.FieldError e = new CommonDtos.FieldError();
            e.field = fe.getField(); e.message = fe.getDefaultMessage(); e.code = "FIELD_ERROR"; return e; }).toList();
        res.error.correlation_id = UUID.randomUUID().toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonDtos.ApiResponse<Object>> handleGeneric(Exception ex) {
        CommonDtos.ApiResponse<Object> res = new CommonDtos.ApiResponse<>();
        res.success = false;
        res.error = new CommonDtos.ApiError();
        res.error.code = "INTERNAL_SERVER_ERROR";
        res.error.message = "Server-side error";
        res.error.details = ex.getMessage();
        res.error.correlation_id = UUID.randomUUID().toString();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}
