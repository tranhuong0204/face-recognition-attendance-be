# BE-web
# Face Recognition Attendance System — Backend

[![Backend CI](https://github.com/tranhuong0204/face-recognition-attendance-be/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/tranhuong0204/face-recognition-attendance-be/actions/workflows/ci.yml)

A Spring Boot REST API backend for a face-recognition-based attendance system. The application manages authentication, attendance operations, and business logic, with integrations for a separate Python-based AI service and a Flutter client.

## Overview

This project is the backend component of a team-developed face recognition attendance system. It provides REST APIs for the client application, manages application data, and supports secure access to protected resources.

The system combines conventional backend technologies with AI-based face processing and location verification to support attendance workflows.

## Key Features

- **RESTful APIs:** Backend endpoints for authentication, attendance sessions, check-in/check-out, attendance history, and related operations.
- **Authentication and authorization:** Spring Security and JWT-based authentication for protected endpoints.
- **Database integration:** Spring Data JPA and Hibernate for persistence with MySQL-compatible databases, including TiDB Cloud.
- **AI service integration:** Communicates with a separate Python service for face-related processing, including image uploads and face embedding workflows.
- **Location verification:** Supports GPS-based attendance verification and anti-mock-location checks as part of the attendance workflow.
- **API documentation:** Swagger UI and OpenAPI documentation for exploring and testing API endpoints.
- **Health monitoring:** Spring Boot Actuator health endpoint for application status checks.
- **Automated CI:** GitHub Actions runs Maven verification on pushes and pull requests targeting the `develop` branch.
- **Continuous deployment:** Render deploys the backend after CI checks pass.

## Tech Stack

| Category | Technologies |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL-compatible database, TiDB Cloud |
| API documentation | Springdoc OpenAPI, Swagger UI |
| Build tool | Maven |
| Containerization | Docker |
| CI/CD | GitHub Actions, Render |
| AI integration | Python-based face processing service |
| Client integration | Flutter REST API integration |

## Architecture

The backend is part of a multi-component system.

```text
Flutter Client
      |
      | HTTPS / REST API
      v
Spring Boot Backend
      |
      +---- Spring Security / JWT
      |
      +---- Attendance Business Logic
      |
      +---- Spring Data JPA
      |          |
      |          v
      |      TiDB Cloud
      |
      +---- Python AI Service
                 |
                 +---- Face Detection
                 +---- Face Embedding Processing
                 +---- Anti-spoofing
```

**Component responsibilities**

- **Flutter client:** Provides the mobile application interface and communicates with the backend through REST APIs.
- **Spring Boot backend:** Handles authentication, attendance workflows, business rules, database operations, and integration with the AI service.
- **TiDB Cloud:** Stores application data through a MySQL-compatible connection.
- **Python AI service:** Handles face-related processing separately from the Java backend.

The Python AI service is maintained in a separate repository and must be configured and deployed independently.

## Getting Started

### Prerequisites

- Java 17
- Maven, or use the included Maven Wrapper
- A MySQL-compatible database, such as MySQL or TiDB Cloud
- Git
- Docker (optional, for containerized execution)

### 1. Clone the repository

```bash
git clone https://github.com/tranhuong0204/face-recognition-attendance-be.git
cd face-recognition-attendance-be
```

### 2. Configure environment variables

Configure the required environment variables in your local environment or deployment platform.

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for the database |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | Secret used to sign JWTs |
| `ADMIN_PASSWORD` | Password for the configured admin account |

Depending on the features enabled in your environment, you may also need to configure Cloudinary credentials and other service-specific settings.

**Security:** Never commit real passwords, JWT secrets, API keys, or production credentials to Git. Keep `.env` files out of version control.

Example database configuration for a local MySQL instance:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hit_learning_connect
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=replace_with_a_secure_secret
ADMIN_PASSWORD=replace_with_a_secure_password
```

These values are examples. Replace them with your own environment-specific configuration.

### 3. Run the application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

The application uses port `8081` by default when no deployment-specific `PORT` is provided. Configure the datasource and required secrets before starting the application.

### 4. Access API documentation

When the application is running locally, open:

- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI specification: `http://localhost:8081/v3/api-docs`
- Health endpoint: `http://localhost:8081/actuator/health`

The application may use a different port depending on its environment configuration.

## Testing

Run the Maven verification lifecycle:

```bash
./mvnw clean verify
```

On Windows:

```bash
mvnw.cmd clean verify
```

This runs the project's configured verification steps, including tests and build checks.

## CI/CD Pipeline

The project uses GitHub Actions and Render to automate verification and deployment.

```text
Push / Pull Request to develop
              |
              v
       GitHub Actions CI
              |
              v
       Maven clean verify
              |
       +------+------+
       |             |
      Fail          Pass
       |             |
       v             v
  No deployment   Render deploys
```

The GitHub Actions workflow is defined in `.github/workflows/ci.yml`.

The pipeline:

1. Checks out the repository.
2. Sets up Java 17.
3. Runs Maven verification.
4. Allows Render to deploy only after the configured CI checks pass.

Render is configured to use **After CI Checks Pass** for Auto-Deploy.

## Deployment

The backend is deployed as a web service on Render, with TiDB Cloud providing the external database.

Deployment configuration includes:

- Runtime port configured through the `PORT` environment variable.
- Database connection configured through environment variables.
- JWT and administrator credentials supplied securely through deployment settings.
- Docker-based application packaging.
- `/actuator/health` configured as the health-check endpoint.

The Python AI service is a separate component and is not included in the backend deployment.

## Project Repository

- **Backend:** [face-recognition-attendance-be](https://github.com/tranhuong0204/face-recognition-attendance-be)
- **AI service:** [face_recognition_pipeline](https://github.com/tranhuong0204/face_recognition_pipeline)

## Notes

- Face recognition and anti-spoofing depend on the separately maintained AI service.
- GPS and anti-mock-location checks provide additional verification but should not be considered a guarantee against every possible form of location spoofing.
- Production availability depends on the hosting platform, database, and any external services used by the application.

## License

No license has been specified for this repository yet.

## Live Demo

- **Swagger UI:** [Explore the API](https://face-recognition-attendance-be.onrender.com/swagger-ui/index.html)
- **Health Check:** [Application Health](https://face-recognition-attendance-be.onrender.com/actuator/health)

The backend is deployed on Render. Swagger UI provides an interface for exploring and testing the available REST API endpoints.

> Note: The service may take some time to respond after a period of inactivity on the free hosting plan. Endpoints requiring authentication need a valid JWT access token.
