import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // In development the backend runs on :8080; in Docker nginx does this proxying.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    // e2e/ holds the Playwright smoke test, which runs against the full stack (npm run test:e2e)
    include: ['src/**/*.test.{ts,tsx}'],
  },
})
