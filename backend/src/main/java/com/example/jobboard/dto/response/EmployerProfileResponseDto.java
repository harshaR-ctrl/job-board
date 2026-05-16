package com.example.jobboard.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileResponseDto {

    private Long id;
    private String name;
    private String email;
    private String companyName;
    private String website;
    private String description;
}
