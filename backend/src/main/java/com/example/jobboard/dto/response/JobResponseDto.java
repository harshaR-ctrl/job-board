package com.example.jobboard.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for job listing data returned to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    private Long id;
    private String title;
    private String description;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String status;
    private String companyName;
    private String employerEmail;
    private LocalDateTime postedAt;
    private LocalDateTime updatedAt;
}
