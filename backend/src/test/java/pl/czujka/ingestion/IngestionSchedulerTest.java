package pl.czujka.ingestion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import pl.czujka.event.EventCategory;
import pl.czujka.event.EventRepository;
import pl.czujka.event.Severity;
import pl.czujka.event.ThreatEvent;

class IngestionSchedulerTest {

    private final EventRepository repository = mock(EventRepository.class);

    @Test
    void failingSourceDoesNotStopOtherSources() {
        ThreatEvent event = new ThreatEvent("working", "1", EventCategory.WEATHER, Severity.LOW, "Storm", null,
                52.0, 19.0, Instant.now(), null, null);
        EventSource failing = source("failing", () -> {
            throw new IllegalStateException("source is down");
        });
        EventSource working = source("working", () -> List.of(event));

        new IngestionScheduler(List.of(failing, working), repository).ingestAll();

        verify(repository).upsert(event);
    }

    private static EventSource source(String name, Supplier<List<ThreatEvent>> fetch) {
        return new EventSource() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public List<ThreatEvent> fetch() {
                return fetch.get();
            }
        };
    }
}
