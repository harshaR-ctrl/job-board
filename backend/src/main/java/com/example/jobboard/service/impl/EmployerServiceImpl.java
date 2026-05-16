package com.example.jobboard.service.impl;

import com.example.jobboard.dto.request.EmployerProfileRequestDto;
import com.example.jobboard.dto.response.EmployerProfileResponseDto;
import com.example.jobboard.entity.EmployerProfile;
import com.example.jobboard.entity.User;
import com.example.jobboard.exception.ResourceNotFoundException;
import com.example.jobboard.mapper.EmployerMapper;
import com.example.jobboard.repository.EmployerProfileRepository;
import com.example.jobboard.repository.UserRepository;
import com.example.jobboard.service.EmployerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerServiceImpl implements EmployerService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final EmployerMapper employerMapper;

    public EmployerServiceImpl(EmployerProfileRepository employerProfileRepository,
                               UserRepository userRepository,
                               EmployerMapper employerMapper) {
        this.employerProfileRepository = employerProfileRepository;
        this.userRepository = userRepository;
        this.employerMapper = employerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployerProfileResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("EmployerProfile", "userId", user.getId()));

        return employerMapper.toDto(profile);
    }

    @Override
    @Transactional
    public EmployerProfileResponseDto updateProfile(String email, EmployerProfileRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("EmployerProfile", "userId", user.getId()));

        employerMapper.updateEntityFromDto(request, profile);
        EmployerProfile updatedProfile = employerProfileRepository.save(profile);
        return employerMapper.toDto(updatedProfile);
    }
}
