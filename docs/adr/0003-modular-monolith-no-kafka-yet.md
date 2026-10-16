# ADR-0003: Modular monolith first, Kafka later

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

Ingestion naturally looks like an event stream (sources → normalisation → storage → notifications),
and Kafka would fit it well. But the MVP has a few sources polled every minute or so: a few events per
minute at most. One developer maintains the system.

## Decision

- Build **one Spring Boot application** organised **by feature** (`event`, `ingestion`, later
  `notification`, `risk`), with clear boundaries between packages.
- Inside the app, features talk through **Spring application events**. That is the seam where a message
  broker can plug in later without changing the domain code.
- **No Kafka** in M0–M3.

## When to revisit

Introduce Kafka (or a lighter broker) when at least one of these is true:
- ingestion and the API need to scale or deploy separately,
- a high-volume source (e.g. ADS-B) arrives,
- several consumers (notifications, risk scoring, archive) need to replay the event stream.

## Consequences

- Fewer moving parts: `docker compose up` starts three containers.
- A later migration to Kafka is a deliberate step, documented in a new ADR.
