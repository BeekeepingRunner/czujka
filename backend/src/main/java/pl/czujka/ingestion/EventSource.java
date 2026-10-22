package pl.czujka.ingestion;

import java.util.List;

import pl.czujka.event.ThreatEvent;

/**
 * An external source of threat events, such as IMGW warnings or RCB alerts.
 * <p>
 * To add a new source, implement this interface as a Spring bean. {@link IngestionScheduler} finds it
 * automatically and polls it on every run.
 */
public interface EventSource {

    /**
     * Short, stable identifier stored with every event, e.g. {@code "imgw"}.
     */
    String name();

    /**
     * Fetches the current events from the source. May throw: a failing source is logged and skipped.
     */
    List<ThreatEvent> fetch();
}
