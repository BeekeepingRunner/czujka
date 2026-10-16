# ADR-0004: PostgreSQL + PostGIS for storage

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

Events have a location: a point or an area such as a county or voivodeship. Key queries are spatial:
"events in the visible map area", "events within 30 km of me", "events in this voivodeship".

## Decision

- **PostgreSQL with the PostGIS extension**, with geometries stored in WGS 84 (`SRID 4326`).
- Schema migrations with **Flyway**.
- Spatial queries are written in plain SQL (`ST_MakeEnvelope`, `ST_DWithin`) and run through Spring's
  `JdbcClient`. No JPA or Hibernate Spatial: the few queries we need stay explicit and easy to read.

## Alternatives considered

- **Plain PostgreSQL + lat/lng columns** — fine for points, but awkward for areas and distances.
- **MongoDB geo queries** — would add a second paradigm without a real benefit.

## Consequences

- Testcontainers uses the `postgis/postgis` image, so tests match production.
- Voivodeship and county boundaries can be loaded once as reference data.
