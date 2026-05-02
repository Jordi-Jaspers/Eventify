/**
 * Public Navigation Screenshot Tests
 *
 * Validates shared public navbar across landing and pricing pages.
 */
import { test, setTheme, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME: string = 'public-nav';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Public Navigation Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	// Landing page - mobile navigation open
	for (const theme of THEMES) {
		test(`landing mobile ${theme} - navigation open`, async ({ page }, testInfo) => {
			await page.setViewportSize({ width: 375, height: 667 });
			await setTheme(page, theme);
			await page.goto('/');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			const menuButton = page.locator('button[aria-label="Toggle menu"]');
			await menuButton.click();
			await page.waitForTimeout(ANIMATION_SETTLE_MS);

			await page.screenshot({
				path: getScreenshot(`landing-mobile-nav-${theme}`, testInfo.project.name),
				fullPage: false
			});
		});
	}

	// Pricing page - mobile navigation open
	for (const theme of THEMES) {
		test(`pricing mobile ${theme} - navigation open`, async ({ page }, testInfo) => {
			await page.setViewportSize({ width: 375, height: 667 });
			await setTheme(page, theme);
			await page.goto('/pricing');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			const menuButton = page.locator('button[aria-label="Toggle menu"]');
			await menuButton.click();
			await page.waitForTimeout(ANIMATION_SETTLE_MS);

			await page.screenshot({
				path: getScreenshot(`pricing-mobile-nav-${theme}`, testInfo.project.name),
				fullPage: false
			});
		});
	}
});
