package com.example.jobboard.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for application data including job and company context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponseDto {

    private Long id;
    private String jobTitle;
    private String companyName;
    private String candidateName;
    private String candidateEmail;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
