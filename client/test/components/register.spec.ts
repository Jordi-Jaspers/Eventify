import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/register/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/register');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Register Page Screenshots', () => {
    test.setTimeout(30000);

    test.beforeEach(async ({ page }) => {
        await page.goto('/register');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
    });

    test.describe('Dark Mode', () => {
        test.beforeEach(async ({ page }) => {
            // Set dark mode (emulateMedia applies retroactively to already-loaded page)
            await page.emulateMedia({ colorScheme: 'dark' });
            await page.waitForTimeout(100); // Let theme change apply
        });

        test('default state - empty form', async ({ page }, testInfo) => {
            const screenshotPath: string = getScreenshotPath('01-default-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with data filled', async ({ page }, testInfo) => {
            await page.getByLabel('First Name').fill('John');
            await page.getByLabel('Last Name').fill('Doe');
            await page.getByLabel('Email').fill('john.doe@example.com');
            await page.locator('#password').fill('SecurePassword123!');
            await page.locator('#passwordConfirmation').fill('SecurePassword123!');

            const screenshotPath: string = getScreenshotPath('02-filled-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password strength indicator - weak password', async ({ page }, testInfo) => {
            await page.locator('#password').fill('weak');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('03-password-weak-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password strength indicator - strong password', async ({ page }, testInfo) => {
            await page.locator('#password').fill('SecurePassword123!');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('04-password-strong-dark', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('passwords match indicator', async ({ page }, testInfo) => {
            await page.locator('#password').fill('SecurePassword123!');
            await page.locator('#passwordConfirmation').fill('SecurePassword123!');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('05-passwords-match-dark', testInfo.project.name);

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
            const screenshotPath: string = getScreenshotPath('01-default-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('form with data filled', async ({ page }, testInfo) => {
            await page.getByLabel('First Name').fill('John');
            await page.getByLabel('Last Name').fill('Doe');
            await page.getByLabel('Email').fill('john.doe@example.com');
            await page.locator('#password').fill('SecurePassword123!');
            await page.locator('#passwordConfirmation').fill('SecurePassword123!');

            const screenshotPath: string = getScreenshotPath('02-filled-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password strength indicator - weak password', async ({ page }, testInfo) => {
            await page.locator('#password').fill('weak');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('03-password-weak-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('password strength indicator - strong password', async ({ page }, testInfo) => {
            await page.locator('#password').fill('SecurePassword123!');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('04-password-strong-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });

        test('passwords match indicator', async ({ page }, testInfo) => {
            await page.locator('#password').fill('SecurePassword123!');
            await page.locator('#passwordConfirmation').fill('SecurePassword123!');
            await page.waitForTimeout(300);

            const screenshotPath: string = getScreenshotPath('05-passwords-match-light', testInfo.project.name);

            await page.screenshot({
                path: screenshotPath,
                fullPage: true
            });

            expect(existsSync(screenshotPath)).toBeTruthy();
            console.log(`Screenshot saved: ${screenshotPath}`);
        });
    });
});
