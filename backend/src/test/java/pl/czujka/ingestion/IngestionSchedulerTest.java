package pl.czujka.ingestion;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import pl.czujka.event.EventCategory;
import pl.czujka.event.EventRepository;
import pl.czujka.event.Severity;
import pl.czujka.event.ThreatEvent;

class IngestionSchedulerTest {

    private final EventRepository repository = mock(EventRepository.class);

    @Test
    void storesEveryEventFromEverySource() {
        ThreatEvent storm = event("imgw", "storm");
        ThreatEvent flood = event("imgw", "flood");
        ThreatEvent alert = event("rcb", "alert");

        ingest(source("imgw", () -> List.of(storm, flood)), source("rcb", () -> List.of(alert)));

        verify(repository).upsert(storm);
        verify(repository).upsert(flood);
        verify(repository).upsert(alert);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void sourceWithoutEventsStoresNothing() {
        ingest(source("empty", List::of));

        verifyNoInteractions(repository);
    }

    @Test
    void worksWithoutAnySources() {
        ingest();

        verifyNoInteractions(repository);
    }

    @Test
    void failingSourceDoesNotStopOtherSources() {
        ThreatEvent event = event("working", "1");

        ingest(source("failing", () -> {
            throw new IllegalStateException("source is down");
        }), source("working", () -> List.of(event)));

        verify(repository).upsert(event);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void sourceReturningNullDoesNotStopOtherSources() {
        ThreatEvent event = event("working", "1");

        ingest(source("broken", () -> null), source("working", () -> List.of(event)));

        verify(repository).upsert(event);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void storageFailureForOneSourceDoesNotStopOtherSources() {
        ThreatEvent rejected = event("first", "1");
        ThreatEvent accepted = event("second", "1");
        doThrow(new DataAccessResourceFailureException("database unavailable")).when(repository).upsert(rejected);

        ingest(source("first", () -> List.of(rejected)), source("second", () -> List.of(accepted)));

        verify(repository).upsert(accepted);
    }

    private void ingest(EventSource... sources) {
        new IngestionScheduler(List.of(sources), repository).ingestAll();
    }

    private static ThreatEvent event(String source, String id) {
        return new ThreatEvent(source, id, EventCategory.WEATHER, Severity.LOW, "Event " + id, null,
                52.0, 19.0, Instant.parse("2026-10-16T10:00:00Z"), null, null);
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
