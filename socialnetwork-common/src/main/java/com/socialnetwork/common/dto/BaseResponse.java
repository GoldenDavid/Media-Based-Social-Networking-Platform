package com.socialnetwork.common.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response envelope used by all REST controllers.
 *
 * <p>Format:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Success",
 *   "timestamp": "2025-06-02T15:00:00Z",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>Use {@code @JsonInclude(NON_NULL)} so that {@code data} is omitted on
 * error responses.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private boolean success;
    private String message;
    private Instant timestamp;
    private T data;

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .message("Success")
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> ok() {
        return ok(null);
    }

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
