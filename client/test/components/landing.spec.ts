/**
 * Landing Page Screenshot Tests
 *
 * Simplified tests capturing full page layouts for design validation.
 * Full-page screenshots capture all sections; individual section tests removed.
 */
import { test, setTheme, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME: string = 'landing';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Landing Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	// Desktop full-page tests (captures all sections)
	for (const theme of THEMES) {
		test(`desktop ${theme} - full page`, async ({ page }, testInfo) => {
			await setTheme(page, theme);
			await page.goto('/');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			await page.screenshot({
				path: getScreenshot(`desktop-${theme}`, testInfo.project.name),
				fullPage: true
			});
		});
	}

	// Mobile full-page tests
	for (const theme of THEMES) {
		test(`mobile ${theme} - full page`, async ({ page }, testInfo) => {
			await page.setViewportSize({ width: 375, height: 667 });
			await setTheme(page, theme);
			await page.goto('/');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			await page.screenshot({
				path: getScreenshot(`mobile-${theme}`, testInfo.project.name),
				fullPage: true
			});
		});
	}

});
