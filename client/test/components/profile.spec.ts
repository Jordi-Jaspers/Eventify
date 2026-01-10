/**
 * Profile Page Screenshot Tests
 *
 * Tests the user profile page in both dark and light modes.
 * This is an authenticated page - requires login.
 */
import { test, expect, setTheme, loginAndNavigate } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'profile';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Profile Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/profile');
			});

			test(`default state - profile view`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`editable field focused`, async ({ page }, testInfo) => {
				// Try to click an edit button if available
				const pencilButtons = page.locator('button:has(svg.lucide-pencil)');
				const count = await pencilButtons.count();

				if (count > 0) {
					await pencilButtons.first().click();
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`02-edit-mode-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
