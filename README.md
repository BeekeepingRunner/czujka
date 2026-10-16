# Czujka

A map-first web app that gathers public information about threats around the place you live in Poland
(civil alerts, air threats, weather and flood warnings) and shows it on a single map.

> ⚠️ Czujka is **not an official warning system**. In an emergency follow the instructions of the authorities (RCB, 112).

**Status:** early development. The walking skeleton shows sample (mock) events on the map.

## Run it

Requirements: Docker (Docker Desktop or Rancher Desktop with the `dockerd (moby)` engine).

```bash
docker compose up --build
```

Open http://localhost:8080. The API is available at http://localhost:8080/api/events.

For local development (JDK 25, Node 24), see [CLAUDE.md](CLAUDE.md#commands).

## Documentation

- [Product vision](docs/VISION.md)
- [Roadmap](docs/ROADMAP.md)
- Architecture decision records: [docs/adr](docs/adr)

## Tech stack

Java 25 · Spring Boot 4.1 · PostgreSQL + PostGIS · React 19 · Vite 8 · TypeScript · MapLibre GL JS · Docker

## Credits

- Base map: [OpenFreeMap](https://openfreemap.org), data © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors
- Voivodeship boundaries: [ppatrzyk/polska-geojson](https://github.com/ppatrzyk/polska-geojson) (MIT)
