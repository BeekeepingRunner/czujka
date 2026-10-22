CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE threat_event
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source      VARCHAR(50)            NOT NULL,
    external_id VARCHAR(255)           NOT NULL,
    category    VARCHAR(30)            NOT NULL,
    severity    VARCHAR(20)            NOT NULL,
    title       VARCHAR(500)           NOT NULL,
    description TEXT,
    location    geometry(Point, 4326)  NOT NULL,
    starts_at   TIMESTAMPTZ            NOT NULL,
    ends_at     TIMESTAMPTZ,
    url         VARCHAR(2000),
    created_at  TIMESTAMPTZ            NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ            NOT NULL DEFAULT now(),
    CONSTRAINT uq_threat_event_source_external_id UNIQUE (source, external_id)
);

CREATE INDEX idx_threat_event_location ON threat_event USING GIST (location);
CREATE INDEX idx_threat_event_starts_at ON threat_event (starts_at DESC);
