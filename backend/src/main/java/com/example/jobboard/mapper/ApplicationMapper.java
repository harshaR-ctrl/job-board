package com.example.jobboard.mapper;

import com.example.jobboard.dto.response.ApplicationResponseDto;
import com.example.jobboard.entity.Application;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Application entities and DTOs.
 */
@Component
public class ApplicationMapper {

    private final ModelMapper modelMapper;

    public ApplicationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts an Application entity to an ApplicationResponseDto.
     * Resolves job title, company name, and candidate details from relationships.
     *
     * @param entity the Application entity
     * @return the mapped ApplicationResponseDto
     */
    public ApplicationResponseDto toDto(Application entity) {
        return ApplicationResponseDto.builder()
                .id(entity.getId())
                .jobTitle(entity.getJob() != null ? entity.getJob().getTitle() : "Unknown")
                .companyName(entity.getJob() != null && entity.getJob().getEmployer() != null
                        ? entity.getJob().getEmployer().getName() : "Unknown")
                .candidateName(entity.getCandidate() != null ? entity.getCandidate().getName() : "Unknown")
                .candidateEmail(entity.getCandidate() != null ? entity.getCandidate().getEmail() : null)
                .status(entity.getStatus().name())
                .appliedAt(entity.getAppliedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
