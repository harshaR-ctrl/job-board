# 💼 Job Board & Application Tracking Platform

A production-grade full-stack job board application built with **Spring Boot 3**, **React 18**, and **PostgreSQL**. Features JWT authentication, role-based access control, email notifications, Swagger API documentation, and Docker deployment.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Local Setup with Docker](#local-setup-with-docker)
- [Running Without Docker](#running-without-docker)
- [Running Tests](#running-tests)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Deployment](#deployment)
- [Project Structure](#project-structure)

---

## ✨ Features

- **Authentication & Authorization** — JWT-based stateless auth with role-based access (Employer / Candidate)
- **Job Listings** — CRUD operations for employers, public search with filters for candidates
- **Application Tracking** — Candidates apply to jobs; employers manage applicant pipeline (Applied → Shortlisted → Hired / Rejected)
- **Email Notifications** — Automated HTML emails on application status changes via Thymeleaf templates
- **Candidate Profiles** — Resume upload, skills & experience management
- **Swagger UI** — Interactive API documentation at `/swagger-ui.html`
- **Docker Ready** — One-command deployment with `docker-compose`

---

## 🛠️ Tech Stack

| Layer      | Technology                                                       |
| ---------- | ---------------------------------------------------------------- |
| Backend    | Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA      |
| Auth       | JWT (jjwt 0.11.5), stateless sessions                           |
| Database   | PostgreSQL 15, Hibernate, data.sql seed                          |
| Email      | Spring Mail + Thymeleaf HTML templates                           |
| API Docs   | SpringDoc OpenAPI 3 (Swagger UI)                                 |
| Frontend   | React 18, Vite, React Router v6, Tailwind CSS, shadcn/ui        |
| State Mgmt | TanStack Query, React Context API                                |
| Forms      | React Hook Form + Zod validation                                 |
| HTTP       | Axios with interceptors                                          |
| DevOps     | Docker, Docker Compose, Nginx                                    |

---

## 📦 Prerequisites

- **Docker** >= 20.x & **Docker Compose** >= 2.x (recommended)
- OR for local development:
  - Java 17 (JDK)
  - Maven 3.9+
  - Node.js 20+
  - PostgreSQL 15+

---

## 🔐 Environment Variables

Copy `.env.example` to `.env` and fill in all values:

```bash
cp .env.example .env
```

| Variable        | Description                              | Example                                          |
| --------------- | ---------------------------------------- | ------------------------------------------------ |
| `DB_URL`        | PostgreSQL JDBC connection URL           | `jdbc:postgresql://localhost:5432/jobboard`       |
| `DB_USERNAME`   | PostgreSQL username                      | `postgres`                                       |
| `DB_PASSWORD`   | PostgreSQL password                      | `secret`                                         |
| `JWT_SECRET`    | JWT signing secret (min 32 characters)   | `my-super-secret-jwt-key-32chars!`               |
| `MAIL_HOST`     | SMTP server hostname                     | `smtp.gmail.com`                                 |
| `MAIL_PORT`     | SMTP server port                         | `587`                                            |
| `MAIL_USERNAME` | SMTP sender email                        | `you@gmail.com`                                  |
| `MAIL_PASSWORD` | SMTP password / app password             | `abcd efgh ijkl mnop`                            |
| `VITE_API_URL`  | Backend API URL for the React frontend   | `http://localhost:8080/api`                       |

---

## 🐳 Local Setup with Docker

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/job-board.git
   cd job-board
   ```

2. **Create environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

3. **Build and start all services**
   ```bash
   docker compose up -d --build
   ```

4. **Check status**
   ```bash
   docker compose ps
   ```

5. **View backend logs**
   ```bash
   docker compose logs backend --tail 40
   ```

6. **Access the application**
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Backend API: [http://localhost:8080/api](http://localhost:8080/api)
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

7. **Preloaded seed users**

   | Email                    | Password     | Role       |
   | ------------------------ | ------------ | ---------- |
   | `employer1@example.com`  | `password123`| EMPLOYER   |
   | `employer2@example.com`  | `password123`| EMPLOYER   |
   | `candidate1@example.com` | `password123`| CANDIDATE  |
   | `candidate2@example.com` | `password123`| CANDIDATE  |

8. **Stop all services**
   ```bash
   docker compose down
   ```

---

## 💻 Running Without Docker

### Backend

```bash
cd backend

# Set environment variables or create application-local.properties
export DB_URL=jdbc:postgresql://localhost:5432/jobboard
export DB_USERNAME=postgres
export DB_PASSWORD=secret
export JWT_SECRET=your-jwt-secret-min-32-characters
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password

mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 🧪 Running Tests

```bash
cd backend
mvn test
```

Test suites include:

- **AuthControllerTest** — registration/login flows, validation failures
- **JobControllerTest** — CRUD operations with role-based mocking
- **JobServiceImplTest** — unit tests with Mockito
- **ApplicationServiceImplTest** — apply, duplicate check, status updates
- **JobRepositoryTest** — `@DataJpaTest` with H2 in-memory DB

---

## 📖 API Documentation (Swagger)

Once the backend is running, visit:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🚀 Deployment

### Deploying Frontend to Vercel

1. **Install Vercel CLI**
   ```bash
   npm i -g vercel
   ```

2. **Navigate to the frontend directory**
   ```bash
   cd frontend
   ```

3. **Deploy**
   ```bash
   vercel --prod
   ```

4. **Set environment variables in Vercel Dashboard**
   - Go to your project settings → Environment Variables
   - Add `VITE_API_URL` pointing to your deployed backend URL

### Deploying Backend

For the backend, deploy to any cloud platform that supports Docker:

- **Railway**: Connect your GitHub repo, set environment variables, deploy
- **Render**: Create a new Web Service from your Docker image
- **AWS ECS / GCP Cloud Run**: Push Docker image to registry and deploy

### Production Checklist

- [ ] Use a strong, unique `JWT_SECRET` (min 256-bit)
- [ ] Enable HTTPS on all services
- [ ] Configure CORS for your production frontend domain
- [ ] Set up a managed PostgreSQL instance
- [ ] Use an SMTP relay service (SendGrid, AWS SES) for emails
- [ ] Set `spring.jpa.show-sql=false` in production
- [ ] Configure proper logging and monitoring

---

## 🗂️ Project Structure

```
job-board/
├── backend/
│   ├── src/main/java/com/example/jobboard/
│   │   ├── controller/       # REST API endpoints
│   │   ├── service/          # Business logic interfaces
│   │   │   └── impl/         # Service implementations
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── entity/           # JPA entity classes
│   │   ├── dto/              # Request/Response DTOs
│   │   ├── config/           # App configuration (Swagger, CORS, Mail)
│   │   ├── security/         # JWT, Security filters, config
│   │   ├── exception/        # Global exception handling
│   │   ├── enums/            # Role, ApplicationStatus, JobStatus
│   │   ├── mapper/           # Entity ↔ DTO mappers
│   │   └── util/             # Utility helpers
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── templates/        # Thymeleaf email templates
│   │   └── data.sql          # Schema + seed data
│   ├── src/test/             # Unit & integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/              # Axios instance & interceptors
│   │   ├── components/       # Reusable UI components
│   │   ├── pages/            # Route-level page components
│   │   ├── hooks/            # Custom React hooks
│   │   └── context/          # Auth context provider
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 📄 License

This project is licensed under the MIT License.
