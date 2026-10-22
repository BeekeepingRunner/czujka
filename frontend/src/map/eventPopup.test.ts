import { describe, expect, it } from 'vitest'
import type { ThreatEvent } from '../api/events'
import { eventPopupContent } from './eventPopup'
import { toFeatureCollection } from './eventFeatures'

const event: ThreatEvent = {
  source: 'test',
  externalId: '1',
  category: 'AIR_THREAT',
  severity: 'HIGH',
  title: '<img src=x onerror=alert(1)>',
  description: null,
  latitude: 51.25,
  longitude: 22.57,
  startsAt: '2026-10-16T10:00:00Z',
  endsAt: null,
  url: null,
}

describe('eventPopupContent', () => {
  it('renders external text as plain text, never as HTML', () => {
    const content = eventPopupContent(event)
    expect(content.querySelector('img')).toBeNull()
    expect(content.querySelector('h3')?.textContent).toBe(event.title)
  })
})

describe('toFeatureCollection', () => {
  it('puts longitude first, as GeoJSON requires', () => {
    const [feature] = toFeatureCollection([event]).features
    expect(feature.geometry.coordinates).toEqual([22.57, 51.25])
    expect(feature.id).toBe('test:1')
  })
})
