# ADR-0005: Playwright smoke test against the full stack in CI

- **Status:** Accepted
- **Date:** 2026-11-11

## Context

CI builds and unit-tests the backend and frontend, and builds the Docker images, but it never runs them.
Problems in the runtime images (nginx, JRE, PostGIS) and in frontend runtime libraries (MapLibre, React,
TanStack Query) only showed up when someone opened the map by hand. The MapLibre worker served with the wrong
MIME type, for example, gives a blank map that unit tests cannot see. Most PRs are Dependabot updates of exactly
these parts, so every one needed a manual browser check.

## Decision

- **One Playwright smoke test** (`frontend/e2e/smoke.spec.ts`) in the CI `docker` job. The job starts the stack with
  `docker compose up` and opens the map in headless Chromium. The test checks that the MapLibre worker is served as
  JavaScript, that `/api/events` returns events through the nginx proxy, that they reach the UI, that the map
  initialises, and that there are no console errors or failed requests.
- **No dependency on external servers.** The OpenFreeMap style is replaced by a local stand-in
  (`e2e/fixtures/base-style.json`), and every other request that leaves the app is blocked and fails the test.
- **No visual regression testing** for now. The UI is still changing, and WebGL rendering differs slightly between
  runs, so screenshot comparisons would mostly produce noise.
- **Docs-only changes skip CI** (`paths-ignore` in `ci.yml`), so the extra minutes are spent only where they can
  find something.

## Alternatives considered

- **Cypress**: heavier, with weaker multi-browser and network-routing support, and no advantage for a single test.
- **curl checks only**: they check nginx and the API, but not the MIME type as the browser enforces it, WebGL or
  JavaScript errors, which is where blank maps come from.
- **Keep checking by hand**: slow, easy to forget, and it does not scale with the Dependabot flow.

## Consequences

- The `docker` job takes about 2 minutes, mostly building the images. It runs in parallel with the backend
  and frontend jobs, so the CI wall-clock time grows by about a minute rather than by the whole job. The Chromium
  headless shell is cached per Playwright version, and system packages are not installed with `--with-deps`
  because the runner image already has them.
- `@playwright/test` is one more dev dependency for Dependabot.
- UI changes still need a look in a browser. The smoke test proves that the map works, not that it looks right.
