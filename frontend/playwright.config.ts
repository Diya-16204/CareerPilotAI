import { defineConfig, devices } from '@playwright/test'

const browserChannel = process.env.PLAYWRIGHT_BROWSER === 'msedge' ? 'msedge' : undefined
const appBaseURL = process.env.CAREERPILOT_E2E_BASE_URL ?? 'http://127.0.0.1:4173'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: { baseURL: appBaseURL, trace: 'retain-on-failure', screenshot: 'only-on-failure' },
  projects: [{ name: browserChannel ?? 'chromium', use: { ...devices['Desktop Chrome'], channel: browserChannel } }],
  webServer: process.env.CAREERPILOT_E2E_BASE_URL ? undefined : { command: 'npm run dev -- --host 127.0.0.1 --port 4173', url: 'http://127.0.0.1:4173', reuseExistingServer: !process.env.CI, timeout: 30_000 },
})
