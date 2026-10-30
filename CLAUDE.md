# CLAUDE.md

Guidance for AI coding agents (and humans) working on Czujka. Product context: [docs/VISION.md](docs/VISION.md).

## Repository layout

- `backend/` — Spring Boot 4.1, Java 25, Gradle (Kotlin DSL). Package-by-feature under `pl.czujka`:
  - `event/` — `ThreatEvent` domain record, `EventRepository` (JdbcClient + PostGIS SQL), REST controller
  - `ingestion/` — `EventSource` interface, `IngestionScheduler`, one sub-package per source (e.g. `mock/`)
  - `common/` — cross-cutting code (error handling)
- `frontend/` — React 19, Vite 8, TypeScript, MapLibre GL JS 6, TanStack Query
  - `src/api/` — backend types and fetching hooks, `src/map/` — map component and helpers
- `docs/` — vision, roadmap, ADRs (`docs/adr/NNNN-title.md`)
- `compose.yaml` — the full stack: PostGIS, backend, frontend (nginx)

## Commands

| Task | Command |
|---|---|
| Backend build + tests (needs Docker for Testcontainers) | `cd backend && ./gradlew build` |
| Backend dev run with a throwaway PostGIS container | `cd backend && ./gradlew bootTestRun` |
| Frontend dev server (proxies `/api` to :8080) | `cd frontend && npm run dev` |
| Frontend checks | `cd frontend && npm run lint && npm run format:check && npm test && npm run build` |
| Whole stack | `docker compose up --build`, then open http://localhost:8080 |

## How to add a new data source

1. Create a sub-package `pl.czujka.ingestion.<source>` with a `@Component` class implementing `EventSource`.
2. Map the external data to `ThreatEvent`. `externalId` must be stable so re-fetching updates the event.
3. Make it switchable with `@ConditionalOnBooleanProperty("czujka.sources.<source>.enabled")` and add the
   property to `application.yaml`.
4. Test the mapping with a saved sample response under `src/test/resources/<source>/`. Tests never call real APIs.
5. Update the source table in `docs/VISION.md`.

## Conventions

- Keep code simple and readable. Prefer records, constructor injection and package-private visibility.
- External text is untrusted: never render it as HTML in the frontend.
- Schema changes go through a new Flyway migration (`V<n>__description.sql`). Never edit an applied one.
- Workflow: one branch + PR per change, Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:` …).
  CI must be green before merging.
- Record significant technical decisions as a new ADR.
