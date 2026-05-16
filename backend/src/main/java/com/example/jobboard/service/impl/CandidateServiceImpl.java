package com.example.jobboard.service.impl;

import com.example.jobboard.dto.request.CandidateProfileRequestDto;
import com.example.jobboard.dto.response.CandidateProfileResponseDto;
import com.example.jobboard.entity.CandidateProfile;
import com.example.jobboard.entity.User;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.mapper.CandidateMapper;
import com.example.jobboard.repository.CandidateProfileRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.CandidateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of {@link CandidateService} for managing candidate profiles
 * and resume uploads.
 */
@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public CandidateServiceImpl(CandidateProfileRepository candidateProfileRepository,
                                UserRepository userRepository,
                                CandidateMapper candidateMapper) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
        this.candidateMapper = candidateMapper;
    }

    /**
     * {@inheritDoc}
     * Retrieves the candidate profile. If no profile exists yet, creates one.
     */
    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile", "userId", user.getId()));

        return candidateMapper.toDto(profile);
    }

    /**
     * {@inheritDoc}
     * Updates the candidate profile with the provided fields.
     * Creates a profile if one doesn't exist yet.
     */
    @Override
    @Transactional
    public CandidateProfileResponseDto updateProfile(String email, CandidateProfileRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CandidateProfile newProfile = CandidateProfile.builder()
                            .user(user)
                            .experienceYears(0)
                            .build();
                    return candidateProfileRepository.save(newProfile);
                });

        candidateMapper.updateEntityFromDto(request, profile);
        CandidateProfile updatedProfile = candidateProfileRepository.save(profile);
        return candidateMapper.toDto(updatedProfile);
    }

    /**
     * {@inheritDoc}
     * Stores the uploaded resume file on disk and saves the filename in the database.
     * Generates a unique filename to prevent collisions.
     */
    @Override
    @Transactional
    public CandidateProfileResponseDto uploadResume(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CandidateProfile newProfile = CandidateProfile.builder()
                            .user(user)
                            .experienceYears(0)
                            .build();
                    return candidateProfileRepository.save(newProfile);
                });

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            profile.setResumeUrl(uniqueFilename);
            CandidateProfile updatedProfile = candidateProfileRepository.save(profile);
            return candidateMapper.toDto(updatedProfile);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resume file: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Serves the candidate's uploaded resume file.
     */
    @Override
    @Transactional(readOnly = true)
    public Resource downloadResume(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile", "userId", user.getId()));

        if (profile.getResumeUrl() == null || profile.getResumeUrl().isBlank()) {
            throw new ResourceNotFoundException("Resume", "userId", user.getId());
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(profile.getResumeUrl()).normalize();
            return new InputStreamResource(new FileInputStream(filePath.toFile()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resume file: " + e.getMessage(), e);
        }
    }
}
