<h1 align="center">Course Platform</h1>
<p align="center">
  <img src="https://socialify.git.ci/MostafaSensei106/course/image?custom_language=Kotlin&font=KoHo&language=1&logo=https%3A%2F%2Favatars.githubusercontent.com%2Fu%2F138288138%3Fv%3D4&name=1&owner=1&pattern=Floating+Cogs&theme=Light" alt="Course Platform Banner">
</p>

<p align="center">
  <strong>online course backend built in Kotlin & Spring Boot.</strong><br>
  Fast. Scalable. Robust. Built with Clean Architecture.
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#features">Features</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#installation">Installation</a> •
  <a href="#configuration">Configuration</a> •
  <a href="#technologies">Technologies</a> •
  <a href="#testing">Testing</a> •
  <a href="#deployment">Deployment</a>
</p>

---

## About

Welcome to **Course**a backend built with **Kotlin** and **Spring Boot 4.1**, leveraging the power of **Java 21 Virtual Threads** for maximum concurrency (Thanaweya Amma scale) and **PostgreSQL** for reliable data persistence. 

This project empowers developers with a strict **Clean Architecture** (Domain-Driven Design) that elegantly separates business logic from data access. Whether you're building a small e-learning site or a massive educational platform, it provides a solid, highly-tuned foundation for user management, course catalogs, and secure enrollments.

---

## Features

### 🌟 Core Functionality

- **High-Performance Engine**: Optimized for massive throughput and low latency using Java 21 Virtual Threads and aggressive connection pooling.
- **Course Management**: Comprehensive catalog operations, from drafting to publishing courses, with category support.
- **Secure Authentication**: Built-in, stateless JWT (JSON Web Token) authentication powered by Spring Security.
- **Clean Architecture**: Decoupled layers (Delivery/Controllers, Use Cases, Domain Entities, Data Adapters) for maximum maintainability and testability.
- **PostgreSQL Power**: Robust data handling with Spring Data JPA and version-controlled schemas via Flyway.

### 🛠️ Advanced Capabilities

- **Modular Design**: Feature-sliced into modules (`auth`, `course`) so you can easily extend or extract microservices later.
- **API Documentation**: Auto-generated interactive Swagger UI and OpenAPI 3 specifications.
- **Docker Ready**: Fully containerized local database environment via `compose.yaml`.
- **Global Error Handling**: Standardized, predictable API error responses using a unified `Result` and `GlobalExceptionHandler` pipeline.

---

## Architecture

To ensure long-term maintainability, this project strictly follows **Clean Architecture** and **Domain-Driven Design (DDD)** principles, organizing code by feature modules rather than technical layers.

### Directory Structure

```text
├── src/main/
│   ├── kotlin/com/mostafasensei/course/
│   │   ├── core/                  # Cross-cutting concerns and shared logic
│   │   │   ├── config/            # Spring configurations (OpenAPI, Threading)
│   │   │   ├── delivery/          # Global exception handlers and response formatting
│   │   │   ├── error/             # Custom error definitions
│   │   │   ├── logging/           # Request logging and MDC filters
│   │   │   ├── security/          # JWT Auth filters, current user extraction
│   │   │   └── utils/             # UseCase base classes, Result wrappers
│   │   └── modules/               # Feature-specific modules
│   │       ├── auth/              # User authentication, registration, profiles
│   │       │   ├── data/          # JPA Entities (UserJpaEntity), Repositories
│   │       │   ├── domain/        # Domain entities (User), Security Ports
│   │       │   └── handler/       # Controllers, UseCases, DTOs
│   │       └── course/            # Course catalog, lessons, enrollments
│   │           ├── data/          # Course, Category, Enrollment JPA Entities
│   │           ├── domain/        # Domain entities (Course, CourseStatus)
│   │           └── handler/       # Course APIs and UseCases
│   └── resources/
│       ├── application.properties # App configuration (DB, Tuning, Swagger)
│       └── db/migration/          # Flyway SQL migrations (e.g., V2__thanaweya_indexes.sql)
```

### Request Lifecycle

1. **Routing**: A request hits the Spring Boot Router/Controller (`handler/controller/`).
2. **Security**: Middleware (`JwtAuthFilter`) authenticates the JWT token, extracts the `CurrentUser`, and sets the security context.
3. **Delegation**: The Controller delegates the payload to a specific `UseCase` (`handler/usecase/`).
4. **Business Logic**: The `UseCase` enforces business rules using pure Kotlin Domain Entities (`domain/entity/`).
5. **Persistence**: Data is fetched/persisted via Domain Repository interfaces, implemented by Spring Data JPA Repositories in the `data` layer.
6. **Response**: Results are wrapped in a `Result` monad, returned to the controller, and formatted by the `GlobalExceptionHandler` on failure.

