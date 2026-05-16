package com.example.jobboard.service;

import com.example.jobboard.dto.request.ApplicationStatusUpdateDto;
import com.example.jobboard.dto.response.ApplicationResponseDto;
import com.example.jobboard.entity.Application;
import com.example.jobboard.entity.JobListing;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.DuplicateApplicationException;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.mapper.ApplicationMapper;
import com.example.jobboard.repository.ApplicationRepository;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ApplicationServiceImpl} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User candidate;
    private User employer;
    private JobListing jobListing;
    private Application application;
    private ApplicationResponseDto applicationResponse;

    @BeforeEach
    void setUp() {
        employer = User.builder()
                .id(1L)
                .name("TechCorp HR")
                .email("employer@example.com")
                .role(Role.EMPLOYER)
                .createdAt(LocalDateTime.now())
                .build();

        candidate = User.builder()
                .id(2L)
                .name("Alice Johnson")
                .email("candidate@example.com")
                .role(Role.CANDIDATE)
                .createdAt(LocalDateTime.now())
                .build();

        jobListing = JobListing.builder()
                .id(1L)
                .employer(employer)
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("180000"))
                .status(JobStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .job(jobListing)
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        applicationResponse = ApplicationResponseDto.builder()
                .id(1L)
                .jobTitle("Senior Java Developer")
                .companyName("TechCorp HR")
                .candidateName("Alice Johnson")
                .status("APPLIED")
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("applyToJob — should create application successfully")
    void applyToJob_ValidRequest_ReturnsApplicationResponse() {
        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobListing));
        when(applicationRepository.existsByCandidateIdAndJobId(2L, 1L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(application);
        when(applicationMapper.toDto(any(Application.class))).thenReturn(applicationResponse);

        ApplicationResponseDto result = applicationService.applyToJob(1L, "candidate@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getJobTitle()).isEqualTo("Senior Java Developer");
        verify(emailService).sendStatusEmail(
                eq("candidate@example.com"),
                eq("Alice Johnson"),
                eq("Senior Java Developer"),
                anyString(),
                eq(ApplicationStatus.APPLIED)
        );
    }

    @Test
    @DisplayName("applyToJob — should throw DuplicateApplicationException when already applied")
    void applyToJob_AlreadyApplied_ThrowsDuplicateException() {
        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobListing));
        when(applicationRepository.existsByCandidateIdAndJobId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.applyToJob(1L, "candidate@example.com"))
                .isInstanceOf(DuplicateApplicationException.class);

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("applyToJob — should throw when job not found")
    void applyToJob_JobNotFound_ThrowsException() {
        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.applyToJob(999L, "candidate@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateApplicationStatus — should update status and trigger email")
    void updateStatus_ValidRequest_UpdatesAndSendsEmail() {
        ApplicationStatusUpdateDto statusUpdate = ApplicationStatusUpdateDto.builder()
                .status("SHORTLISTED")
                .build();

        ApplicationResponseDto updatedResponse = ApplicationResponseDto.builder()
                .id(1L)
                .jobTitle("Senior Java Developer")
                .status("SHORTLISTED")
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);
        when(applicationMapper.toDto(any(Application.class))).thenReturn(updatedResponse);

        ApplicationResponseDto result = applicationService.updateApplicationStatus(
                1L, statusUpdate, "employer@example.com"
        );

        assertThat(result.getStatus()).isEqualTo("SHORTLISTED");
        verify(emailService).sendStatusEmail(
                eq("candidate@example.com"),
                eq("Alice Johnson"),
                eq("Senior Java Developer"),
                anyString(),
                eq(ApplicationStatus.SHORTLISTED)
        );
    }

    @Test
    @DisplayName("getCandidateApplications — should return list of applications")
    void getCandidateApplications_ReturnsList() {
        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(candidate));
        when(applicationRepository.findByCandidateId(2L)).thenReturn(List.of(application));
        when(applicationMapper.toDto(any(Application.class))).thenReturn(applicationResponse);

        List<ApplicationResponseDto> results = applicationService.getCandidateApplications("candidate@example.com");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getJobTitle()).isEqualTo("Senior Java Developer");
    }
}
