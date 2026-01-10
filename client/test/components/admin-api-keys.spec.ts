/**
 * Admin API Keys Page Screenshot Tests
 *
 * Tests the admin API keys page in both dark and light modes.
 * This is an admin page - requires login with admin privileges.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'admin-api-keys';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Admin API Keys Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/admin/api-keys', ANIMATION_SETTLE_MS);
			});

			test(`default state`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`stats cards loaded`, async ({ page }, testInfo) => {
				await page.waitForSelector('text=Total Keys', { timeout: ELEMENT_WAIT_TIMEOUT_MS });

				await page.screenshot({
					path: getScreenshot(`02-stats-loaded-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`revoke dialog`, async ({ page }, testInfo) => {
				await page.waitForSelector('text=All API Keys', { timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await page.waitForTimeout(DATA_LOAD_MS);

				// Try to find and click the first actions button
				const actionsButton = page.locator('[aria-label="Actions"]').first();
				if ((await actionsButton.count()) > 0) {
					await actionsButton.click();
					await page.waitForTimeout(300);

					// Click revoke option
					const revokeOption = page.getByText('Revoke Key');
					if ((await revokeOption.count()) > 0) {
						await revokeOption.click();
						await page.waitForTimeout(ANIMATION_SETTLE_MS);
					}
				}

				await page.screenshot({
					path: getScreenshot(`03-revoke-dialog-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
