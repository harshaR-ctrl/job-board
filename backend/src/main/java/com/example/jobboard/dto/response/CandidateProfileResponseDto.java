package com.example.jobboard.dto.response;

import lombok.*;

/**
 * Response DTO for candidate profile data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String resumeUrl;
    private String skills;
    private Integer experienceYears;
}
