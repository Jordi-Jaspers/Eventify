import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/admin-users/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/admin-users');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Admin Users Page Screenshots', () => {
    test.setTimeout(30000);

    test.beforeEach(async ({ page }) => {
        // Login first using dev credentials button (admin user)
        await page.goto('/login');
        await page.waitForLoadState('domcontentloaded');

        // Wait for dev credentials to load and click "Fill Credentials" button
        const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
        await fillButton.waitFor({ state: 'visible', timeout: 15000 });
        await fillButton.click();

        // Submit login form
        await page.getByRole('button', { name: 'Sign In' }).click();

        // Wait for redirect to dashboard (login success)
        await page.waitForURL('/dashboard', { timeout: 15000 });

        // Navigate to admin users page
        await page.goto('/admin/users');
        await page.waitForLoadState('domcontentloaded');

        // Wait for page to settle and data to load
        await page.waitForTimeout(1000);
    });

    test('users table default state', async ({ page }, testInfo) => {
        const screenshotPath: string = getScreenshotPath('01-default', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('user details sheet opened', async ({ page }, testInfo) => {
        // Click on first user row to open sheet
        const userRow = page.locator('[role="button"]').filter({ hasText: /@/ }).first();
        await userRow.waitFor({ state: 'visible', timeout: 5000 });
        await userRow.click();
        await page.waitForTimeout(500); // Wait for sheet animation

        const screenshotPath: string = getScreenshotPath('02-user-details-sheet', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });
});
