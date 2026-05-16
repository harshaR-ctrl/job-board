package com.example.jobboard.controller;

import com.example.jobboard.dto.request.ApplicationStatusUpdateDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.ApplicationResponseDto;
import com.example.jobboard.service.ApplicationService;
import com.example.jobboard.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for job application management — apply, track, and update status.
 */
@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Job application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Submits a new application for a job listing. CANDIDATE only.
     *
     * @param jobId the job listing ID to apply to
     * @return ApiResponse containing the created application
     */
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply to a job", description = "Submits a new application for the specified job listing. Candidate only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate application — already applied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> applyToJob(@PathVariable Long jobId) {
        String email = AppUtils.getCurrentUserEmail();
        ApplicationResponseDto response = applicationService.applyToJob(jobId, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppUtils.buildSuccessResponse("Application submitted successfully", response));
    }

    /**
     * Retrieves all applications submitted by the authenticated candidate.
     *
     * @return ApiResponse containing the list of applications
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my applications", description = "Returns all applications submitted by the authenticated candidate")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Applications retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getMyApplications() {
        String email = AppUtils.getCurrentUserEmail();
        List<ApplicationResponseDto> applications = applicationService.getCandidateApplications(email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Applications retrieved successfully", applications));
    }

    /**
     * Retrieves all applications for a specific job listing. EMPLOYER only (owner).
     *
     * @param jobId the job listing ID
     * @return ApiResponse containing the list of applicants
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get applicants for a job", description = "Returns all applications for a specific job listing. Owner employer only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Applicants retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the owner of this listing"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getApplicationsForJob(@PathVariable Long jobId) {
        String email = AppUtils.getCurrentUserEmail();
        List<ApplicationResponseDto> applications = applicationService.getApplicationsForJob(jobId, email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Applicants retrieved successfully", applications));
    }

    /**
     * Updates the status of an application. EMPLOYER only (owner of associated job).
     * Triggers an email notification to the candidate.
     *
     * @param id      the application ID
     * @param request the status update request
     * @return ApiResponse containing the updated application
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update application status", description = "Updates the status of an application (SHORTLISTED, REJECTED, HIRED). Triggers email notification.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized to update this application")
    })
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateDto request) {
        String email = AppUtils.getCurrentUserEmail();
        ApplicationResponseDto response = applicationService.updateApplicationStatus(id, request, email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Application status updated successfully", response));
    }

    /**
     * Withdraws an application. CANDIDATE only — can only withdraw if status is APPLIED.
     *
     * @param id the application ID
     * @return ApiResponse containing the updated application
     */
    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw application", description = "Withdraws an application. Only possible if status is APPLIED.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Application withdrawn successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Application cannot be withdrawn"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> withdrawApplication(@PathVariable Long id) {
        String email = AppUtils.getCurrentUserEmail();
        ApplicationResponseDto response = applicationService.withdrawApplication(id, email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Application withdrawn successfully", response));
    }

    /**
     * Downloads the resume of a candidate for a specific application. EMPLOYER only.
     *
     * @param id the application ID
     * @return the resume file
     */
    @GetMapping("/{id}/resume")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Download candidate resume", description = "Downloads the resume of a candidate for a specific application. Employer must own the job.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resume downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Application or resume not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorized")
    })
    public ResponseEntity<Resource> downloadCandidateResume(@PathVariable Long id) {
        String email = AppUtils.getCurrentUserEmail();
        Resource resource = applicationService.getCandidateResume(id, email);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                .body(resource);
    }
}
