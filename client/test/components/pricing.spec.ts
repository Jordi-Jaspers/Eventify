/**
 * Pricing Page Screenshot Tests
 *
 * Tests the pricing page with 3 tier cards across desktop/mobile and dark/light themes.
 */
import { expect, test } from '@playwright/test';

const PAGE_NAME = 'pricing';
const screenshotDir = `test/resources/screenshots/${PAGE_NAME}`;

test.describe('Pricing Page Screenshots', () => {
    test.setTimeout(30000);

    test('has 3 pricing tiers', async ({ page }) => {
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        const tiers = page.locator('[data-testid="pricing-tier"]');
        await expect(tiers).toHaveCount(3);
    });

    test('has Popular badge on Pro tier', async ({ page }) => {
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await expect(page.getByText('Popular')).toBeVisible();
    });

    test('desktop dark - full page', async ({ page }) => {
        await page.emulateMedia({ colorScheme: 'dark' });
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/01-desktop-dark.png`,
            fullPage: true
        });
    });

    test('desktop light - full page', async ({ page }) => {
        await page.emulateMedia({ colorScheme: 'light' });
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/02-desktop-light.png`,
            fullPage: true
        });
    });

    test('mobile dark - full page', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 812 });
        await page.emulateMedia({ colorScheme: 'dark' });
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/03-mobile-dark.png`,
            fullPage: true
        });
    });

    test('mobile light - full page', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 812 });
        await page.emulateMedia({ colorScheme: 'light' });
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/04-mobile-light.png`,
            fullPage: true
        });
    });
});
