package com.example.jobboard.service;

import com.example.jobboard.dto.request.EmployerProfileRequestDto;
import com.example.jobboard.dto.response.EmployerProfileResponseDto;

public interface EmployerService {

    EmployerProfileResponseDto getProfile(String email);

    EmployerProfileResponseDto updateProfile(String email, EmployerProfileRequestDto request);
}
