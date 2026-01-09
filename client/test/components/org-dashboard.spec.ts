import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/org-dashboard/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/org-dashboard');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Organization Dashboard Page Screenshots', () => {
    test.setTimeout(30000);

    test.beforeEach(async ({ page }) => {
        // Login first using dev credentials button
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

        // Navigate to organization dashboard (org ID 1 = Acme Corporation)
        await page.goto('/organizations/1/dashboard');
        await page.waitForLoadState('domcontentloaded');

        // Wait for page to settle
        await page.waitForTimeout(800);
    });

    test('default state', async ({ page }, testInfo) => {
        const screenshotPath: string = getScreenshotPath('01-default', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });
});
