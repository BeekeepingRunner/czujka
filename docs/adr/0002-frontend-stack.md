# ADR-0002: Frontend stack — React + Vite + TypeScript + MapLibre

- **Status:** Accepted
- **Date:** 2026-10-16

## Context

The main view is an interactive map of Poland with events refreshed every few tens of seconds.
The author knows Angular. The project is also a portfolio and a learning exercise.

## Decision

- **React 19.2** — the largest ecosystem for map-heavy UIs, and a small mental model (components + hooks).
  It also adds a second popular framework to the author's CV next to Angular.
- **Vite 8** (Rolldown bundler) — fast dev server and builds, and it proxies `/api` to the backend in dev.
- **TypeScript** — 7.0 (the native Go compiler) if the lint tooling (typescript-eslint) supports it when
  we scaffold. Otherwise 6.x, because TS 7.0 does not yet ship a stable programmatic API.
- **MapLibre GL JS 6** — an open-source WebGL vector map with no API key or billing. It handles thousands of
  features and polygon fills (risk per voivodeship). Base map: **OpenFreeMap** tiles (free, OSM data).
- **TanStack Query v5** — server state: polling, caching, loading/error states.
- **Node 24 LTS** for building. Node 26 becomes LTS on 2026-10-28, so we bump later.

## Alternatives considered

- **Angular 20** — familiar, but less learning value and fewer map examples.
- **Leaflet** — simpler, but raster-based and slower with many features.
- **Mapbox GL / Google Maps** — need an API key and are paid above the free tier.

## Consequences

- MapLibre 6 is ESM-only and requires WebGL2, so very old browsers are not supported. That is acceptable.
- The frontend is a static SPA served by nginx, with no server-side rendering.
