import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for screenshot testing.
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
    testDir: './test',
    outputDir: './test/resources/test-results',
    timeout: 5000,
    fullyParallel: false,
    retries: 2,
    workers: 2,
    reporter: [['html', { outputFolder: './test/resources/report', open: 'never' }], ['list']],
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure'
    },
    projects: [
        {
            name: 'Desktop Chrome',
            use: { ...devices['Desktop Chrome'] }
        }
    ],
    webServer: {
        command: 'bun run dev',
        url: 'http://localhost:5173',
        timeout: 120 * 1000,
        reuseExistingServer: true
    }
});
