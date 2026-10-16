import { useEffect, useRef, useState } from 'react'
import { type GeoJSONSource, MapLibreMap, NavigationControl, Popup } from 'maplibre-gl'
import type { ThreatEvent } from '../api/events'
import { SEVERITY_COLORS, toFeatureCollection } from './eventFeatures'
import { eventPopupContent } from './eventPopup'

const BASE_STYLE = 'https://tiles.openfreemap.org/styles/positron'
const POLAND_CENTER: [number, number] = [19.1, 52.0]
const POLAND_ZOOM = 5.5

interface MapViewProps {
  events: ThreatEvent[]
}

export function MapView({ events }: MapViewProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [map, setMap] = useState<MapLibreMap | null>(null)

  // Create the map once, with a voivodeship layer and an empty events layer.
  useEffect(() => {
    const instance = new MapLibreMap({
      container: containerRef.current!,
      style: BASE_STYLE,
      center: POLAND_CENTER,
      zoom: POLAND_ZOOM,
    })
    instance.addControl(new NavigationControl(), 'top-right')

    instance.on('load', () => {
      instance.addSource('voivodeships', { type: 'geojson', data: '/voivodeships.geojson' })
      instance.addLayer({
        id: 'voivodeship-borders',
        type: 'line',
        source: 'voivodeships',
        paint: { 'line-color': '#59636e', 'line-width': 1 },
      })

      instance.addSource('events', { type: 'geojson', data: toFeatureCollection([]) })
      instance.addLayer({
        id: 'events',
        type: 'circle',
        source: 'events',
        paint: {
          'circle-radius': 9,
          'circle-color': [
            'match',
            ['get', 'severity'],
            'CRITICAL',
            SEVERITY_COLORS.CRITICAL,
            'HIGH',
            SEVERITY_COLORS.HIGH,
            'MEDIUM',
            SEVERITY_COLORS.MEDIUM,
            SEVERITY_COLORS.LOW,
          ],
          'circle-stroke-color': '#ffffff',
          'circle-stroke-width': 2,
        },
      })

      instance.on('click', 'events', (e) => {
        const feature = e.features?.[0]
        if (!feature) return
        new Popup()
          .setLngLat(e.lngLat)
          .setDOMContent(eventPopupContent(feature.properties as ThreatEvent))
          .addTo(instance)
      })
      instance.on('mouseenter', 'events', () => (instance.getCanvas().style.cursor = 'pointer'))
      instance.on('mouseleave', 'events', () => (instance.getCanvas().style.cursor = ''))

      setMap(instance)
    })

    return () => instance.remove()
  }, [])

  // Redraw events whenever new data arrives.
  useEffect(() => {
    map?.getSource<GeoJSONSource>('events')?.setData(toFeatureCollection(events))
  }, [map, events])

  return <div ref={containerRef} className="map" />
}
