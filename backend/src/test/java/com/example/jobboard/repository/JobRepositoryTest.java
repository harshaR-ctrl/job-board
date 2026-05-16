package com.example.jobboard.repository;

import com.example.jobboard.entity.JobListing;
import com.example.jobboard.entity.User;
import com.example.jobboard.enums.JobStatus;
import com.example.jobboard.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link JobRepository} using @DataJpaTest with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    private User employer;

    @BeforeEach
    void setUp() {
        employer = User.builder()
                .name("Test Employer")
                .email("test-employer@example.com")
                .password("password123")
                .role(Role.EMPLOYER)
                .build();
        employer = entityManager.persistAndFlush(employer);
    }

    @Test
    @DisplayName("findByEmployerId — should return jobs for a specific employer")
    void findByEmployerId_ReturnsEmployerJobs() {
        JobListing job1 = createJob("Java Developer", "Remote", JobStatus.OPEN);
        JobListing job2 = createJob("Python Developer", "NYC", JobStatus.OPEN);
        entityManager.persistAndFlush(job1);
        entityManager.persistAndFlush(job2);

        Page<JobListing> result = jobRepository.findByEmployerId(employer.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("searchJobs — should filter by title")
    void searchJobs_ByTitle_ReturnsMatchingJobs() {
        entityManager.persistAndFlush(createJob("Java Developer", "Remote", JobStatus.OPEN));
        entityManager.persistAndFlush(createJob("Python Developer", "NYC", JobStatus.OPEN));

        Page<JobListing> result = jobRepository.searchJobs(
                "Java", null, null, null, JobStatus.OPEN, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Java");
    }

    @Test
    @DisplayName("searchJobs — should filter by location")
    void searchJobs_ByLocation_ReturnsMatchingJobs() {
        entityManager.persistAndFlush(createJob("Dev 1", "Remote", JobStatus.OPEN));
        entityManager.persistAndFlush(createJob("Dev 2", "New York", JobStatus.OPEN));

        Page<JobListing> result = jobRepository.searchJobs(
                null, "Remote", null, null, JobStatus.OPEN, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocation()).isEqualTo("Remote");
    }

    @Test
    @DisplayName("searchJobs — should exclude CLOSED jobs")
    void searchJobs_ExcludesClosedJobs() {
        entityManager.persistAndFlush(createJob("Open Job", "Remote", JobStatus.OPEN));
        entityManager.persistAndFlush(createJob("Closed Job", "Remote", JobStatus.CLOSED));

        Page<JobListing> result = jobRepository.searchJobs(
                null, null, null, null, JobStatus.OPEN, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Open Job");
    }

    @Test
    @DisplayName("searchJobs — should filter by salary range")
    void searchJobs_BySalaryRange_ReturnsMatchingJobs() {
        JobListing highPay = createJob("Senior Dev", "Remote", JobStatus.OPEN);
        highPay.setSalaryMin(new BigDecimal("150000"));
        highPay.setSalaryMax(new BigDecimal("200000"));
        entityManager.persistAndFlush(highPay);

        JobListing lowPay = createJob("Junior Dev", "Remote", JobStatus.OPEN);
        lowPay.setSalaryMin(new BigDecimal("50000"));
        lowPay.setSalaryMax(new BigDecimal("80000"));
        entityManager.persistAndFlush(lowPay);

        Page<JobListing> result = jobRepository.searchJobs(
                null, null, new BigDecimal("100000"), null, JobStatus.OPEN, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Senior Dev");
    }

    private JobListing createJob(String title, String location, JobStatus status) {
        return JobListing.builder()
                .employer(employer)
                .title(title)
                .description("Job description for " + title)
                .location(location)
                .salaryMin(new BigDecimal("80000"))
                .salaryMax(new BigDecimal("120000"))
                .status(status)
                .build();
    }
}
