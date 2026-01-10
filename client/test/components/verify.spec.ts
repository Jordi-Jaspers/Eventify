import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/verify/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/verify');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Verify Email Page Screenshots', () => {
    test.setTimeout(30000);

    test.describe('Dark Mode', () => {
        test('default state - no token (error state)', async ({ page }, testInfo) => {
            // Set dark mode BEFORE navigation
            await page.emulateMedia({ colorScheme: 'dark' });

            // Navigate without token - will show error state briefly before redirect
            await page.goto('/verify');
            await page.waitForLoadState('domcontentloaded');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('01-no-token-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });

    test.describe('Light Mode', () => {
        test('default state - no token (error state)', async ({ page }, testInfo) => {
            // Set light mode BEFORE navigation
            await page.emulateMedia({ colorScheme: 'light' });

            // Navigate without token - will show error state briefly before redirect
            await page.goto('/verify');
            await page.waitForLoadState('domcontentloaded');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('01-no-token-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });
});
