package com.example.jobboard.controller;

import com.example.jobboard.dto.request.EmployerProfileRequestDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.EmployerProfileResponseDto;
import com.example.jobboard.service.EmployerService;
import com.example.jobboard.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employers")
@Tag(name = "Employers", description = "Employer profile management endpoints")
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get my company profile", description = "Returns the authenticated employer's company profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ApiResponse<EmployerProfileResponseDto>> getProfile() {
        String email = AppUtils.getCurrentUserEmail();
        EmployerProfileResponseDto response = employerService.getProfile(email);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update my company profile", description = "Updates the authenticated employer's company profile information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<EmployerProfileResponseDto>> updateProfile(
            @Valid @RequestBody EmployerProfileRequestDto request) {
        String email = AppUtils.getCurrentUserEmail();
        EmployerProfileResponseDto response = employerService.updateProfile(email, request);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Profile updated successfully", response));
    }
}
