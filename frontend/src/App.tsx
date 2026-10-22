import { useEvents } from './api/useEvents'
import { EventSummary } from './EventSummary'
import { MapView } from './map/MapView'

export default function App() {
  const { data: events, isLoading, error } = useEvents()

  return (
    <div className="app">
      <header className="app-header">
        <h1>Czujka</h1>
        <EventSummary count={events?.length} isLoading={isLoading} error={error} />
      </header>
      <div className="disclaimer">
        Not an official warning system. In an emergency follow the instructions of the authorities (RCB, 112).
      </div>
      <MapView events={events ?? []} />
    </div>
  )
}
