package com.example.jobboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for updating the status of a job application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusUpdateDto {

    @NotBlank(message = "Status is required")
    private String status;
}
