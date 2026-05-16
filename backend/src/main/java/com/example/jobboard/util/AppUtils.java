package com.example.jobboard.util;

import com.example.jobboard.dto.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * General-purpose utility methods used across the application.
 */
public final class AppUtils {

    private AppUtils() {
        // Prevent instantiation
    }

    /**
     * Extracts the currently authenticated user's email from the SecurityContext.
     *
     * @return the current user's email address
     * @throws IllegalStateException if no authentication is present
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return authentication.getName();
    }

    /**
     * Builds a success API response with a message and data payload.
     *
     * @param message the success message
     * @param data    the response data
     * @param <T>     the data type
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> buildSuccessResponse(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Builds an error API response with a message and no data.
     *
     * @param message the error message
     * @return an error ApiResponse
     */
    public static ApiResponse<Void> buildErrorResponse(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }
}
