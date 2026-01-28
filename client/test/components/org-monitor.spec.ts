/**
 * Organization Monitor Page Screenshot Tests
 *
 * Tests the organization monitor page with unified timeline visualization
 * in both dark and light modes.
 * This is an authenticated page requiring organization membership.
 *
 * Note: These tests assume org ID 1 and watchlist ID 1 exist. If not, the page will show
 * a "not found" or "no watchlist" state which is also a valid screenshot to capture.
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

const PAGE_NAME: string = 'org-monitor';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Monitor Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	// Use org ID 1 and watchlist ID 1 for testing
	const orgId = '1';
	const watchlistId = '1';

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
			});

			test(`default layout`, async ({ page }, testInfo) => {
				await loginAndNavigate(page, `/organizations/${orgId}/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2); // Extra time for API call

				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`watchlist selector dropdown`, async ({ page }, testInfo) => {
				await loginAndNavigate(page, `/organizations/${orgId}/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Try to open watchlist selector dropdown
				const selectorButton = page.locator('button').filter({ hasText: /.*/ }).first();
				const isVisible = await selectorButton.isVisible().catch(() => false);

				if (isVisible) {
					await selectorButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`02-watchlist-selector-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`configure popover open`, async ({ page }, testInfo) => {
				await loginAndNavigate(page, `/organizations/${orgId}/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Try to open configure popover
				const configureButton = page.getByRole('button', { name: /Configure/i }).first();
				const isVisible = await configureButton.isVisible().catch(() => false);

				if (isVisible) {
					await configureButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`03-configure-open-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`no watchlist selected state`, async ({ page }, testInfo) => {
				// Navigate without a watchlist ID to see empty state
				await loginAndNavigate(page, `/organizations/${orgId}/watchlists/monitor`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				await page.screenshot({
					path: getScreenshot(`04-no-watchlist-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
