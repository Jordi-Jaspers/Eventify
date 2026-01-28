/**
 * Organization Watchlists Screenshot Tests
 *
 * Tests the organization watchlists page in both dark and light modes.
 * This is an authenticated page requiring organization membership or global admin.
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

const PAGE_NAME: string = 'org-watchlists';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Watchlists Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				// Navigate to org watchlists page - using org ID 1 (created in test seed data)
				await loginAndNavigate(page, '/organizations/1/watchlists');
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

				// Capture the watchlists list - either populated or empty state
				await page.screenshot({
					path: getScreenshot(`02-watchlists-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`new watchlist button hover`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Look for new watchlist button (visible to OWNER/ADMIN)
				const newButton = page.getByRole('button', { name: /New Watchlist/i });
				const isVisible = await newButton.isVisible().catch(() => false);

				if (isVisible) {
					const box = await newButton.boundingBox();
					if (box) {
						await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`03-new-button-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`watchlist actions`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Try to hover over first watchlist row to show actions
				const firstRow = page.locator('[class*="hover:bg-muted"]').first();
				const rowExists = await firstRow.isVisible().catch(() => false);

				if (rowExists) {
					await firstRow.hover();
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`04-watchlist-actions-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`search functionality`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Look for search input
				const searchInput = page.getByPlaceholder(/Search watchlists/i);
				const isVisible = await searchInput.isVisible().catch(() => false);

				if (isVisible) {
					await searchInput.fill('test');
					await page.waitForTimeout(500);
				}

				await page.screenshot({
					path: getScreenshot(`05-search-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
