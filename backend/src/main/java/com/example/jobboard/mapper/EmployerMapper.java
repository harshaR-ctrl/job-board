package com.example.jobboard.mapper;

import com.example.jobboard.dto.request.EmployerProfileRequestDto;
import com.example.jobboard.dto.response.EmployerProfileResponseDto;
import com.example.jobboard.entity.EmployerProfile;
import org.springframework.stereotype.Component;

@Component
public class EmployerMapper {

    public EmployerProfileResponseDto toDto(EmployerProfile entity) {
        return EmployerProfileResponseDto.builder()
                .id(entity.getId())
                .name(entity.getUser() != null ? entity.getUser().getName() : null)
                .email(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .companyName(entity.getCompanyName())
                .website(entity.getWebsite())
                .description(entity.getDescription())
                .build();
    }

    public void updateEntityFromDto(EmployerProfileRequestDto dto, EmployerProfile entity) {
        if (dto.getCompanyName() != null) {
            entity.setCompanyName(dto.getCompanyName());
        }
        if (dto.getWebsite() != null) {
            entity.setWebsite(dto.getWebsite());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
