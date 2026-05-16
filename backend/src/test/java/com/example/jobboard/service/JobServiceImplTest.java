package com.example.jobboard.service;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.JobResponseDto;
import com.example.jobboard.entity.JobListing;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.exception.UnauthorizedException;
import com.example.jobboard.mapper.JobMapper;
import com.example.jobboard.repository.JobRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JobServiceImpl} using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private User employer;
    private JobListing jobListing;
    private JobRequestDto jobRequest;
    private JobResponseDto jobResponse;

    @BeforeEach
    void setUp() {
        employer = User.builder()
                .id(1L)
                .name("TechCorp HR")
                .email("employer@example.com")
                .role(Role.EMPLOYER)
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

        jobRequest = JobRequestDto.builder()
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("180000"))
                .build();

        jobResponse = JobResponseDto.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("180000"))
                .status("OPEN")
                .companyName("TechCorp HR")
                .postedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createJob — should create and return job response")
    void createJob_ValidRequest_ReturnsJobResponse() {
        when(userRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(employer));
        when(jobMapper.toEntity(any(JobRequestDto.class))).thenReturn(jobListing);
        when(jobRepository.save(any(JobListing.class))).thenReturn(jobListing);
        when(jobMapper.toDto(any(JobListing.class))).thenReturn(jobResponse);

        JobResponseDto result = jobService.createJob(jobRequest, "employer@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Senior Java Developer");
        verify(jobRepository).save(any(JobListing.class));
    }

    @Test
    @DisplayName("createJob — should throw when user not found")
    void createJob_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.createJob(jobRequest, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("closeJob — should set status to CLOSED")
    void closeJob_ValidOwner_ReturnsClosedJob() {
        JobResponseDto closedResponse = JobResponseDto.builder()
                .id(1L)
                .title("Senior Java Developer")
                .status("CLOSED")
                .build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobListing));
        when(jobRepository.save(any(JobListing.class))).thenReturn(jobListing);
        when(jobMapper.toDto(any(JobListing.class))).thenReturn(closedResponse);

        JobResponseDto result = jobService.closeJob(1L, "employer@example.com");

        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(jobRepository).save(any(JobListing.class));
    }

    @Test
    @DisplayName("closeJob — should throw UnauthorizedException for non-owner")
    void closeJob_NotOwner_ThrowsUnauthorized() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobListing));

        assertThatThrownBy(() -> jobService.closeJob(1L, "other@example.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("searchJobs — should return paginated results")
    void searchJobs_WithFilters_ReturnsPage() {
        Page<JobListing> jobPage = new PageImpl<>(List.of(jobListing));
        Pageable pageable = PageRequest.of(0, 10);

        when(jobRepository.searchJobs(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(jobPage);
        when(jobMapper.toDto(any(JobListing.class))).thenReturn(jobResponse);

        Page<JobResponseDto> result = jobService.searchJobs("Java", null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Senior Java Developer");
    }

    @Test
    @DisplayName("getJobById — should return job when found")
    void getJobById_Exists_ReturnsJob() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobListing));
        when(jobMapper.toDto(jobListing)).thenReturn(jobResponse);

        JobResponseDto result = jobService.getJobById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getJobById — should throw when not found")
    void getJobById_NotFound_ThrowsException() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
