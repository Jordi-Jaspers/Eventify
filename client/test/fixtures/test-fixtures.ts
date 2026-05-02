/**
 * Custom Playwright test fixtures for screenshot testing.
 *
 * These fixtures provide:
 * - Screenshot helpers bound to page context
 * - Theme management utilities
 * - Authentication helpers
 *
 * Usage:
 *   import { test, expect } from '../fixtures/test-fixtures';
 *
 * Each test file remains independent - fixtures are instantiated per-test.
 */
import type {Page} from '@playwright/test';
import {expect, test as base} from '@playwright/test';
import {
    ANIMATION_SETTLE_MS,
    COLD_START_TIMEOUT_MS,
    DATA_LOAD_MS,
    getScreenshotPath,
    loginAndNavigate,
    PAGE_SETTLE_MS,
    setTheme,
    type Theme
} from '../utils';

/**
 * Screenshot helper interface for taking consistent screenshots.
 */
export interface ScreenshotHelper {
    /**
     * Takes a full-page screenshot with consistent naming.
     * @param name - The screenshot name (e.g., '01-default', '02-filled')
     */
    capture(name: string): Promise<void>;
}

/**
 * Custom test fixtures interface.
 */
export interface TestFixtures {
    /** Helper for taking screenshots with consistent naming */
    screenshotHelper: ScreenshotHelper;

    /** The page name for screenshot organization (set via test.use()) */
    pageName: string;

    /** Current theme for the test */
    theme: Theme;
}

/**
 * Extended test with custom fixtures.
 */
export const test = base.extend<TestFixtures>({
    // Default page name - override in test files with test.use({ pageName: 'your-page' })
    pageName: ['unknown', {option: true}],

    // Default theme - override in describe blocks with test.use({ theme: 'dark' })
    theme: ['dark', {option: true}],

    // Auto-clear cookies before each test for clean state
    context: async ({context}, use) => {
        await context.clearCookies();
        await use(context);
    },

    // Screenshot helper that uses the page name and theme from fixtures
    screenshotHelper: async ({page, pageName, theme}, use, testInfo) => {
        await use({
            capture: async (name: string) => {
                const fullName = `${name}-${theme}`;
                const path = getScreenshotPath(pageName, fullName, testInfo.project.name);
                await page.screenshot({path, fullPage: true});
            }
        });
    }
});

export type {Page, Theme};
export {expect};
export {loginAndNavigate, setTheme};
export {ANIMATION_SETTLE_MS, COLD_START_TIMEOUT_MS, DATA_LOAD_MS, PAGE_SETTLE_MS};
