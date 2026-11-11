# REVIEW.md

How to review a pull request in Czujka, for AI agents and humans alike. Coding conventions live in
[CLAUDE.md](CLAUDE.md) and product rules in [docs/VISION.md](docs/VISION.md). This file says what to
check against them, and what matters most.

## Severity

Label each finding so the author knows what blocks the merge:

- **Blocker**: a bug, a security problem, data loss, or a broken build. Must be fixed before merging.
- **Should fix**: a real problem that is not urgent, like a missing test for new logic or a misleading name.
  Fix it in this PR or open a follow-up issue.
- **Nit**: style or taste. Optional, and never the only reason to hold a PR.

Report only what you have checked. If you are not sure something is a bug, say so and explain what
would confirm it.

## What to check

### Rollback

Every change must be easy to undo. By default the rollback is `git revert` of the merge commit, so check
that a revert really restores the previous behaviour. If it does not, the PR description has a **Rollback**
section that says how to undo it. A change that cannot be undone and has no rollback plan is a blocker.

- **One change per PR.** A refactor, a feature and a dependency update are separate PRs, so one can be
  reverted without the others.
- **Migrations are forward-only.** We use Flyway Community, which has no undo migrations (`U<n>__` scripts
  are a paid feature). Even where they exist, an undo cannot bring back dropped data, and `git revert` does
  not run them. Reverting the code leaves the new schema in place, so the previous version of the code must
  still work with it:
  - Additive changes are safe: new tables, new nullable columns, new indexes.
  - Destructive changes (dropping or renaming a column or table, narrowing a type, adding `NOT NULL` to
    existing data) go in their own PR, after a PR in which the code has stopped using what is removed.
  - A migration that changes or deletes existing data says in the PR how to restore that data.
  - To undo a schema change, add a compensating migration (a new `V<n+1>__` that reverses it). Never
    delete or edit the applied one.
- **Risky behaviour can be switched off without changing code.** A new data source has its
  `czujka.sources.<source>.enabled` switch, and new behaviour that might misbehave in production gets a
  property switch too. A property is a faster rollback than a revert.
- **Ingested data can be repaired.** Upserts overwrite events, so a broken mapping corrupts stored data that
  a revert does not fix. Check that a fixed version re-fetching the source corrects the events, or that the PR
  explains how to clean them up.
- **The backend API changes compatibly.** Add fields instead of renaming or removing them. When a field must
  go, remove it from the frontend first, so either PR can be reverted on its own.
- **Versions are locked.** npm dependencies are locked in `package-lock.json`, the Gradle version is set in
  the wrapper, and images never use `latest`. That way a revert brings back the previous versions. Image tags
  such as `nginx:1.31-alpine` still get patch updates within their line, so a revert restores the line, not
  the exact image.

### Untrusted data and safety

- Text from external sources (titles, descriptions, URLs) is never rendered as HTML. The frontend uses
  `textContent` and DOM APIs, never `innerHTML` or `dangerouslySetInnerHTML` (see `src/map/eventPopup.ts`).
- When the UI links to an event's original source (`url`), it allows only `http(s)` URLs and opens them
  with `rel="noopener noreferrer"`.
- SQL uses named parameters (`:name` with `JdbcClient`). User input and external data are never
  concatenated into SQL.
- No secrets in the repo. Credentials come from environment variables and have no default value in
  `application.yaml` (`DB_PASSWORD` is required, and `DatabasePasswordCheck` enforces it).
- Nothing in the UI suggests Czujka is an official warning system. The disclaimer stays, and every event
  shows its source.

### Data sources (`ingestion/<source>`)

- Follows "How to add a new data source" in CLAUDE.md: its own sub-package, an `EventSource`, a
  `czujka.sources.<source>.enabled` switch, and the source listed in the VISION table.
- `externalId` is stable across fetches, so re-fetching updates an event instead of duplicating it.
- HTTP calls have connect and read timeouts. One slow or failing source must not block or break the others.
- Malformed records are skipped and logged, not allowed to fail the whole batch.
- The mapping is tested against a saved sample response in `src/test/resources/<source>/`. Tests never
  call real APIs.
- The source's terms allow reuse, and the PR says where that was checked.

### Geo and time

- Coordinate order is **longitude, latitude** everywhere: `ST_Point(lon, lat, 4326)`, `ST_MakeEnvelope`,
  GeoJSON and the `bbox` parameter. A swapped pair puts events in the wrong country and still passes many
  tests, so check it explicitly.
- Geometries use SRID 4326.
- Times are `Instant` in Java and `TIMESTAMPTZ` in the database. No `LocalDateTime` and no server-local
  time zones. Formatting for display happens only in the frontend.
- "Active" queries handle a missing `ends_at` (`ends_at IS NULL`).

### Database

- Schema changes are a new Flyway migration `V<n>__description.sql`. An applied migration is never edited.
- New columns used in filters or ordering have an index (GIST for geometry).
- Queries that return lists have a limit.

### API and frontend

- A change to the backend JSON shape is matched in `frontend/src/api/events.ts`, in the same PR.
- Invalid input returns `400` with an RFC 9457 problem body (through `ApiExceptionHandler`), not `500`.
- A UI change has been checked in a browser, not only by unit tests. MapLibre problems such as the worker
  `.mjs` MIME type show up as a blank map that the tests do not catch.

### Code quality

- Matches the surrounding code: records, constructor injection, package-private by default, and
  package-by-feature.
- New logic has tests. Spatial or time-based SQL is tested against real PostGIS (Testcontainers), not mocks.
- Simple beats clever. A new framework, module or service needs an ADR (`docs/adr/`), especially anything
  that reverses an existing ADR (for example adding Kafka, ADR-0003).

## Dependency PRs (Dependabot)

CI runs the backend build and tests, the frontend lint, format check, tests and build, and a Docker image
build. It also validates `gradle-wrapper.jar` against the official checksums, so a green CI covers the
wrapper and you do not need to check the jar by hand.

What CI does **not** cover, and is worth a look:

- **Major versions**: read the breaking changes and check each one against how this project uses the library.
- **Runtime images** (nginx, JRE, PostGIS): CI only builds them. Run `docker compose up --build`, then check
  that the map loads and `/api/events` responds.
- **Frontend runtime libraries** (MapLibre, React, TanStack Query): open the map in a browser.

Every dependency PR gets a review, minor and patch updates included. Read the release notes, then apply the
checks above where they fit. One dependency PR is one revert, so do not combine a dependency update with code
changes.

## Out of scope

Do not flag:

- Formatting that Prettier or oxlint would catch. CI already does.
- Missing features that are planned for a later milestone in [docs/ROADMAP.md](docs/ROADMAP.md).
- Personal style preferences that contradict the conventions in CLAUDE.md.
