import type { FeatureCollection, Point } from 'geojson'
import type { ThreatEvent, Severity } from '../api/events'

export const SEVERITY_COLORS: Record<Severity, string> = {
  LOW: '#2f81f7',
  MEDIUM: '#d29922',
  HIGH: '#f0730a',
  CRITICAL: '#da3633',
}

/** Converts events to GeoJSON so MapLibre can draw them as a layer. */
export function toFeatureCollection(events: ThreatEvent[]): FeatureCollection<Point, ThreatEvent> {
  return {
    type: 'FeatureCollection',
    features: events.map((event) => ({
      type: 'Feature',
      id: `${event.source}:${event.externalId}`,
      geometry: { type: 'Point', coordinates: [event.longitude, event.latitude] },
      properties: event,
    })),
  }
}
