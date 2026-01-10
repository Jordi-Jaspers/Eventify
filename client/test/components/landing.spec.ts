/**
 * Landing Page Screenshot Tests
 *
 * Tests the landing/home page in both dark and light modes.
 * This is a public page - no authentication required.
 */
import { test, expect, setTheme, ANIMATION_SETTLE_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'landing';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Landing Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await page.goto('/');
				await page.waitForLoadState('domcontentloaded');
				await setTheme(page, theme);
				await page.waitForTimeout(ANIMATION_SETTLE_MS);
			});

			test(`default state`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
