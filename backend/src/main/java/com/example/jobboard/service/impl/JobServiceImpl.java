package com.example.jobboard.service.impl;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.JobResponseDto;
import com.example.jobboard.entity.JobListing;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.exception.UnauthorizedException;
import com.example.jobboard.mapper.JobMapper;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of {@link JobService} for managing job listings.
 * Handles creation, updates, closing, search, and retrieval of job postings.
 */
@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobMapper = jobMapper;
    }

    /**
     * {@inheritDoc}
     * Creates a new OPEN job listing linked to the authenticated employer.
     */
    @Override
    @Transactional
    public JobResponseDto createJob(JobRequestDto request, String email) {
        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        JobListing job = jobMapper.toEntity(request);
        job.setEmployer(employer);
        job.setStatus(JobStatus.OPEN);

        JobListing savedJob = jobRepository.save(job);
        return jobMapper.toDto(savedJob);
    }

    /**
     * {@inheritDoc}
     * Updates an existing job listing. Only the owning employer can update.
     */
    @Override
    @Transactional
    public JobResponseDto updateJob(Long id, JobRequestDto request, String email) {
        JobListing job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        validateOwnership(job, email);

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());

        JobListing updatedJob = jobRepository.save(job);
        return jobMapper.toDto(updatedJob);
    }

    /**
     * {@inheritDoc}
     * Closes a job listing by setting its status to CLOSED.
     * Only the owning employer can close a listing.
     */
    @Override
    @Transactional
    public JobResponseDto closeJob(Long id, String email) {
        JobListing job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        validateOwnership(job, email);

        job.setStatus(JobStatus.CLOSED);
        JobListing closedJob = jobRepository.save(job);
        return jobMapper.toDto(closedJob);
    }

    /**
     * {@inheritDoc}
     * Returns a paginated list of jobs posted by the authenticated employer.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobResponseDto> getEmployerJobs(String email, Pageable pageable) {
        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return jobRepository.findByEmployerId(employer.getId(), pageable)
                .map(jobMapper::toDto);
    }

    /**
     * {@inheritDoc}
     * Searches public OPEN job listings with optional title, location, and salary filters.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JobResponseDto> searchJobs(String title, String location, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
        return jobRepository.searchJobs(title, location, minSalary, maxSalary, JobStatus.OPEN, pageable)
                .map(jobMapper::toDto);
    }

    /**
     * {@inheritDoc}
     * Retrieves a single job listing by its ID.
     */
    @Override
    @Transactional(readOnly = true)
    public JobResponseDto getJobById(Long id) {
        JobListing job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
        return jobMapper.toDto(job);
    }

    /**
     * Validates that the authenticated user is the owner of the job listing.
     *
     * @param job   the job listing to validate
     * @param email the authenticated user's email
     * @throws UnauthorizedException if the user is not the owner
     */
    private void validateOwnership(JobListing job, String email) {
        if (!job.getEmployer().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to modify this job listing");
        }
    }
}
