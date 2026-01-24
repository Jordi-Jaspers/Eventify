/**
 * Watchlists Page Screenshot Tests
 *
 * Tests the watchlists page in both dark and light modes.
 * This is an authenticated page for personal user watchlists.
 */
import {
	test,
	expect,
	setTheme,
	loginAndNavigate,
	ANIMATION_SETTLE_MS,
	DATA_LOAD_MS
} from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME: string = 'watchlists';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Watchlists Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/watchlists');
			});

			test(`page layout`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`watchlist row hover`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Wait for data table to load
				const watchlistRow = page.locator('[class*="grid"][class*="rounded-lg"]').first();
				await watchlistRow.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

				// Hover over first watchlist row
				await watchlistRow.hover();
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`02-watchlist-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`empty state`, async ({ page }, testInfo) => {
				// Note: This test might not show empty state if test data exists
				// It's here as a placeholder for when empty state testing is available
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`03-empty-state-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
