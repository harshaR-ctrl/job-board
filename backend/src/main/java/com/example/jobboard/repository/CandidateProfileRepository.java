package com.example.jobboard.repository;

import com.example.jobboard.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CandidateProfile entity operations.
 */
@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    /**
     * Finds a candidate profile by the associated user ID.
     *
     * @param userId the user ID
     * @return an Optional containing the profile if found
     */
    Optional<CandidateProfile> findByUserId(Long userId);
}
