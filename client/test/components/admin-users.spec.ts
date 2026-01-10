/**
 * Admin Users Page Screenshot Tests
 *
 * Tests the admin users page in both dark and light modes.
 * This is an admin page - requires login with admin privileges.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'admin-users';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Admin Users Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/admin/users', DATA_LOAD_MS);
			});

			test(`users table default state`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`user details sheet opened`, async ({ page }, testInfo) => {
				// Click on first user row to open sheet
				const userRow = page.locator('[role="button"]').filter({ hasText: /@/ }).first();
				await userRow.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await userRow.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-user-details-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
