package com.example.jobboard.service;

import com.example.jobboard.dto.request.ForgotPasswordRequestDto;
import com.example.jobboard.dto.request.LoginRequestDto;
import com.example.jobboard.dto.request.RegisterRequestDto;
import com.example.jobboard.dto.request.ResetPasswordRequestDto;
import com.example.jobboard.dto.response.ApiResponse;
import com.example.jobboard.dto.response.AuthResponseDto;

/**
 * Service interface for authentication operations including registration and login.
 */
public interface AuthService {

    /**
     * Registers a new user and returns an authentication response with JWT token.
     *
     * @param request the registration request containing user details
     * @return AuthResponseDto with JWT token and user info
     */
    AuthResponseDto register(RegisterRequestDto request);

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request containing credentials
     * @return AuthResponseDto with JWT token and user info
     */
    AuthResponseDto login(LoginRequestDto request);

    /**
     * Sends a password reset email.
     *
     * @param request the forgot password request with email
     */
    void forgotPassword(ForgotPasswordRequestDto request);

    /**
     * Resets the password using a reset token.
     *
     * @param request the reset password request with token and new password
     */
    void resetPassword(ResetPasswordRequestDto request);

    /**
     * Verifies a user's email using a verification token.
     *
     * @param token the email verification token
     */
    void verifyEmail(String token);

    /**
     * Resends the email verification link.
     *
     * @param email the user's email
     */
    void resendVerification(String email);
}
