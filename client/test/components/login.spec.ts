import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Screenshots go to screenshots/login/ folder
const screenshotsDir = join(__dirname, '../resources/screenshots/login');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Login Page Screenshots', () => {
    // Increase timeout for cold start (first test needs more time for dev server initialization)
    test.setTimeout(30000);

    test.beforeEach(async ({ page }) => {
        // Navigate to login page
        await page.goto('/login');
        // Wait for the page content to be loaded (avoid networkidle due to HMR websocket)
        await page.waitForLoadState('domcontentloaded');
        // Wait for any animations/transitions to settle
        await page.waitForTimeout(500);
    });

    test.describe('Dark Mode', () => {
        test.beforeEach(async ({ page }) => {
            // Set dark mode (emulateMedia applies retroactively to already-loaded page)
            await page.emulateMedia({ colorScheme: 'dark' });
            await page.waitForTimeout(100); // Let theme change apply
        });

        test('default state - empty form', async ({ page }, testInfo) => {
            const screenshotPath = getScreenshotPath('01-default-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with email filled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');

            const screenshotPath = getScreenshotPath('02-with-email-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with credentials filled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');
            await page.locator('#password').fill('SecurePassword123!');

            const screenshotPath = getScreenshotPath('03-filled-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password visibility toggled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');
            await page.locator('#password').fill('SecurePassword123!');
            await page.getByRole('button', { name: 'Show password' }).click();

            const screenshotPath = getScreenshotPath('04-password-visible-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('focused email input', async ({ page }, testInfo) => {
            await page.getByLabel('Email').focus();

            const screenshotPath = getScreenshotPath('05-email-focused-dark', testInfo.project.name);

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

        test('default state - empty form', async ({ page }, testInfo) => {
            const screenshotPath = getScreenshotPath('01-default-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with email filled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');

            const screenshotPath = getScreenshotPath('02-with-email-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with credentials filled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');
            await page.locator('#password').fill('SecurePassword123!');

            const screenshotPath = getScreenshotPath('03-filled-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password visibility toggled', async ({ page }, testInfo) => {
            await page.getByLabel('Email').fill('user@example.com');
            await page.locator('#password').fill('SecurePassword123!');
            await page.getByRole('button', { name: 'Show password' }).click();

            const screenshotPath = getScreenshotPath('04-password-visible-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('focused email input', async ({ page }, testInfo) => {
            await page.getByLabel('Email').focus();

            const screenshotPath = getScreenshotPath('05-email-focused-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });
});
