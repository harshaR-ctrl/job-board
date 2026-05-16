package com.example.jobboard.dto.response;

import lombok.*;

/**
 * Response DTO returned after successful authentication (login or register).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String role;
    private String email;
    private String name;
    private boolean verified;
}
