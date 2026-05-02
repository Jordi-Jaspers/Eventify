/**
 * Public Navigation Screenshot Tests
 *
 * Tests the updated nav (Logo left, Pricing + Get Started right) on landing and pricing pages.
 */
import { test } from '@playwright/test';

const PAGE_NAME = 'public-nav';
const screenshotDir = `test/resources/screenshots/${PAGE_NAME}`;

test.describe('Public Navigation Screenshots', () => {
    test.setTimeout(30000);

    test('landing page nav - default (unauthenticated)', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/01-landing-nav.png`,
            clip: { x: 0, y: 0, width: 1280, height: 80 }
        });
    });

    test('landing page full - unauthenticated', async ({ page }) => {
        await page.goto('/');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/02-landing-full.png`,
            fullPage: true
        });
    });

    test('pricing page nav - active state', async ({ page }) => {
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/03-pricing-nav.png`,
            clip: { x: 0, y: 0, width: 1280, height: 80 }
        });
    });

    test('pricing page full - unauthenticated', async ({ page }) => {
        await page.goto('/pricing');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/04-pricing-full.png`,
            fullPage: true
        });
    });

    test('landing page mobile menu', async ({ page }) => {
        await page.setViewportSize({ width: 375, height: 812 });
        await page.goto('/');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);

        await page.screenshot({
            path: `${screenshotDir}/05-landing-mobile-closed.png`,
            fullPage: false
        });

        await page.getByRole('button', { name: 'Toggle menu' }).click();
        await page.waitForTimeout(300);

        await page.screenshot({
            path: `${screenshotDir}/06-landing-mobile-open.png`,
            fullPage: false
        });
    });
});
