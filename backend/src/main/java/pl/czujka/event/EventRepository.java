package pl.czujka.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {

    private static final int MAX_RESULTS = 500;

    private final JdbcClient jdbc;

    EventRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Inserts the event, or updates it if an event with the same source and external id already exists.
     */
    public void upsert(ThreatEvent event) {
        jdbc.sql("""
                        INSERT INTO threat_event (source, external_id, category, severity, title, description,
                                                  location, starts_at, ends_at, url)
                        VALUES (:source, :externalId, :category, :severity, :title, :description,
                                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :startsAt, :endsAt, :url)
                        ON CONFLICT (source, external_id) DO UPDATE SET
                            category    = EXCLUDED.category,
                            severity    = EXCLUDED.severity,
                            title       = EXCLUDED.title,
                            description = EXCLUDED.description,
                            location    = EXCLUDED.location,
                            starts_at   = EXCLUDED.starts_at,
                            ends_at     = EXCLUDED.ends_at,
                            url         = EXCLUDED.url,
                            updated_at  = now()
                        """)
                .param("source", event.source())
                .param("externalId", event.externalId())
                .param("category", event.category().name())
                .param("severity", event.severity().name())
                .param("title", event.title())
                .param("description", event.description())
                .param("longitude", event.longitude())
                .param("latitude", event.latitude())
                .param("startsAt", toOffset(event.startsAt()))
                .param("endsAt", toOffset(event.endsAt()))
                .param("url", event.url())
                .update();
    }

    /**
     * Returns events located inside the box that have not ended yet, newest first.
     */
    public List<ThreatEvent> findActiveIn(BoundingBox box) {
        return jdbc.sql("""
                        SELECT source, external_id, category, severity, title, description,
                               ST_Y(location) AS latitude, ST_X(location) AS longitude, starts_at, ends_at, url
                        FROM threat_event
                        WHERE location && ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
                          AND (ends_at IS NULL OR ends_at > now())
                        ORDER BY starts_at DESC
                        LIMIT :limit
                        """)
                .param("minLon", box.minLon())
                .param("minLat", box.minLat())
                .param("maxLon", box.maxLon())
                .param("maxLat", box.maxLat())
                .param("limit", MAX_RESULTS)
                .query(EventRepository::mapRow)
                .list();
    }

    private static ThreatEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ThreatEvent(
                rs.getString("source"),
                rs.getString("external_id"),
                EventCategory.valueOf(rs.getString("category")),
                Severity.valueOf(rs.getString("severity")),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                toInstant(rs.getObject("starts_at", OffsetDateTime.class)),
                toInstant(rs.getObject("ends_at", OffsetDateTime.class)),
                rs.getString("url"));
    }

    private static OffsetDateTime toOffset(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    private static Instant toInstant(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant();
    }
}
