package com.example.jobboard.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating or updating a candidate profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileRequestDto {

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    private String skills;

    @Min(value = 0, message = "Experience years must be non-negative")
    private Integer experienceYears;
}
