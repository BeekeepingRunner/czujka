package pl.czujka.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import pl.czujka.TestcontainersConfiguration;

/**
 * Runs against a real PostGIS database, because the spatial and time filtering happens in SQL.
 * Timestamps are fixed far in the past or future, so "active" never depends on when the tests run.
 */
@SpringBootTest(properties = "czujka.ingestion.enabled=false")
@Import(TestcontainersConfiguration.class)
class EventRepositoryTest {

    private static final Instant STARTED = Instant.parse("2026-10-16T10:00:00Z");
    private static final Instant ENDS_IN_FUTURE = Instant.parse("2100-01-01T00:00:00Z");
    private static final Instant ENDED_IN_PAST = Instant.parse("2000-01-01T00:00:00Z");

    @Autowired
    EventRepository repository;

    @Autowired
    JdbcClient jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.sql("DELETE FROM threat_event").update();
    }

    @Test
    void storesAndReadsBackEveryField() {
        ThreatEvent event = new ThreatEvent("rcb", "alert-1", EventCategory.CIVIL_ALERT, Severity.CRITICAL,
                "Zagrożenie — województwo łódzkie", "Pozostań w domu, śledź komunikaty.",
                51.759445, 19.457216, STARTED, ENDS_IN_FUTURE, "https://example.org/alert/1");

        repository.upsert(event);

        assertThat(repository.findActiveIn(BoundingBox.POLAND)).containsExactly(event);
    }

    @Test
    void storesEventWithoutOptionalFields() {
        ThreatEvent event = new ThreatEvent("test", "minimal", EventCategory.OTHER, Severity.LOW, "Minimal",
                null, 52.0, 19.0, STARTED, null, null);

        repository.upsert(event);

        assertThat(repository.findActiveIn(BoundingBox.POLAND)).containsExactly(event);
    }

    @Test
    void returnsOnlyEventsInsideTheBox() {
        repository.upsert(event("lublin", 51.25, 22.57));
        repository.upsert(event("berlin", 52.52, 13.40));
        repository.upsert(event("vilnius", 54.69, 25.28));

        assertThat(externalIds(repository.findActiveIn(BoundingBox.POLAND))).containsExactly("lublin");
    }

    @Test
    void treatsTheBoxEdgesAsInside() {
        BoundingBox box = new BoundingBox(14.0, 49.0, 24.0, 55.0);
        repository.upsert(event("south-west-corner", 49.0, 14.0));
        repository.upsert(event("north-east-corner", 55.0, 24.0));
        repository.upsert(event("just-outside-east", 52.0, 24.0001));
        repository.upsert(event("just-outside-north", 55.0001, 19.0));

        assertThat(externalIds(repository.findActiveIn(box)))
                .containsExactlyInAnyOrder("south-west-corner", "north-east-corner");
    }

    @Test
    void returnsEventsWithoutEndAndEventsEndingInTheFuture() {
        repository.upsert(event("open-ended", 52.0, 19.0, STARTED, null));
        repository.upsert(event("ends-later", 52.0, 19.0, STARTED, ENDS_IN_FUTURE));
        repository.upsert(event("already-ended", 52.0, 19.0, STARTED, ENDED_IN_PAST));

        assertThat(externalIds(repository.findActiveIn(BoundingBox.POLAND)))
                .containsExactlyInAnyOrder("open-ended", "ends-later");
    }

    @Test
    void ordersEventsNewestFirst() {
        repository.upsert(event("middle", 52.0, 19.0, STARTED, null));
        repository.upsert(event("newest", 52.0, 19.0, STARTED.plusSeconds(60), null));
        repository.upsert(event("oldest", 52.0, 19.0, STARTED.minusSeconds(60), null));

        assertThat(externalIds(repository.findActiveIn(BoundingBox.POLAND)))
                .containsExactly("newest", "middle", "oldest");
    }

    @Test
    void returnsAtMost500NewestEvents() {
        // 501 events, e1 is the oldest; inserted in one statement to keep the test fast
        jdbc.sql("""
                INSERT INTO threat_event (source, external_id, category, severity, title, location, starts_at)
                SELECT 'bulk', 'e' || i, 'OTHER', 'LOW', 'Event ' || i, ST_Point(19.0, 52.0, 4326),
                       TIMESTAMPTZ '2026-01-01 00:00:00Z' + i * INTERVAL '1 minute'
                FROM generate_series(1, 501) AS i
                """).update();

        List<String> ids = externalIds(repository.findActiveIn(BoundingBox.POLAND));

        assertThat(ids).hasSize(500).startsWith("e501").endsWith("e2").doesNotContain("e1");
    }

    @Test
    void upsertReplacesAllFieldsOfAnExistingEvent() {
        repository.upsert(event("storm", 52.0, 19.0));
        ThreatEvent updated = new ThreatEvent("test", "storm", EventCategory.WEATHER, Severity.CRITICAL,
                "Updated title", "Updated description", 50.06, 19.94, STARTED.plusSeconds(3600),
                ENDS_IN_FUTURE, "https://example.org/updated");

        repository.upsert(updated);

        assertThat(repository.findActiveIn(BoundingBox.POLAND)).containsExactly(updated);
        assertThat(rowCount()).isEqualTo(1);
    }

    @Test
    void upsertCanEndAnEvent() {
        repository.upsert(event("alert", 52.0, 19.0, STARTED, null));

        repository.upsert(event("alert", 52.0, 19.0, STARTED, ENDED_IN_PAST));

        assertThat(repository.findActiveIn(BoundingBox.POLAND)).isEmpty();
        assertThat(rowCount()).isEqualTo(1);
    }

    @Test
    void upsertCanMoveAnEventOutOfTheBox() {
        repository.upsert(event("moving", 52.0, 19.0));

        repository.upsert(event("moving", 52.52, 13.40));

        assertThat(repository.findActiveIn(BoundingBox.POLAND)).isEmpty();
    }

    @Test
    void sameExternalIdFromDifferentSourcesIsTwoEvents() {
        repository.upsert(new ThreatEvent("imgw", "1", EventCategory.WEATHER, Severity.LOW, "Storm", null,
                52.0, 19.0, STARTED, null, null));
        repository.upsert(new ThreatEvent("rcb", "1", EventCategory.CIVIL_ALERT, Severity.HIGH, "Alert", null,
                52.0, 19.0, STARTED, null, null));

        assertThat(repository.findActiveIn(BoundingBox.POLAND))
                .extracting(ThreatEvent::source)
                .containsExactlyInAnyOrder("imgw", "rcb");
    }

    private int rowCount() {
        return jdbc.sql("SELECT count(*) FROM threat_event").query(Integer.class).single();
    }

    private static List<String> externalIds(List<ThreatEvent> events) {
        return events.stream().map(ThreatEvent::externalId).toList();
    }

    private static ThreatEvent event(String id, double latitude, double longitude) {
        return event(id, latitude, longitude, STARTED, ENDS_IN_FUTURE);
    }

    private static ThreatEvent event(String id, double latitude, double longitude, Instant startsAt, Instant endsAt) {
        return new ThreatEvent("test", id, EventCategory.AIR_THREAT, Severity.HIGH, "Event " + id, null,
                latitude, longitude, startsAt, endsAt, null);
    }
}
