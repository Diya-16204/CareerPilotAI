import { defineConfig, devices } from '@playwright/test';
const browserChannel = process.env.PLAYWRIGHT_BROWSER === 'msedge' ? 'msedge' : undefined;
const appBaseURL = process.env.CAREERPILOT_E2E_BASE_URL ?? 'http://127.0.0.1:4173';
export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: Boolean(process.env.CI),
    retries: process.env.CI ? 1 : 0,
    reporter: 'list',
    use: { baseURL: appBaseURL, trace: 'retain-on-failure', screenshot: 'only-on-failure' },
    projects: [{ name: browserChannel ?? 'chromium', use: { ...devices['Desktop Chrome'], channel: browserChannel } }],
    webServer: process.env.CAREERPILOT_E2E_BASE_URL ? undefined : { command: 'npm run dev -- --host 127.0.0.1 --port 4173', url: 'http://127.0.0.1:4173', reuseExistingServer: !process.env.CI, timeout: 30_000 },
});

//import {defineConfig, devices} from '@playwright/test';: Imports Playwright's config helper for autocomplete and built-in device emulation profiles.
//
// const browserChannel = ...: Checks the PLAYWRIGHT_BROWSER environment variable to run tests in Microsoft Edge (msedge) or default Chromium.
//
// const appBaseURL = ...: Sets the target application URL, defaulting to [http://127.0.0.1:4173](http://127.0.0.1:4173) if CAREERPILOT_E2E_BASE_URL is not defined.
//
// export default defineConfig({ ... }): Exports the master configuration object governing Playwright End-to-End (E2E) test runs.
//
// testDir: './e2e': Directs Playwright to locate and execute test files inside the ./e2e directory.
//
// fullyParallel: true: Executes test cases across files concurrently to speed up test run times.
//
// forbidOnly: Boolean(process.env.CI): Prevents accidental commits of focused tests by failing CI builds if test.only is left in the code.
//
// retries: process.env.CI ? 1 : 0: Grants 1 automatic retry attempt for failed tests in CI environments while keeping retries off locally.
//
// reporter: 'list': Outputs test execution status line-by-line in terminal list format.
//
// use: { ... }: Specifies global test defaults, capturing diagnostic traces and screenshots exclusively on test failure.
//
// projects: [...]: Configures test run environments (Desktop Chrome or Microsoft Edge based on browserChannel).
//
// webServer: ...: Automatically boots the local Vite application server (npm run dev) on port 4173 if no pre-existing E2E URL was supplied.
