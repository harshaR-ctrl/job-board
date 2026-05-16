package com.example.jobboard.controller;

import com.example.jobboard.dto.request.CandidateProfileRequestDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.CandidateProfileResponseDto;
import com.example.jobboard.service.CandidateService;
import com.example.jobboard.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for candidate profile management — view, update, and resume upload.
 */
@RestController
@RequestMapping("/api/candidates")
@Tag(name = "Candidates", description = "Candidate profile management endpoints")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    /**
     * Retrieves the authenticated candidate's profile.
     *
     * @return ApiResponse containing the candidate profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my profile", description = "Returns the authenticated candidate's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ApiResponse<CandidateProfileResponseDto>> getProfile() {
        String email = AppUtils.getCurrentUserEmail();
        CandidateProfileResponseDto response = candidateService.getProfile(email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Profile retrieved successfully", response));
    }

    /**
     * Updates the authenticated candidate's profile.
     *
     * @param request the profile update request
     * @return ApiResponse containing the updated profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update my profile", description = "Updates the authenticated candidate's profile information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<CandidateProfileResponseDto>> updateProfile(
            @Valid @RequestBody CandidateProfileRequestDto request) {
        String email = AppUtils.getCurrentUserEmail();
        CandidateProfileResponseDto response = candidateService.updateProfile(email, request);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Profile updated successfully", response));
    }

    /**
     * Uploads a resume file for the authenticated candidate.
     *
     * @param file the resume file (multipart)
     * @return ApiResponse containing the updated profile with resume URL
     */
    @PostMapping("/profile/resume")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload resume", description = "Uploads a resume file for the authenticated candidate. Max 5MB.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resume uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or file too large")
    })
    public ResponseEntity<ApiResponse<CandidateProfileResponseDto>> uploadResume(
            @RequestParam("file") MultipartFile file) {
        String email = AppUtils.getCurrentUserEmail();
        CandidateProfileResponseDto response = candidateService.uploadResume(email, file);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Resume uploaded successfully", response));
    }

    /**
     * Downloads the authenticated candidate's resume file.
     *
     * @return the resume file
     */
    @GetMapping("/profile/resume/download")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Download my resume", description = "Downloads the authenticated candidate's resume file")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resume downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No resume uploaded")
    })
    public ResponseEntity<Resource> downloadResume() {
        String email = AppUtils.getCurrentUserEmail();
        Resource resource = candidateService.downloadResume(email);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                .body(resource);
    }
}
