import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/forgot-password/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/forgot-password');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Forgot Password Page Screenshots', () => {
    test.setTimeout(30000);

    test.beforeEach(async ({ page }) => {
        await page.goto('/forgot-password');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
    });

    test.describe('Dark Mode', () => {
        test.beforeEach(async ({ page }) => {
            // Set dark mode (emulateMedia applies retroactively to already-loaded page)
            await page.emulateMedia({ colorScheme: 'dark' });
            await page.waitForTimeout(100); // Let theme change apply
        });

        test('default state', async ({ page }, testInfo) => {
            const screenshotPath: string = getScreenshotPath('01-default-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });

    test.describe('Light Mode', () => {
        test.beforeEach(async ({ page }) => {
            await page.emulateMedia({ colorScheme: 'light' });
            await page.waitForTimeout(100); // Let theme change apply
        });

        test('default state', async ({ page }, testInfo) => {
            const screenshotPath: string = getScreenshotPath('01-default-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });
});
