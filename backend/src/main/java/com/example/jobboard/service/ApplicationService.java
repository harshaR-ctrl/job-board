package com.example.jobboard.service;

import com.example.jobboard.dto.request.ApplicationStatusUpdateDto;
import com.example.jobboard.dto.response.ApplicationResponseDto;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Service interface for job application operations.
 */
public interface ApplicationService {

    /**
     * Submits a new application for the given job on behalf of the authenticated candidate.
     *
     * @param jobId the job listing ID to apply to
     * @param email the candidate's email
     * @return the created application response
     */
    ApplicationResponseDto applyToJob(Long jobId, String email);

    /**
     * Retrieves all applications submitted by the authenticated candidate.
     *
     * @param email the candidate's email
     * @return list of application responses
     */
    List<ApplicationResponseDto> getCandidateApplications(String email);

    /**
     * Retrieves all applications for a specific job listing (employer view).
     *
     * @param jobId the job listing ID
     * @param email the employer's email (for ownership validation)
     * @return list of application responses
     */
    List<ApplicationResponseDto> getApplicationsForJob(Long jobId, String email);

    /**
     * Updates the status of an application and triggers an email notification.
     *
     * @param applicationId the application ID
     * @param request       the status update request
     * @param email         the employer's email (for ownership validation)
     * @return the updated application response
     */
    ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateDto request, String email);

    /**
     * Withdraws an application (candidate only).
     *
     * @param applicationId the application ID
     * @param email         the candidate's email
     * @return the updated application response
     */
    ApplicationResponseDto withdrawApplication(Long applicationId, String email);

    /**
     * Returns the resume file for a candidate's application (employer only).
     *
     * @param applicationId the application ID
     * @param email         the employer's email
     * @return the resume file resource
     */
    Resource getCandidateResume(Long applicationId, String email);
}
