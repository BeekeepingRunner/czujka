package pl.czujka.ingestion.mock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.czujka.event.BoundingBox;
import pl.czujka.event.ThreatEvent;

class MockEventSourceTest {

    private final MockEventSource source = new MockEventSource();
    private final List<ThreatEvent> events = source.fetch();

    @Test
    void returnsEventsTaggedWithItsName() {
        assertThat(events).isNotEmpty().allSatisfy(e -> assertThat(e.source()).isEqualTo(source.name()));
    }

    @Test
    void usesStableUniqueExternalIdsSoRefetchingUpdatesInsteadOfDuplicating() {
        List<String> ids = events.stream().map(ThreatEvent::externalId).toList();

        assertThat(ids).doesNotHaveDuplicates();
        assertThat(source.fetch()).extracting(ThreatEvent::externalId).containsExactlyElementsOf(ids);
    }

    @Test
    void placesEveryEventInPoland() {
        BoundingBox poland = BoundingBox.POLAND;

        assertThat(events).allSatisfy(e -> {
            assertThat(e.longitude()).isBetween(poland.minLon(), poland.maxLon());
            assertThat(e.latitude()).isBetween(poland.minLat(), poland.maxLat());
        });
    }

    @Test
    void returnsCurrentlyActiveEvents() {
        Instant now = Instant.now();

        assertThat(events).allSatisfy(e -> {
            assertThat(e.startsAt()).isBeforeOrEqualTo(now);
            assertThat(e.endsAt()).isAfter(now);
        });
    }
}
