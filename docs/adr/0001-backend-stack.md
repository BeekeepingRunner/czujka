# ADR-0001: Backend stack — Java 25 + Spring Boot 4

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

The backend polls external sources, normalises events, stores them and serves a REST API.
The author is an experienced Java/Spring developer, and the project aims to use up-to-date technology.

## Decision

- **Java 25** (current LTS) — records, pattern matching, virtual threads.
- **Spring Boot 4.1.x** (Spring Framework 7) — the current line. Boot 3.5 open-source support has ended.
- **Gradle with Kotlin DSL** as the build tool.
- Virtual threads are on (`spring.threads.virtual.enabled=true`), so blocking HTTP calls to external
  sources stay simple and cheap.
- `RestClient` for outgoing HTTP, `ProblemDetail` for errors.
- Tests: JUnit 5 and Testcontainers with a real PostGIS database.

## Consequences

- The author's existing skills apply, so learning effort goes into new features and AI-assisted workflow.
- Some third-party libraries may lag behind Spring Boot 4 and Jackson 3. We keep dependencies minimal.
- A GraalVM native image is possible later but is not a goal of the MVP.
