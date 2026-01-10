/**
 * Dashboard Page Screenshot Tests
 *
 * Tests the main dashboard page in both dark and light modes.
 * This is an authenticated page - requires login.
 */
import { test, expect, setTheme, loginAndNavigate } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'dashboard';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Dashboard Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/dashboard');
			});

			test(`default state with organizations`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
