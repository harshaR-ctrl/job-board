package com.example.jobboard.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Extended profile for employer users — stores company details.
 */
@Entity
@Table(name = "employer_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(length = 500)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;
}
