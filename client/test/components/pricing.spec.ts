/**
 * Pricing Page Screenshot Tests
 *
 * Captures full page layouts for design validation across themes and viewports.
 */
import { test, setTheme, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';
import { expect } from '@playwright/test';

const PAGE_NAME: string = 'pricing';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Pricing Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	// Desktop full-page tests
	for (const theme of THEMES) {
		test(`desktop ${theme} - full page`, async ({ page }, testInfo) => {
			await setTheme(page, theme);
			await page.goto('/pricing');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			const tiers = page.locator('[data-testid="pricing-tier"]');
			await expect(tiers).toHaveCount(3);

			await page.screenshot({
				path: getScreenshot(`desktop-${theme}`, testInfo.project.name),
				fullPage: true
			});
		});
	}

	// Mobile full-page tests
	for (const theme of THEMES) {
		test(`mobile ${theme} - full page`, async ({ page }, testInfo) => {
			await page.setViewportSize({ width: 375, height: 667 });
			await setTheme(page, theme);
			await page.goto('/pricing');
			await page.waitForLoadState('domcontentloaded');
			await page.waitForTimeout(DATA_LOAD_MS);

			const tiers = page.locator('[data-testid="pricing-tier"]');
			await expect(tiers).toHaveCount(3);

			await page.screenshot({
				path: getScreenshot(`mobile-${theme}`, testInfo.project.name),
				fullPage: true
			});
		});
	}
});
