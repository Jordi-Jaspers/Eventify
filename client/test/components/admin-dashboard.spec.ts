/**
 * Admin Dashboard Page Screenshot Tests
 *
 * Tests the admin dashboard page in both dark and light modes.
 * This is an admin page - requires login with admin privileges.
 */
import { test, expect, setTheme, loginAndNavigate } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'admin-dashboard';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

// Admin dashboard needs extra time for stats to load
const STATS_LOAD_MS = 1500;

test.describe('Admin Dashboard Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/admin/dashboard', STATS_LOAD_MS);
			});

			test(`default state with stats and chart`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
