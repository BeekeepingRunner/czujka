package pl.czujka.ingestion.mock;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import pl.czujka.event.EventCategory;
import pl.czujka.event.Severity;
import pl.czujka.event.ThreatEvent;
import pl.czujka.ingestion.EventSource;

/**
 * Fixed sample events used until real sources are integrated. Disable it with {@code czujka.sources.mock.enabled=false}.
 */
@Component
@ConditionalOnBooleanProperty(name = "czujka.sources.mock.enabled")
class MockEventSource implements EventSource {

    private static final String NAME = "mock";
    private static final String DISCLAIMER = "Sample event — not real data.";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public List<ThreatEvent> fetch() {
        Instant now = Instant.now();
        Instant inTwoHours = now.plus(Duration.ofHours(2));
        return List.of(
                event("lublin-air", EventCategory.AIR_THREAT, Severity.HIGH,
                        "Air threat — Lublin Voivodeship", 51.2465, 22.5684, now, inTwoHours),
                event("rzeszow-alert", EventCategory.CIVIL_ALERT, Severity.MEDIUM,
                        "RCB alert — Podkarpackie Voivodeship", 50.0413, 21.9990, now, inTwoHours),
                event("krakow-storm", EventCategory.WEATHER, Severity.MEDIUM,
                        "Thunderstorm warning, level 2", 50.0647, 19.9450, now, inTwoHours),
                event("wroclaw-flood", EventCategory.FLOOD, Severity.LOW,
                        "Hydrological warning — Odra river", 51.1079, 17.0385, now, inTwoHours),
                event("warszawa-air-quality", EventCategory.AIR_QUALITY, Severity.LOW,
                        "Poor air quality", 52.2297, 21.0122, now, inTwoHours));
    }

    private static ThreatEvent event(String id, EventCategory category, Severity severity, String title,
                                     double latitude, double longitude, Instant startsAt, Instant endsAt) {
        return new ThreatEvent(NAME, id, category, severity, title, DISCLAIMER,
                latitude, longitude, startsAt, endsAt, null);
    }
}
