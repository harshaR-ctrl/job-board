package com.example.jobboard.service.impl;

import com.example.jobboard.dto.request.ForgotPasswordRequestDto;
import com.example.jobboard.dto.request.LoginRequestDto;
import com.example.jobboard.dto.request.RegisterRequestDto;
import com.example.jobboard.dto.request.ResetPasswordRequestDto;
import com.example.jobboard.dto.response.AuthResponseDto;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.EmailVerificationToken;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.PasswordResetToken;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.Role;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.repository.CandidateProfileRepository;
import com.example.jobboard.repository.EmailVerificationTokenRepository;
import com.example.jobboard.repository.EmployerProfileRepository;
import com.example.jobboard.repository.PasswordResetTokenRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.security.JwtUtil;
import com.example.jobboard.service.AuthService;
import com.example.jobboard.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of {@link AuthService} handling user registration and login
 * with JWT token generation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository,
                           CandidateProfileRepository candidateProfileRepository,
                           EmployerProfileRepository employerProfileRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailVerificationTokenRepository emailVerificationTokenRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    /**
     * {@inheritDoc}
     * Creates a new user, associated profile (employer or candidate),
     * and returns a JWT token for immediate authentication.
     */
    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        user = userRepository.save(user);

        // Generate and send email verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .token(verificationToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        emailVerificationTokenRepository.save(emailToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);

        // Create associated profile based on role
        if (role == Role.EMPLOYER) {
            EmployerProfile profile = EmployerProfile.builder()
                    .user(user)
                    .companyName(request.getCompanyName() != null ? request.getCompanyName() : request.getName())
                    .website(request.getWebsite())
                    .description(request.getCompanyDescription())
                    .build();
            employerProfileRepository.save(profile);
        } else {
            CandidateProfile profile = CandidateProfile.builder()
                    .user(user)
                    .experienceYears(0)
                    .build();
            candidateProfileRepository.save(profile);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponseDto.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .name(user.getName())
                .verified(user.isVerified())
                .build();
    }

    /**
     * {@inheritDoc}
     * Authenticates the user via Spring Security's AuthenticationManager
     * and returns a JWT token upon success.
     */
    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getEmail()));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponseDto.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .name(user.getName())
                .verified(user.isVerified())
                .build();
    }

    /**
     * {@inheritDoc}
     * Generates a password reset token and sends an email with the reset link.
     * Silently succeeds even if the email is not found (to prevent email enumeration).
     */
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        });
    }

    /**
     * {@inheritDoc}
     * Validates the reset token and updates the user's password.
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Reset token has already been used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    /**
     * {@inheritDoc}
     * Verifies the user's email using the provided token.
     */
    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (verificationToken.isUsed()) {
            throw new IllegalArgumentException("Verification token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    /**
     * {@inheritDoc}
     * Resends the email verification link to the user.
     */
    @Override
    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        emailVerificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);
    }
}
