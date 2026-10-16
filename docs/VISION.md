# Czujka — Product Vision

> **Czujka** (pl. *detector, sentinel*) — a web app that gathers public information about threats
> around the place you live in Poland and shows it on a single map.

## 1. Problem

People in Poland receive safety information from many disconnected channels: RCB SMS alerts,
voivodeship announcements about siren tests and alarms, IMGW weather warnings, air-quality reports,
airspace restrictions, and news/OSINT about drones and missiles near the eastern border.
Each channel has its own format, delay and level of detail. There is no simple place where a
resident can answer the question:

> *"Is anything happening right now that may affect me, where I live?"*

## 2. Vision statement

For **residents of Poland** who want to stay informed about local threats,
**Czujka** is a **map-first web app** that **aggregates public threat data from many sources,
normalises it into one event model and shows what matters near the user's location**.
Unlike official channels, it puts all sources side by side, always showing **where each piece of
information came from and how trustworthy it is**.

## 3. Target users

| Persona | Need |
|---|---|
| **Resident** (primary) | Glance at the map, see if anything is happening in my voivodeship/city, get notified when it does. |
| **Curious / informed citizen** | Browse history, compare sources, understand the situation in other regions. |
| **Developer (portfolio reviewer)** | Read clean code, a clear architecture and the reasoning behind decisions. |

## 4. Goals

1. **One map, many sources** — a main view with a map of Poland (or zoomed on the user's area)
   showing current events from several external sources.
2. **Location awareness** — the user sets a location (browser geolocation or a chosen place) and sees
   what is relevant within a radius / in their voivodeship.
3. **Normalised event model** — every source is mapped to one `ThreatEvent` (category, severity,
   location/area, time window, source, link to the original).
4. **Transparency** — every event shows its source, publication time and reliability level
   (official / semi-official / community-OSINT).
5. **Risk overview** *(later)* — a simple, explainable risk level per voivodeship, computed from recent events.
6. **Notifications** *(later)* — Web Push (PWA) when a relevant event appears near the user.
7. **Easy to extend** — adding a new data source means writing one class.

## 5. Non-goals

- **Not an official warning system.** Czujka never replaces RCB, RSO or the authorities. The UI
  shows a disclaimer, and official alerts are always linked to their origin.
- **No military or classified data**, no attempt to predict attacks, no detection of objects.
- **No user-generated reports** in the first versions (moderation and misinformation risk).
- **No native mobile apps** at first — a responsive web app / PWA is enough.

## 6. Key assumptions

- Enough useful data is **publicly available** (APIs, RSS, open data) and its terms allow
  non-commercial reuse. Each source is checked before it is integrated.
- **Polling** external sources every 30 s – 5 min is good enough for an MVP. Real-time push is a later goal.
- The project has **one developer working with AI agents**, so simplicity beats completeness: a
  modular monolith, few moving parts, everything runs with `docker compose up`.
- Data about drones, missiles or aircraft may be **wrong or late**. The UI must show uncertainty
  instead of hiding it.

## 7. Data source catalogue

Status: ✅ known public API · 🔍 to investigate (format, terms, availability) · ⏳ later

| Source | What it provides | Category | Reliability | Access | Status |
|---|---|---|---|---|---|
| **IMGW-PIB** – danepubliczne.imgw.pl | Meteorological & hydrological warnings per county/voivodeship | weather, flood | official | public REST API (JSON) | ✅ first real source (M1) |
| **GIOŚ** – api.gios.gov.pl | Air-quality index per measuring station | air quality | official | public REST API | ✅ |
| **RCB** – Rządowe Centrum Bezpieczeństwa | Alert RCB messages (sent as SMS / mObywatel) | civil alerts | official | gov.pl pages; no documented API | 🔍 |
| **RSO** – Regionalny System Ostrzegania | Regional warnings & announcements | civil alerts | official | RSO app / open data | 🔍 |
| **Voivodeship offices** | Siren alarms and tests, crisis-management announcements | sirens | official | websites / social media | 🔍 |
| **PAŻP** | Airspace restrictions (e.g. temporary closures near the eastern border) | airspace | official | AIP / AUP-UUP publications | 🔍 (M4) |
| **ADS-B** – adsb.lol, airplanes.live | Live aircraft positions, incl. some military | air traffic | semi-official / community | public APIs (licence and rate limits apply) | ⏳ (M4) |
| **OSINT** | Reports on drones and missiles over Ukraine near the PL border | air threat | community, low | various | ⏳ — a careful choice is needed |

Lesson learned from similar apps: aggregated OSINT has already put a "drone on the Polish side"
of the border by mistake. Czujka must label low-reliability data clearly.

## 8. MVP scope (milestones M0–M2)

- Map of Poland with voivodeship boundaries (MapLibre, free OSM-based vector tiles).
- Events shown as markers/areas, coloured by severity, with a details popup (source, time, link).
- User location (browser geolocation or a manual choice), with the map centred on it.
- Backend that polls sources on a schedule, normalises events, stores them in PostGIS and exposes
  `GET /api/events?bbox=…`.
- At least **IMGW warnings** and one civil-alert source (RCB or RSO) as real data.
- Everything runs with `docker compose up`, and CI runs on every push.

## 9. Success metrics

- **Portfolio:** a reviewer can clone the repo, run one command and see a working map within 5 minutes.
- **Freshness:** an event is visible in the UI less than 2 min after the source publishes it (for polled sources).
- **Extensibility:** a new source needs one class plus a test, with no changes elsewhere.
- **Quality:** CI is green, and the core domain and every source adapter are covered by tests.

## 10. Legal & ethics

- A clear disclaimer on every page: *"Not an official warning system. In an emergency follow the
  instructions of the authorities (RCB, 112)."*
- Respect each source's terms of use, licences (e.g. ODbL for some ADS-B data) and rate limits.
  Cache responses and identify the client with a `User-Agent`.
- Store no personal data in the MVP. The user's location stays in the browser.
- Never present unverified information as fact.
