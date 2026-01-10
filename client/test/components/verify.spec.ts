/**
 * Verify Email Page Screenshot Tests
 *
 * Tests the email verification page in both dark and light modes.
 * This is a public page - no authentication required.
 * Note: This page shows an error state when accessed without a token.
 */
import { test, expect, setTheme } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'verify';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Verify Email Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test(`default state - no token (error state)`, async ({ page }, testInfo) => {
				// Set theme BEFORE navigation for this page
				await setTheme(page, theme);

				// Navigate without token - will show error state briefly before redirect
				await page.goto('/verify');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`01-no-token-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
