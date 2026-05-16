package com.example.jobboard.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a job listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDto {

    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Min(value = 0, message = "Minimum salary must be non-negative")
    private BigDecimal salaryMin;

    @Min(value = 0, message = "Maximum salary must be non-negative")
    private BigDecimal salaryMax;
}
