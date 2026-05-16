package com.example.jobboard.service;

import com.example.jobboard.dto.request.CandidateProfileRequestDto;
import com.example.jobboard.dto.response.CandidateProfileResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for candidate profile operations.
 */
public interface CandidateService {

    /**
     * Retrieves the profile of the authenticated candidate.
     *
     * @param email the candidate's email
     * @return the candidate profile response
     */
    CandidateProfileResponseDto getProfile(String email);

    /**
     * Updates the profile of the authenticated candidate.
     *
     * @param email   the candidate's email
     * @param request the profile update request
     * @return the updated candidate profile response
     */
    CandidateProfileResponseDto updateProfile(String email, CandidateProfileRequestDto request);

    /**
     * Uploads a resume file for the authenticated candidate.
     *
     * @param email the candidate's email
     * @param file  the resume file (multipart)
     * @return the updated candidate profile response with resume URL
     */
    CandidateProfileResponseDto uploadResume(String email, MultipartFile file);

    /**
     * Downloads the authenticated candidate's resume file.
     *
     * @param email the candidate's email
     * @return the resume file resource
     */
    Resource downloadResume(String email);
}
