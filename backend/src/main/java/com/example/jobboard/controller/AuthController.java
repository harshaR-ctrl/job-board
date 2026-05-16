package com.example.jobboard.controller;

import com.example.jobboard.dto.request.ForgotPasswordRequestDto;
import com.example.jobboard.dto.request.LoginRequestDto;
import com.example.jobboard.dto.request.RegisterRequestDto;
import com.example.jobboard.dto.request.ResetPasswordRequestDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.AuthResponseDto;
import com.example.jobboard.service.AuthService;
import com.example.jobboard.util.AppUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints — registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user (EMPLOYER or CANDIDATE) and returns a JWT token.
     *
     * @param request the registration request with user details
     * @return ApiResponse containing the auth token and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token for immediate authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or email already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppUtils.buildSuccessResponse("User registered successfully", response));
    }

    /**
     * Authenticates a user with email and password, returning a JWT token.
     *
     * @param request the login request with credentials
     * @return ApiResponse containing the auth token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Login successful", response));
    }

    /**
     * Sends a password reset email if the email is registered.
     *
     * @param request the forgot password request with email
     * @return ApiResponse indicating success
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends a password reset email if the email is registered")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset email sent if email exists")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("If the email exists, a reset link has been sent", null));
    }

    /**
     * Resets the password using a valid reset token.
     *
     * @param request the reset password request with token and new password
     * @return ApiResponse indicating success
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets the password using a valid reset token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Password reset successfully", null));
    }

    /**
     * Verifies a user's email address using a verification token.
     *
     * @param token the email verification token
     * @return ApiResponse indicating success
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verifies a user's email using a verification token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("Email verified successfully", null));
    }

    /**
     * Resends the email verification link.
     *
     * @param request the forgot password request with email
     * @return ApiResponse indicating success
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification", description = "Resends the email verification link")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification email sent if email exists")
    })
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(AppUtils.buildSuccessResponse("If the account exists and is not verified, a verification email has been sent", null));
    }
}
