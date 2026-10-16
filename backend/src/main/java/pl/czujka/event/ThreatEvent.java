package pl.czujka.event;

import java.time.Instant;

/**
 * A single potentially dangerous event, normalised from any external source.
 * The pair {@code (source, externalId)} identifies an event, so fetching it again updates it instead of duplicating it.
 */
public record ThreatEvent(
        String source,
        String externalId,
        EventCategory category,
        Severity severity,
        String title,
        String description,
        double latitude,
        double longitude,
        Instant startsAt,
        Instant endsAt,
        String url
) {
}
