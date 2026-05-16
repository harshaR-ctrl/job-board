package com.example.jobboard.repository;

import com.example.jobboard.entity.JobListing;
import com.example.jobboard.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for JobListing entity with specification-based search support.
 */
@Repository
public interface JobRepository extends JpaRepository<JobListing, Long>, JpaSpecificationExecutor<JobListing> {

    /**
     * Finds all job listings posted by a specific employer, paginated.
     *
     * @param employerId the employer's user ID
     * @param pageable   pagination parameters
     * @return a page of job listings
     */
    Page<JobListing> findByEmployerId(Long employerId, Pageable pageable);

    /**
     * Searches open job listings by title, location, and salary range.
     *
     * @param title     partial title match (case-insensitive)
     * @param location  partial location match (case-insensitive)
     * @param minSalary minimum salary filter
     * @param maxSalary maximum salary filter
     * @param status    job status filter
     * @param pageable  pagination parameters
     * @return a page of matching job listings
     */
    @Query("SELECT j FROM JobListing j LEFT JOIN FETCH j.employer WHERE " +
           "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:minSalary IS NULL OR j.salaryMin >= :minSalary) AND " +
           "(:maxSalary IS NULL OR j.salaryMax <= :maxSalary) AND " +
           "(:status IS NULL OR j.status = :status)")
    Page<JobListing> searchJobs(
            @Param("title") String title,
            @Param("location") String location,
            @Param("minSalary") java.math.BigDecimal minSalary,
            @Param("maxSalary") java.math.BigDecimal maxSalary,
            @Param("status") JobStatus status,
            Pageable pageable
    );
}
