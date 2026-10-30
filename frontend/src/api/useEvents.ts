import { useQuery } from '@tanstack/react-query'
import { fetchEvents } from './events'

const REFRESH_INTERVAL_MS = 30_000

/** Active events, refreshed every 30 seconds. */
export function useEvents() {
  return useQuery({
    queryKey: ['events'],
    queryFn: fetchEvents,
    refetchInterval: REFRESH_INTERVAL_MS,
  })
}
