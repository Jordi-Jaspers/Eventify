/**
 * Notification Bell Screenshot Tests
 *
 * Tests the notification bell icon, unread badge, sheet panel open,
 * and user dropdown with What's New item in both dark and light modes.
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

const PAGE_NAME = 'notification-bell';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Notification Bell Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/dashboard');
			});

			test(`bell icon in sidebar`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-bell-icon-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`notification panel open`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				const bellButton = page.getByRole('button', { name: /Notifications/i });
				await bellButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await bellButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-panel-open-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`user dropdown with whats new`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Open the user dropdown - click the ChevronsUpDown button area in sidebar footer
				const userMenuButton = page.locator('[data-sidebar="footer"] button[data-size="lg"]').first();
				await userMenuButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await userMenuButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`03-user-dropdown-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
