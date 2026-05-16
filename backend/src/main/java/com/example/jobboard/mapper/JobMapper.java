package com.example.jobboard.mapper;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.JobResponseDto;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.JobListing;
import com.example.jobboard.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between JobListing entities and DTOs.
 * Uses ModelMapper for base mapping and adds custom field resolution.
 */
@Component
public class JobMapper {

    private final ModelMapper modelMapper;

    public JobMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a JobListing entity to a JobResponseDto.
     * Resolves the company name from the employer's profile or uses the employer's name.
     *
     * @param entity the JobListing entity
     * @return the mapped JobResponseDto
     */
    public JobResponseDto toDto(JobListing entity) {
        return JobResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .location(entity.getLocation())
                .salaryMin(entity.getSalaryMin())
                .salaryMax(entity.getSalaryMax())
                .status(entity.getStatus().name())
                .companyName(entity.getEmployer() != null ? entity.getEmployer().getName() : "Unknown")
                .employerEmail(entity.getEmployer() != null ? entity.getEmployer().getEmail() : null)
                .postedAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a JobRequestDto to a JobListing entity.
     * Note: the employer relationship must be set separately.
     *
     * @param dto the JobRequestDto
     * @return the mapped JobListing entity (without employer set)
     */
    public JobListing toEntity(JobRequestDto dto) {
        JobListing job = modelMapper.map(dto, JobListing.class);
        return job;
    }
}
