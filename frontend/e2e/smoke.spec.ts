import { readFileSync } from 'node:fs'
import { expect, test } from '@playwright/test'

// The real base map comes from OpenFreeMap. CI must not depend on that server, so the style is replaced
// by a local stand-in and every other request that leaves the app is blocked and reported.
const BASE_STYLE_URL = 'https://tiles.openfreemap.org/styles/positron'
const baseStyle = readFileSync(new URL('./fixtures/base-style.json', import.meta.url), 'utf8')

test('the map loads with events from the backend', async ({ page, context, baseURL }) => {
  const appOrigin = new URL(baseURL!).origin
  const blockedRequests: string[] = []
  const failedResponses: string[] = []
  const errors: string[] = []

  // Context level, so requests from the MapLibre web worker are covered too.
  await context.route('**/*', (route) => {
    const url = route.request().url()
    if (url === BASE_STYLE_URL) {
      return route.fulfill({ contentType: 'application/json', body: baseStyle })
    }
    if (new URL(url).origin !== appOrigin) {
      blockedRequests.push(url)
      return route.abort()
    }
    return route.continue()
  })
  context.on('response', (response) => {
    if (response.status() >= 400) failedResponses.push(`${response.status()} ${response.url()}`)
  })
  page.on('pageerror', (error) => errors.push(error.message))
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text())
  })

  const workerResponse = context.waitForEvent('response', (r) => /maplibre-gl-worker-[^/]*\.mjs$/.test(r.url()))
  const eventsResponse = page.waitForResponse((r) => new URL(r.url()).pathname === '/api/events')
  const bordersResponse = context.waitForEvent('response', (r) => r.url().endsWith('/voivodeships.geojson'))

  await page.goto('/')

  // nginx must serve the module worker as JavaScript, otherwise the map stays blank.
  const worker = await workerResponse
  expect(worker.status()).toBe(200)
  expect(worker.headers()['content-type']).toMatch(/javascript/)

  // Backend, PostGIS and the nginx /api proxy work together.
  const events = await eventsResponse
  expect(events.status()).toBe(200)
  expect((await events.json()).length).toBeGreaterThan(0)
  await expect(page.getByText(/^[1-9]\d* active events?$/)).toBeVisible()

  // The map initialised (WebGL) and ran its load handler, which adds the voivodeship borders.
  await expect(page.locator('canvas.maplibregl-canvas')).toBeVisible()
  expect((await bordersResponse).status()).toBe(200)

  expect(errors, 'console errors').toEqual([])
  expect(failedResponses, 'failed requests').toEqual([])
  expect(blockedRequests, 'requests to external servers').toEqual([])
})
