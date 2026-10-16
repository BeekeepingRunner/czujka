import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { EventSummary } from './EventSummary'

describe('EventSummary', () => {
  it('shows the number of active events', () => {
    render(<EventSummary count={3} isLoading={false} error={null} />)
    expect(screen.getByText('3 active events')).toBeInTheDocument()
  })

  it('shows an alert when loading failed', () => {
    render(<EventSummary count={undefined} isLoading={false} error={new Error('boom')} />)
    expect(screen.getByRole('alert')).toHaveTextContent('Could not load events')
  })
})
