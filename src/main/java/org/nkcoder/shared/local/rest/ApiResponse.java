package org.nkcoder.shared.local.rest;

import java.time.LocalDateTime;

public record ApiResponse<T>(String message, T data, LocalDateTime timestamp) {
    // Static factory methods
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, LocalDateTime.now());
    }
}
