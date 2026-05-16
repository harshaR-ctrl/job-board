package com.example.jobboard.service;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.JobResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Service interface for job listing operations.
 */
public interface JobService {

    /**
     * Creates a new job listing for the currently authenticated employer.
     *
     * @param request the job creation request
     * @param email   the employer's email
     * @return the created job response
     */
    JobResponseDto createJob(JobRequestDto request, String email);

    /**
     * Updates an existing job listing owned by the authenticated employer.
     *
     * @param id      the job listing ID
     * @param request the update request
     * @param email   the employer's email
     * @return the updated job response
     */
    JobResponseDto updateJob(Long id, JobRequestDto request, String email);

    /**
     * Closes an existing job listing owned by the authenticated employer.
     *
     * @param id    the job listing ID
     * @param email the employer's email
     * @return the updated job response
     */
    JobResponseDto closeJob(Long id, String email);

    /**
     * Retrieves a paginated list of jobs posted by the authenticated employer.
     *
     * @param email    the employer's email
     * @param pageable pagination parameters
     * @return a page of job responses
     */
    Page<JobResponseDto> getEmployerJobs(String email, Pageable pageable);

    /**
     * Searches public job listings with optional filters.
     *
     * @param title     partial title match
     * @param location  partial location match
     * @param minSalary minimum salary filter
     * @param maxSalary maximum salary filter
     * @param pageable  pagination parameters
     * @return a page of matching job responses
     */
    Page<JobResponseDto> searchJobs(String title, String location, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable);

    /**
     * Retrieves a single job listing by its ID.
     *
     * @param id the job listing ID
     * @return the job response
     */
    JobResponseDto getJobById(Long id);
}
