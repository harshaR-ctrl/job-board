-- ============================================================
-- Job Board Database Schema & Seed Data
-- ============================================================

-- Drop tables if they exist (order matters for FK constraints)
DROP TABLE IF EXISTS email_verification_tokens CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS applications CASCADE;
DROP TABLE IF EXISTS job_listings CASCADE;
DROP TABLE IF EXISTS candidate_profiles CASCADE;
DROP TABLE IF EXISTS employer_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ──────────────────────────────────────────────
-- Users Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL CHECK (role IN ('EMPLOYER', 'CANDIDATE')),
    verified    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ──────────────────────────────────────────────
-- Employer Profiles Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employer_profiles (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name  VARCHAR(200) NOT NULL,
    website       VARCHAR(500),
    description   TEXT
);

-- ──────────────────────────────────────────────
-- Candidate Profiles Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS candidate_profiles (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone            VARCHAR(20),
    resume_url       VARCHAR(500),
    skills           TEXT,
    experience_years INTEGER      DEFAULT 0
);

-- ──────────────────────────────────────────────
-- Job Listings Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS job_listings (
    id          BIGSERIAL PRIMARY KEY,
    employer_id BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT         NOT NULL,
    location    VARCHAR(200) NOT NULL,
    salary_min  NUMERIC(12,2),
    salary_max  NUMERIC(12,2),
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ──────────────────────────────────────────────
-- Applications Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS applications (
    id           BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id       BIGINT      NOT NULL REFERENCES job_listings(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'APPLIED' CHECK (status IN ('APPLIED', 'SHORTLISTED', 'REJECTED', 'HIRED', 'WITHDRAWN')),
    applied_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(candidate_id, job_id)
);

-- ──────────────────────────────────────────────
-- Email Verification Tokens Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ──────────────────────────────────────────────
-- Password Reset Tokens Table
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Passwords are BCrypt hash of "password123"
-- $2a$10$EqKcp1WFKAr4GQKV8MzKfuQ4MvXi3xBNXOJf9LzYgqWtF6MfHyKO

INSERT INTO users (name, email, password, role, verified) VALUES
    ('TechCorp HR',    'employer1@example.com', '$2a$10$EqKcp1WFKAr4GQKV8MzKfuQ4MvXi3xBNXOJf9LzYgqWtF6MfHyKO', 'EMPLOYER', TRUE),
    ('StartupInc HR',  'employer2@example.com', '$2a$10$EqKcp1WFKAr4GQKV8MzKfuQ4MvXi3xBNXOJf9LzYgqWtF6MfHyKO', 'EMPLOYER', TRUE),
    ('Alice Johnson',  'candidate1@example.com','$2a$10$EqKcp1WFKAr4GQKV8MzKfuQ4MvXi3xBNXOJf9LzYgqWtF6MfHyKO', 'CANDIDATE', TRUE),
    ('Bob Williams',   'candidate2@example.com','$2a$10$EqKcp1WFKAr4GQKV8MzKfuQ4MvXi3xBNXOJf9LzYgqWtF6MfHyKO', 'CANDIDATE', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO employer_profiles (user_id, company_name, website, description) VALUES
    ((SELECT id FROM users WHERE email = 'employer1@example.com'), 'TechCorp Solutions', 'https://techcorp.example.com', 'Leading technology solutions company specializing in enterprise software and cloud infrastructure.'),
    ((SELECT id FROM users WHERE email = 'employer2@example.com'), 'StartupInc',          'https://startupinc.example.com', 'Fast-growing startup disrupting the fintech space with innovative payment solutions.')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO candidate_profiles (user_id, phone, resume_url, skills, experience_years) VALUES
    ((SELECT id FROM users WHERE email = 'candidate1@example.com'), '+1-555-0101', NULL, 'Java, Spring Boot, React, PostgreSQL, Docker, AWS', 4),
    ((SELECT id FROM users WHERE email = 'candidate2@example.com'), '+1-555-0202', NULL, 'Python, Django, JavaScript, MongoDB, Kubernetes', 2)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO job_listings (employer_id, title, description, location, salary_min, salary_max, status) VALUES
    ((SELECT id FROM users WHERE email = 'employer1@example.com'),
     'Senior Java Developer',
     'We are looking for an experienced Java developer to join our backend engineering team. You will design and implement high-performance RESTful APIs, work with microservices architecture, and mentor junior developers. Strong experience with Spring Boot and cloud platforms is required.',
     'San Francisco, CA',
     120000.00, 180000.00, 'OPEN'),

    ((SELECT id FROM users WHERE email = 'employer1@example.com'),
     'Frontend React Engineer',
     'Join our frontend team to build beautiful and responsive web applications using React 18, TypeScript, and modern CSS frameworks. You will collaborate closely with UX designers and backend engineers to deliver exceptional user experiences.',
     'Remote',
     100000.00, 150000.00, 'OPEN'),

    ((SELECT id FROM users WHERE email = 'employer2@example.com'),
     'Full Stack Developer',
     'StartupInc is hiring a versatile full-stack developer who can work across the entire technology stack. You will build features end-to-end using Node.js, React, and PostgreSQL while contributing to architecture decisions in a fast-paced startup environment.',
     'New York, NY',
     90000.00, 140000.00, 'OPEN'),

    ((SELECT id FROM users WHERE email = 'employer2@example.com'),
     'DevOps Engineer',
     'Help us scale our infrastructure by designing CI/CD pipelines, managing Kubernetes clusters, and implementing observability solutions. Experience with AWS, Terraform, and Docker is essential. You will be part of a small but impactful platform engineering team.',
     'Austin, TX',
     110000.00, 160000.00, 'OPEN')
ON CONFLICT DO NOTHING;
