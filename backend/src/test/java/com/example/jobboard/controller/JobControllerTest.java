package com.example.jobboard.controller;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.JobResponseDto;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.JwtUtil;
import com.example.jobboard.security.SecurityConfig;
import com.example.jobboard.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link JobController} using @WebMvcTest with role-based security mocking.
 */
@Import(SecurityConfig.class)
@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private JobResponseDto sampleJobResponse() {
        return JobResponseDto.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("180000"))
                .status("OPEN")
                .companyName("TechCorp")
                .postedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/jobs — EMPLOYER should create job successfully")
    @WithMockUser(roles = "EMPLOYER")
    void createJob_AsEmployer_ReturnsCreated() throws Exception {
        JobRequestDto request = JobRequestDto.builder()
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("180000"))
                .build();

        when(jobService.createJob(any(JobRequestDto.class), anyString())).thenReturn(sampleJobResponse());

        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Senior Java Developer"));
    }

    @Test
    @DisplayName("POST /api/jobs — CANDIDATE should be forbidden")
    @WithMockUser(roles = "CANDIDATE")
    void createJob_AsCandidate_ReturnsForbidden() throws Exception {
        JobRequestDto request = JobRequestDto.builder()
                .title("Senior Java Developer")
                .description("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .build();

        mockMvc.perform(post("/api/jobs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/jobs — public search should return results")
    @WithMockUser
    void searchJobs_PublicEndpoint_ReturnsOk() throws Exception {
        Page<JobResponseDto> page = new PageImpl<>(List.of(sampleJobResponse()));
        when(jobService.searchJobs(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/jobs")
                        .param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Senior Java Developer"));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} — should return job detail")
    @WithMockUser
    void getJobById_ExistingJob_ReturnsOk() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(sampleJobResponse());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/close — EMPLOYER should close job")
    @WithMockUser(roles = "EMPLOYER")
    void closeJob_AsEmployer_ReturnsOk() throws Exception {
        JobResponseDto closedJob = sampleJobResponse();
        closedJob.setStatus("CLOSED");
        when(jobService.closeJob(eq(1L), anyString())).thenReturn(closedJob);

        mockMvc.perform(patch("/api/jobs/1/close")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
