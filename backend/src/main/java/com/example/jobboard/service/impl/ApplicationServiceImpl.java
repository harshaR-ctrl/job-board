package com.example.jobboard.service.impl;

import com.example.jobboard.dto.request.ApplicationStatusUpdateDto;
import com.example.jobboard.dto.response.ApplicationResponseDto;
import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.JobListing;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.exception.DuplicateApplicationException;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.exception.UnauthorizedException;
import com.example.jobboard.mapper.ApplicationMapper;
import com.example.jobboard.repository.ApplicationRepository;
import com.example.jobboard.repository.CandidateProfileRepository;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ApplicationService} for managing job applications.
 * Handles applying to jobs, retrieving applications, and updating status with email notifications.
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ApplicationMapper applicationMapper;
    private final EmailService emailService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  JobRepository jobRepository,
                                  UserRepository userRepository,
                                  CandidateProfileRepository candidateProfileRepository,
                                  ApplicationMapper applicationMapper,
                                  EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.applicationMapper = applicationMapper;
        this.emailService = emailService;
    }

    /**
     * {@inheritDoc}
     * Creates a new application for the candidate. Validates the job is OPEN
     * and the candidate hasn't already applied. Sends an email notification.
     */
    @Override
    @Transactional
    public ApplicationResponseDto applyToJob(Long jobId, String email) {
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        JobListing job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("This job listing is closed and no longer accepting applications");
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new DuplicateApplicationException(candidate.getId(), jobId);
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.APPLIED)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Send email notification asynchronously
        String companyName = job.getEmployer() != null ? job.getEmployer().getName() : "Unknown";
        emailService.sendStatusEmail(
                candidate.getEmail(),
                candidate.getName(),
                job.getTitle(),
                companyName,
                ApplicationStatus.APPLIED
        );

        return applicationMapper.toDto(savedApplication);
    }

    /**
     * {@inheritDoc}
     * Returns all applications submitted by the authenticated candidate,
     * sorted by most recent first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getCandidateApplications(String email) {
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return applicationRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Returns all applications for a specific job listing.
     * Only the employer who owns the listing can view applicants.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsForJob(Long jobId, String email) {
        JobListing job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getEmployer().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to view applicants for this job listing");
        }

        return applicationRepository.findByJobId(jobId)
                .stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Updates the application status and triggers the appropriate email notification.
     * Only the employer who owns the associated job listing can update status.
     */
    @Override
    @Transactional
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateDto request, String email) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        JobListing job = application.getJob();
        if (!job.getEmployer().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to update this application");
        }

        ApplicationStatus newStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        application.setStatus(newStatus);

        Application updatedApplication = applicationRepository.save(application);

        // Send email notification asynchronously
        User candidate = application.getCandidate();
        String companyName = job.getEmployer() != null ? job.getEmployer().getName() : "Unknown";
        emailService.sendStatusEmail(
                candidate.getEmail(),
                candidate.getName(),
                job.getTitle(),
                companyName,
                newStatus
        );

        return applicationMapper.toDto(updatedApplication);
    }

    /**
     * {@inheritDoc}
     * Withdraws an application. Only the candidate who owns the application can withdraw,
     * and only if the current status is APPLIED.
     */
    @Override
    @Transactional
    public ApplicationResponseDto withdrawApplication(Long applicationId, String email) {
        User candidate = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You are not authorized to withdraw this application");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new IllegalArgumentException("Only applications with status APPLIED can be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        Application updatedApplication = applicationRepository.save(application);

        return applicationMapper.toDto(updatedApplication);
    }

    /**
     * {@inheritDoc}
     * Returns the resume file for a candidate's application.
     * Only the employer who owns the job listing can download the resume.
     */
    @Override
    @Transactional(readOnly = true)
    public Resource getCandidateResume(Long applicationId, String email) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        JobListing job = application.getJob();
        if (!job.getEmployer().getEmail().equals(email)) {
            throw new UnauthorizedException("You are not authorized to view this application's resume");
        }

        User candidate = application.getCandidate();
        CandidateProfile profile = candidateProfileRepository.findByUserId(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile", "userId", candidate.getId()));

        if (profile.getResumeUrl() == null || profile.getResumeUrl().isBlank()) {
            throw new ResourceNotFoundException("Resume", "candidateId", candidate.getId());
        }

        Path uploadDirPath = Paths.get(uploadDir).toAbsolutePath();
            Path filePath = uploadDirPath.resolve(profile.getResumeUrl()).normalize();
            
            // Prevent path traversal attacks
            if (!filePath.toAbsolutePath().startsWith(uploadDirPath)) {
                throw new UnauthorizedException("Invalid file path");
            }
            
            return new org.springframework.core.io.FileSystemResource(filePath.toFile());
    }
}
