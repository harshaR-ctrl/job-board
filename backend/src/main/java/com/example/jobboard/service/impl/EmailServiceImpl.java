package com.example.jobboard.service.impl;

import com.example.jobboard.enums.ApplicationStatus;
import com.example.jobboard.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation of {@link EmailService} that sends HTML email notifications
 * using Thymeleaf templates and Spring JavaMailSender.
 * All email sending is asynchronous to avoid blocking request threads.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * {@inheritDoc}
     * Sends an HTML email notification based on the application status.
     * Uses the appropriate Thymeleaf template for each status.
     * Runs asynchronously to prevent blocking the calling thread.
     */
    @Override
    @Async
    public void sendStatusEmail(String toEmail, String candidateName, String jobTitle,
                                String companyName, ApplicationStatus status) {
        try {
            String templateName = getTemplateName(status);
            String subject = getSubject(status, jobTitle);

            Context context = new Context();
            context.setVariable("candidateName", candidateName);
            context.setVariable("jobTitle", jobTitle);
            context.setVariable("companyName", companyName);
            context.setVariable("status", status.name());

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully to {} for status {}", toEmail, status);

        } catch (MessagingException e) {
            logger.error("Failed to send email to {} for status {}: {}", toEmail, status, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * Sends a password reset email with a reset token link.
     */
    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        try {
            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            String htmlContent = templateEngine.process("email-password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - JobBoard");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * Sends an email verification link.
     */
    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String name, String token) {
        try {
            String verifyLink = "http://localhost:5173/verify-email?token=" + token;

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verifyLink", verifyLink);

            String htmlContent = templateEngine.process("email-verify", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - JobBoard");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Verification email sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Maps an ApplicationStatus to the corresponding Thymeleaf template name.
     *
     * @param status the application status
     * @return the template name (without extension)
     */
    private String getTemplateName(ApplicationStatus status) {
        return switch (status) {
            case APPLIED -> "email-applied";
            case SHORTLISTED -> "email-shortlisted";
            case REJECTED -> "email-rejected";
            case HIRED -> "email-hired";
            default -> "email-applied";
        };
    }

    /**
     * Generates an email subject line based on the application status.
     *
     * @param status   the application status
     * @param jobTitle the job title for context
     * @return the email subject line
     */
    private String getSubject(ApplicationStatus status, String jobTitle) {
        return switch (status) {
            case APPLIED -> "Application Received - " + jobTitle;
            case SHORTLISTED -> "You've Been Shortlisted! - " + jobTitle;
            case REJECTED -> "Application Update - " + jobTitle;
            case HIRED -> "Congratulations! You're Hired - " + jobTitle;
            default -> "Application Update - " + jobTitle;
        };
    }
}
