# Roadmap

Every milestone ends with a working, deployable app and a green CI run.

| Milestone | Goal | Key deliverables |
|---|---|---|
| **M0 — Walking skeleton** | Prove the whole pipeline end to end | Docs + ADRs, Spring Boot backend with `EventSource` abstraction and a mock source, PostGIS + Flyway, React + MapLibre map of Poland, Docker Compose, GitHub Actions CI |
| **M1 — First real data** | Real events on the map | IMGW meteo/hydro warnings adapter, user location (geolocation / manual), events filtered by the visible map area |
| **M2 — Civil alerts & risk** | Answer "is anything happening near me?" | RCB and/or RSO adapter, voivodeship polygons in PostGIS, simple explainable risk level per voivodeship, event history view |
| **M3 — Notifications** | Tell users without them watching | PWA + Web Push, subscription per location / radius / category |
| **M4 — Airspace** | Air-threat context | PAŻP airspace restrictions, ADS-B data (with reliability labels) |
| **M5 — Platform** | Production-grade delivery | Kubernetes (k3d locally, Helm chart), observability (OpenTelemetry, Prometheus, Grafana), Kafka if ingestion needs it (see ADR-0003), public deployment |

Ideas backlog: dark mode, Polish/English UI, air-quality layer (GIOŚ), embeddable widget, public API.
