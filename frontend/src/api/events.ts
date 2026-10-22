// Mirrors the backend record pl.czujka.event.ThreatEvent.

export type EventCategory = 'AIR_THREAT' | 'CIVIL_ALERT' | 'WEATHER' | 'FLOOD' | 'AIR_QUALITY' | 'OTHER'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface ThreatEvent {
  source: string
  externalId: string
  category: EventCategory
  severity: Severity
  title: string
  description: string | null
  latitude: number
  longitude: number
  startsAt: string
  endsAt: string | null
  url: string | null
}

export async function fetchEvents(): Promise<ThreatEvent[]> {
  const response = await fetch('/api/events')
  if (!response.ok) {
    throw new Error(`Failed to load events: HTTP ${response.status}`)
  }
  return response.json()
}
