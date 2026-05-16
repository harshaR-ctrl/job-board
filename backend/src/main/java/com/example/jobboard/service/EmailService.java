package com.example.jobboard.service;

import com.example.jobboard.enums.ApplicationStatus;

/**
 * Service interface for sending email notifications on application status changes.
 */
public interface EmailService {

    /**
     * Sends an email notification to the candidate based on the application status.
     *
     * @param toEmail       the candidate's email address
     * @param candidateName the candidate's full name
     * @param jobTitle      the title of the job applied to
     * @param companyName   the name of the hiring company
     * @param status        the new application status
     */
    void sendStatusEmail(String toEmail, String candidateName, String jobTitle, String companyName, ApplicationStatus status);

    /**
     * Sends a password reset email with a reset token link.
     *
     * @param toEmail the recipient's email address
     * @param name    the recipient's name
     * @param token   the password reset token
     */
    void sendPasswordResetEmail(String toEmail, String name, String token);

    /**
     * Sends an email verification link.
     *
     * @param toEmail the recipient's email address
     * @param name    the recipient's name
     * @param token   the email verification token
     */
    void sendVerificationEmail(String toEmail, String name, String token);
}
