interface EventSummaryProps {
  count: number | undefined
  isLoading: boolean
  error: Error | null
}

export function EventSummary({ count, isLoading, error }: EventSummaryProps) {
  if (isLoading) return <p>Loading events…</p>
  if (error) return <p role="alert">Could not load events. Retrying…</p>
  return <p>{count === 1 ? '1 active event' : `${count ?? 0} active events`}</p>
}
