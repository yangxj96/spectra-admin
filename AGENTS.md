# AGENTS.md

## Project Overview

Spectra Admin is the **backend API server** for the Spectra system. It serves both the web admin panel (`spectra-ui`) and the mobile app (`spectra-app`).

- Stack: Java 25, Spring Boot 4, Maven multi-module
- Entry point: `com.devops00.spectra.launch.LaunchApplication`
- Dev port: **4004** (set via `SERVER_PORT` in `.mise.local.toml`)
- Both frontends connect to `https://127.0.0.1:4004/` in development

## Build & Run

```bash
# Build (skip tests for speed)
./mvnw clean package -DskipTests

# Run locally (start this before running either frontend)
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

## Code Style & Naming Conventions

### Comments
- Use triple-slash (`///`) comments instead of Javadoc block comments
- Every Java file must include Apache License 2.0 header

### Git Commit Messages
Follow Conventional Commits format: `type(scope): description`
- Common types: `feat`, `fix`, `refactor`, `docs`, `ci`, `chore`, `build`, `style`
- Scope is typically module name: `ai`, `security`, `core`, `framework`, `log`, `project`
- Example: `feat(security): 重构会话令牌Redis键设计并支持登录锁定与令牌续期`

### Naming Conventions
- Entity classes: PascalCase, e.g. `User`, `BaseEntity`
- Controllers: PascalCase + `Controller` suffix
- Services: PascalCase + `Service` suffix (interface), `ServiceImpl` suffix (implementation)
- Form objects: PascalCase + `From` suffix (note: `From` not `Form`)
- VO objects: PascalCase + `VO` suffix
- Package structure: `com.devops00.spectra.{module}.{layer}`

### Layer Structure
```
controller/    → REST endpoints
service/       → Business logic interfaces
service/impl/  → Service implementations
mapper/        → MyBatis-Plus mappers
javabean/
  entity/      → Database entities
  from/        → Request form objects
  vo/          → Response view objects
```

### Entity Conventions
- Use UUID as primary key type (`@TableId(type = IdType.INPUT)`)
- Include audit fields: `createdBy`, `createdAt`, `updatedBy`, `updatedAt`
- Use `Instant deleted` for soft delete (null = not deleted)
- Use `@Version` for optimistic locking
- Use `@OrderBy` for default sorting

### Controller Conventions
- Use constructor injection (not field injection)
- Use `@PreAuthorize` for permission control
- Use `@ULog` for operation logging
- Use `version = "1.0.0+"` in mapping annotations for API versioning
- Use `@Validated(Verify.Insert.class)` or `@Validated(Verify.Update.class)` for group validation

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
- API changes here directly break `spectra-ui` and `spectra-app` — coordinate with frontend when modifying endpoints

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
