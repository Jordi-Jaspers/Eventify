/**
 * Monitor Zoom Navigation Screenshot Tests
 *
 * Tests the LOD (Level of Detail) zoom navigation feature on the monitor page:
 * - Zoom on aggregated segment click
 * - Breadcrumb trail rendering and navigation
 * - LOD resolution indicator in TimeAxisHeader
 * - Mixed-severity stripe rendering on aggregated segments
 *
 * This is an authenticated page - requires login and a watchlist.
 * These tests will FAIL until the zoom navigation feature is implemented (TDD).
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

const PAGE_NAME = 'monitor-zoom';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Monitor Zoom Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	const watchlistId = '1';

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
			});

			test(`default layout with LOD resolution indicator`, async ({ page }, testInfo) => {
				// Given: Monitor page loaded with aggregated data (30d or 7d range)
				await loginAndNavigate(page, `/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// When: Page displays aggregated data with non-null bucketSize
				// Then: LOD resolution indicator should be visible in TimeAxisHeader
				// (e.g., "4h resolution" or "2h resolution")

				// Open ConfigurePopover and select 30d range to ensure aggregated data
				const configureButton = page.getByRole('button', { name: /Configure/i }).first();
				const configureVisible = await configureButton.isVisible().catch(() => false);
				if (configureVisible) {
					await configureButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					// Select 30d time range to trigger LOD aggregation
					const thirtyDayToggle = page.locator('[data-value="30d"], button:has-text("30d")').first();
					const toggleVisible = await thirtyDayToggle.isVisible().catch(() => false);
					if (toggleVisible) {
						await thirtyDayToggle.click();
						await page.waitForTimeout(DATA_LOAD_MS * 2);
					}

					// Close popover by pressing Escape
					await page.keyboard.press('Escape');
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				// Verify LOD resolution indicator is present
				const lodIndicator = page.locator('text=/\\d+[hmd]\\s*resolution/i');
				await expect(lodIndicator.first()).toBeVisible({ timeout: ELEMENT_WAIT_TIMEOUT_MS });

				await page.screenshot({
					path: getScreenshot(`01-lod-indicator-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`zoom in on aggregated segment click`, async ({ page }, testInfo) => {
				// Given: Monitor page with aggregated data (segments have bucketSize)
				await loginAndNavigate(page, `/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Ensure we're on a range that produces aggregated data
				const configureButton = page.getByRole('button', { name: /Configure/i }).first();
				const configureVisible = await configureButton.isVisible().catch(() => false);
				if (configureVisible) {
					await configureButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					const thirtyDayToggle = page.locator('[data-value="30d"], button:has-text("30d")').first();
					const toggleVisible = await thirtyDayToggle.isVisible().catch(() => false);
					if (toggleVisible) {
						await thirtyDayToggle.click();
						await page.waitForTimeout(DATA_LOAD_MS * 2);
					}

					await page.keyboard.press('Escape');
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				// When: Click an aggregated segment (should zoom in, not open modal)
				const segment = page.locator('button[aria-label*="segment"]').first();
				await segment.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await segment.click();
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Then: Breadcrumb trail should appear above the timeline
				const breadcrumb = page.locator('[data-testid="zoom-breadcrumb"]');
				await expect(breadcrumb).toBeVisible({ timeout: ELEMENT_WAIT_TIMEOUT_MS });

				// Then: No modal should be open (zoom happened instead)
				const modal = page.locator('[role="dialog"]');
				await expect(modal).not.toBeVisible();

				await page.screenshot({
					path: getScreenshot(`02-zoomed-with-breadcrumb-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`zoom breadcrumb navigation back to overview`, async ({ page }, testInfo) => {
				// Given: Monitor page zoomed in (breadcrumbs visible)
				await loginAndNavigate(page, `/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Set 30d range for aggregated data
				const configureButton = page.getByRole('button', { name: /Configure/i }).first();
				const configureVisible = await configureButton.isVisible().catch(() => false);
				if (configureVisible) {
					await configureButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					const thirtyDayToggle = page.locator('[data-value="30d"], button:has-text("30d")').first();
					const toggleVisible = await thirtyDayToggle.isVisible().catch(() => false);
					if (toggleVisible) {
						await thirtyDayToggle.click();
						await page.waitForTimeout(DATA_LOAD_MS * 2);
					}

					await page.keyboard.press('Escape');
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				// Zoom in by clicking a segment
				const segment = page.locator('button[aria-label*="segment"]').first();
				await segment.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await segment.click();
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Verify breadcrumbs are visible after zoom
				const breadcrumb = page.locator('[data-testid="zoom-breadcrumb"]');
				await expect(breadcrumb).toBeVisible({ timeout: ELEMENT_WAIT_TIMEOUT_MS });

				// When: Click the first breadcrumb (overview level) to zoom out
				const overviewCrumb = breadcrumb.locator('button, a').first();
				await overviewCrumb.click();
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Then: Breadcrumbs should disappear (back at overview level)
				await expect(breadcrumb).not.toBeVisible();

				await page.screenshot({
					path: getScreenshot(`03-zoomed-out-via-breadcrumb-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`mixed-severity stripe rendering on aggregated segments`, async ({ page }, testInfo) => {
				// Given: Monitor page with aggregated data that may contain mixed severities
				await loginAndNavigate(page, `/watchlists/monitor?id=${watchlistId}`);
				await page.waitForTimeout(DATA_LOAD_MS * 2);

				// Ensure aggregated view (30d range)
				const configureButton = page.getByRole('button', { name: /Configure/i }).first();
				const configureVisible = await configureButton.isVisible().catch(() => false);
				if (configureVisible) {
					await configureButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					const thirtyDayToggle = page.locator('[data-value="30d"], button:has-text("30d")').first();
					const toggleVisible = await thirtyDayToggle.isVisible().catch(() => false);
					if (toggleVisible) {
						await thirtyDayToggle.click();
						await page.waitForTimeout(DATA_LOAD_MS * 2);
					}

					await page.keyboard.press('Escape');
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				// Then: Aggregated segments should have cursor-zoom-in and potential stripe pattern
				// Visual verification — capture current state for screenshot comparison
				const segments = page.locator('button[aria-label*="segment"]');
				await expect(segments.first()).toBeVisible({ timeout: ELEMENT_WAIT_TIMEOUT_MS });

				await page.screenshot({
					path: getScreenshot(`04-stripe-rendering-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
