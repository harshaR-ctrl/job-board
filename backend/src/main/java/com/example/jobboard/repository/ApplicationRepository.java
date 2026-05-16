package com.example.jobboard.repository;

import com.example.jobboard.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Application entity operations.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Finds all applications submitted by a specific candidate.
     *
     * @param candidateId the candidate's user ID
     * @return list of applications
     */
    @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH j.employer WHERE a.candidate.id = :candidateId ORDER BY a.appliedAt DESC")
    List<Application> findByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Finds all applications for a specific job listing.
     *
     * @param jobId the job listing ID
     * @return list of applications
     */
    @Query("SELECT a FROM Application a JOIN FETCH a.candidate JOIN FETCH a.job j JOIN FETCH j.employer WHERE a.job.id = :jobId ORDER BY a.appliedAt DESC")
    List<Application> findByJobId(@Param("jobId") Long jobId);

    /**
     * Checks if a candidate has already applied to a specific job.
     *
     * @param candidateId the candidate's user ID
     * @param jobId       the job listing ID
     * @return true if an application already exists
     */
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
}
