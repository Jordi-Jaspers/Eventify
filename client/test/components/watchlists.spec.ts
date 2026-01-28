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

			test(`watchlist list view`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Capture the watchlists content - either list or empty state
				await page.screenshot({
					path: getScreenshot(`02-watchlists-content-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create watchlist button`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Look for create button and hover if visible
				const createButton = page.getByRole('button', { name: /New Watchlist|Create/i }).first();
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible) {
					const box = await createButton.boundingBox();
					if (box) {
						await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`03-create-button-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
