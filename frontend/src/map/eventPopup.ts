import type { ThreatEvent } from '../api/events'

/**
 * Builds popup content with DOM APIs (textContent) instead of HTML strings,
 * because event texts come from external sources and must never be rendered as HTML.
 */
export function eventPopupContent(event: ThreatEvent): HTMLElement {
  const root = document.createElement('div')
  root.className = 'event-popup'

  const title = document.createElement('h3')
  title.textContent = event.title
  root.append(title)

  const details = [
    `Severity: ${event.severity}`,
    `Category: ${event.category}`,
    `Since: ${new Date(event.startsAt).toLocaleString('pl-PL')}`,
    `Source: ${event.source}`,
  ]
  if (event.description) {
    details.unshift(event.description)
  }
  for (const text of details) {
    const line = document.createElement('p')
    line.textContent = text
    root.append(line)
  }
  return root
}
