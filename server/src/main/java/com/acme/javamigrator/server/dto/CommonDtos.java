package com.acme.javamigrator.server.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CommonDtos {
    public static class ApiResponse<T> {
        public boolean success;
        public T data;
        public ApiError error;
        public Instant timestamp = Instant.now();
    }
    public static class ApiError {
        public String code;
        public String message;
        public String details;
        public String correlation_id;
        public List<FieldError> validation_errors;
    }
    public static class FieldError {
        public String field;
        public String message;
        public String code;
    }
}
