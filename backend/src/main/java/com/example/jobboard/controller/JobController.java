package com.example.jobboard.controller;

import com.example.jobboard.dto.request.JobRequestDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.JobResponseDto;
import com.example.jobboard.service.JobService;
import com.example.jobboard.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for job listing management — CRUD and search.
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job listing management and search endpoints")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Creates a new job listing. Only accessible by EMPLOYER role.
     *
     * @param request the job creation request
     * @return ApiResponse containing the created job
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create a job listing", description = "Creates a new job listing. Employer only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Job created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — not an employer")
    })
    public ResponseEntity<ApiResponse<JobResponseDto>> createJob(@Valid @RequestBody JobRequestDto request) {
        String email = AppUtils.getCurrentUserEmail();
        JobResponseDto response = jobService.createJob(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppUtils.buildSuccessResponse("Job created successfully", response));
    }

    /**
     * Updates an existing job listing. Only the owning employer can update.
     *
     * @param id      the job listing ID
     * @param request the update request
     * @return ApiResponse containing the updated job
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update a job listing", description = "Updates an existing job listing. Owner employer only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner of this listing")
    })
    public ResponseEntity<ApiResponse<JobResponseDto>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDto request) {
        String email = AppUtils.getCurrentUserEmail();
        JobResponseDto response = jobService.updateJob(id, request, email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Job updated successfully", response));
    }

    /**
     * Closes a job listing. Only the owning employer can close.
     *
     * @param id the job listing ID
     * @return ApiResponse containing the closed job
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Close a job listing", description = "Sets the job status to CLOSED. Owner employer only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner of this listing")
    })
    public ResponseEntity<ApiResponse<JobResponseDto>> closeJob(@PathVariable Long id) {
        String email = AppUtils.getCurrentUserEmail();
        JobResponseDto response = jobService.closeJob(id, email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Job closed successfully", response));
    }

    /**
     * Retrieves all job listings posted by the authenticated employer (paginated).
     *
     * @param page the page number (0-indexed)
     * @param size the page size
     * @return ApiResponse containing a page of job listings
     */
    @GetMapping("/employer/mine")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get my job listings", description = "Returns paginated list of jobs posted by the authenticated employer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jobs retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<JobResponseDto>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = AppUtils.getCurrentUserEmail();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponseDto> jobs = jobService.getEmployerJobs(email, pageable);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Jobs retrieved successfully", jobs));
    }

    /**
     * Public search endpoint for job listings with optional filters.
     *
     * @param title     partial title match
     * @param location  partial location match
     * @param minSalary minimum salary filter
     * @param maxSalary maximum salary filter
     * @param page      the page number
     * @param size      the page size
     * @return ApiResponse containing a page of matching jobs
     */
    @GetMapping
    @Operation(summary = "Search job listings", description = "Public endpoint to search and filter open job listings")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned")
    })
    public ResponseEntity<ApiResponse<Page<JobResponseDto>>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponseDto> jobs = jobService.searchJobs(title, location, minSalary, maxSalary, pageable);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Jobs retrieved successfully", jobs));
    }

    /**
     * Retrieves a single job listing by ID. Public endpoint.
     *
     * @param id the job listing ID
     * @return ApiResponse containing the job details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get job details", description = "Public endpoint to retrieve a specific job listing by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<ApiResponse<JobResponseDto>> getJobById(@PathVariable Long id) {
        JobResponseDto response = jobService.getJobById(id);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Job retrieved successfully", response));
    }
}