### Database Schema Overview

```text
users
├── id (uuid, PK)
├── email (varchar, unique, not null)
├── password_hash (varchar)
├── first_name, last_name (varchar)
├── role (enum: STUDENT, INSTRUCTOR, ADMIN)
├── is_verified, is_active (boolean)
└── created_at, updated_at (timestamp)

courses
├── id (uuid, PK)
├── title (varchar, not null)
├── slug (varchar, unique, not null)
├── description (text)
├── price (numeric)
├── status (enum: DRAFT, PUBLISHED)
├── instructor_id (uuid, FK → users)
├── category_id (uuid, FK → categories)
└── created_at, updated_at (timestamp)
```

*(Note: Additional optimized indexes exist for high-traffic paths like instructor listings and enrollments)*

---

## Installation

### ⚠️ Prerequisites

- **Java 21 (JDK 21)**: Required for compiling the application and utilizing Virtual Threads.
- **Docker & Docker Compose**: For spinning up the local PostgreSQL database.
- **Git**: For version control.

### 📦 Easy Setup (Local Development)

```bash
# 1. Clone the repository
git clone https://github.com/MostafaSensei106/course.git
cd course

# 2. Start the Database (PostgreSQL)
# This uses the included compose.yaml to spin up a tuned Postgres instance
docker compose up -d postgres

# 3. Build and Run the Application
# The Gradle wrapper will automatically download dependencies
./gradlew bootRun
```

The server will start on `http://localhost:8080`.

### 📋 API Documentation

Once running, explore the interactive API docs:
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Configuration

The application uses `src/main/resources/application.properties` for configuration. For production, these values should be overridden using Environment Variables.

### 🧾 Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/mydatabase` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `root` |
| `SPRING_THREADS_VIRTUAL_ENABLED` | Enable Java 21 Virtual Threads | `true` |
| `SERVER_TOMCAT_THREADS_MAX` | Max Tomcat connections | `400` |

---

## Available Scripts

Here are the most common commands you'll use during development:

| Command | Description |
| --- | --- |
| `./gradlew bootRun` | Starts the Spring Boot development server |
| `./gradlew build` | Compiles the project, runs tests, and builds the executable JAR |
| `./gradlew clean` | Cleans the `build/` directory |
| `docker compose up -d` | Starts local infrastructure (PostgreSQL) |
| `docker compose down` | Stops local infrastructure and removes containers |

---

## Testing

This project utilizes **JUnit 5** for testing. 

```bash
# Run the entire test suite
./gradlew test

# Run tests for a specific module (e.g., Auth)
./gradlew test --tests "com.mostafasensei.course.modules.auth.*"
```

---

## Deployment

### 🐳 Docker (Recommended)

To deploy the application, you can containerize the Spring Boot artifact. Create a `Dockerfile` in the root:

```dockerfile
# Build Stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run for production:

```bash
# Build the image
docker build -t course-api .

# Run the container with production DB credentials
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://production-db-host:5432/proddb \
  -e SPRING_DATASOURCE_USERNAME=produser \
  -e SPRING_DATASOURCE_PASSWORD=prodpass \
  course-api
```

## Technologies

| Technology | Description |
| --- | --- |
| 🧠 **Kotlin** | [kotlinlang.org](https://kotlinlang.org/) — Modern, expressive, and safe language for the JVM |
| 🚀 **Spring Boot** | [spring.io](https://spring.io/projects/spring-boot) — The most popular framework for enterprise Java/Kotlin apps |
| 🗄️ **PostgreSQL** | [postgresql.org](https://www.postgresql.org/) — World's most advanced open source relational database |
| 🛡️ **Spring Security** | [spring.io/security](https://spring.io/projects/spring-security) — Powerful authentication and access-control |
| 🛠️ **Flyway** | [flywaydb.org](https://flywaydb.org/) — Database migration made easy |
| 🐳 **Docker** | [docker.com](https://www.docker.com/) — Containerization for consistent environments |

---

## Contributing

Contributions are welcome! Here’s how to get started:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m "Add amazing feature"`
4. Push to your branch: `git push origin feature/YourFeature`
5. Open a pull request.

---

## License

This project is licensed under the **MIT License**.
See the [LICENSE](LICENSE) file for full details.

<p align="center">
  Made with ❤️ by <a href="https://github.com/MostafaSensei106">MostafaSensei106</a>
</p>
