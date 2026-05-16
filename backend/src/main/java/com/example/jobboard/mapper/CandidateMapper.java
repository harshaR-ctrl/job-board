package com.example.jobboard.mapper;

import com.example.jobboard.dto.request.CandidateProfileRequestDto;
import com.example.jobboard.dto.response.CandidateProfileResponseDto;
import com.example.jobboard.entity.CandidateProfile;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between CandidateProfile entities and DTOs.
 */
@Component
public class CandidateMapper {

    private final ModelMapper modelMapper;

    public CandidateMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts a CandidateProfile entity to a CandidateProfileResponseDto.
     * Includes user-level details (name, email) from the associated User entity.
     *
     * @param entity the CandidateProfile entity
     * @return the mapped CandidateProfileResponseDto
     */
    public CandidateProfileResponseDto toDto(CandidateProfile entity) {
        return CandidateProfileResponseDto.builder()
                .id(entity.getId())
                .name(entity.getUser() != null ? entity.getUser().getName() : null)
                .email(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .phone(entity.getPhone())
                .resumeUrl(entity.getResumeUrl())
                .skills(entity.getSkills())
                .experienceYears(entity.getExperienceYears())
                .build();
    }

    /**
     * Updates an existing CandidateProfile entity from a request DTO.
     * Only updates non-null fields.
     *
     * @param dto    the update request DTO
     * @param entity the existing entity to update
     */
    public void updateEntityFromDto(CandidateProfileRequestDto dto, CandidateProfile entity) {
        if (dto.getPhone() != null) {
            entity.setPhone(dto.getPhone());
        }
        if (dto.getSkills() != null) {
            entity.setSkills(dto.getSkills());
        }
        if (dto.getExperienceYears() != null) {
            entity.setExperienceYears(dto.getExperienceYears());
        }
    }
}
