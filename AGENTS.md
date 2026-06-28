# AGENTS.md

## Project Overview

Spectra Admin is a Java backend system built with Spring Boot 4 + JDK 25, using Maven multi-module architecture.

## Build & Run

```bash
# Build (skip tests for speed)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run -pl spectra-launch

# Or run the built jar
java -jar spectra-launch/target/spectra-launch-*.jar
```

## Module Structure

```
spectra-common       → shared utils, DTOs, MyBatis-Plus config
spectra-framework    → platform config, Redis, AOP, caching
spectra-starter/     → auto-configuration starters
  spectra-security-base
  spectra-security-spring-boot-starter
  spectra-log-base
  spectra-log-spring-boot-starter
  spectra-ai-base
spectra-modules/     → business modules
  spectra-core       → core business logic
  spectra-upload     → file upload (S3)
  spectra-workflow   → Flowable workflow
  spectra-oa         → OA module
  spectra-ai         → AI integration (LangChain4j)
spectra-launch       → application entry point, run this
```

Entry point: `com.devops00.spectra.launch.LaunchApplication`

## Environment Setup

Uses mise for toolchain management. Copy `.mise.local.toml.example` to `.mise.local.toml` and configure:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - PostgreSQL connection
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` - Redis connection
- `DEFAULT_PASSWORD` - default user password
- `SSL_*` - SSL configuration (optional)
- `S3_*` - S3 storage config
- `AI_KEY`, `AI_BASE_URL`, `AI_MODEL` - AI service config

Required services: PostgreSQL, Redis.

## Key Conventions

- **Java 25** with Temurin distribution
- **UTC timezone** forced at startup (display formatting handles local timezone)
- **MapStruct dependency order matters**: mapstruct → lombok → mapstruct-processor (breaking change if reordered)
- **Version property**: `${revision}` in pom.xml, flattened by `flatten-maven-plugin`
- **Spring profiles**: `dev` (local), `prod` (Docker/deploy)
- **Config import order**: application-common → framework → core → oa → upload → ai → workflow

## Testing

```bash
# Run all tests
./mvnw test

# Run tests in specific module
./mvnw test -pl spectra-common
```

Currently no test files in spectra-launch module.

## Docker

Build requires full JDK image for captcha/AWT support:

```bash
# From spectra-launch directory
docker build --build-arg JAR_FILE=spectra-launch-*.jar -t spectra-admin .
```

Use `paketobuildpacks/builder-jammy-full` if using Spring Boot build-image goal.

## CI/CD

GitHub Actions workflow: `.github/workflows/spectra-minimal-image.yml`
- Manual trigger (`workflow_dispatch`)
- Builds Maven project, creates Docker image, pushes to GHCR

## Common Pitfalls

- `.mise.local.toml` is gitignored - must create from example before running
- JVM args for native: `--add-modules ALL-SYSTEM --enable-native-access=ALL-UNNAMED`
- PostgreSQL required on port 5432 by default
- Redis required on port 6379 by default
