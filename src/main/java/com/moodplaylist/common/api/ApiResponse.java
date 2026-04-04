package com.moodplaylist.common.api;

import java.time.Instant;
import java.util.UUID;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Meta meta
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, new Meta(UUID.randomUUID().toString(), Instant.now()));
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message), new Meta(UUID.randomUUID().toString(), Instant.now()));
    }

    public record ApiError(String code, String message) {}
    public record Meta(String requestId, Instant timestamp) {}
}
