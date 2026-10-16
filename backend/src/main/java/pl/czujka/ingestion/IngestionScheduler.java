package pl.czujka.ingestion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pl.czujka.event.EventRepository;
import pl.czujka.event.ThreatEvent;

/**
 * Periodically polls every {@link EventSource} and stores the events it returns.
 */
@Component
@ConditionalOnBooleanProperty(name = "czujka.ingestion.enabled", matchIfMissing = true)
class IngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    private final List<EventSource> sources;
    private final EventRepository repository;

    IngestionScheduler(List<EventSource> sources, EventRepository repository) {
        this.sources = sources;
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${czujka.ingestion.interval:PT1M}")
    void ingestAll() {
        for (EventSource source : sources) {
            ingest(source);
        }
    }

    private void ingest(EventSource source) {
        try {
            List<ThreatEvent> events = source.fetch();
            events.forEach(repository::upsert);
            log.info("Ingested {} events from '{}'", events.size(), source.name());
        } catch (RuntimeException e) {
            log.warn("Ingestion from '{}' failed: {}", source.name(), e.getMessage(), e);
        }
    }
}
