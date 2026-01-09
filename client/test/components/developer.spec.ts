import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/developer/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/developer');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('Developer Page Screenshots', () => {
    // Increased timeout to account for login + page load on first test
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
        
        // Navigate to developer page
        await page.goto('/developer');
        await page.waitForLoadState('domcontentloaded');
        
        // Wait for page to settle
        await page.waitForTimeout(800);
    });

    test('page layout and navigation', async ({ page }, testInfo) => {
        const screenshotPath: string = getScreenshotPath('01-layout', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('empty state visible', async ({ page }, testInfo) => {
        // Wait for loading to complete
        await page.waitForTimeout(1000);

        const screenshotPath: string = getScreenshotPath('02-empty-state', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('create API key button hover', async ({ page }, testInfo) => {
        // Find and hover over the create button
        const createButton = page.getByRole('button', { name: /new key|create key/i }).first();
        await createButton.waitFor({ state: 'visible', timeout: 5000 });
        await createButton.hover();
        await page.waitForTimeout(300);

        const screenshotPath: string = getScreenshotPath('03-button-hover', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('create API key sheet opened', async ({ page }, testInfo) => {
        // Click create API key button
        const createButton = page.getByRole('button', { name: /new key|create key/i }).first();
        await createButton.waitFor({ state: 'visible', timeout: 5000 });
        await createButton.click();
        await page.waitForTimeout(500); // Wait for sheet animation

        const screenshotPath: string = getScreenshotPath('04-create-sheet', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('create API key form filled', async ({ page }, testInfo) => {
        // Click create API key button
        const createButton = page.getByRole('button', { name: /new key|create key/i }).first();
        await createButton.waitFor({ state: 'visible', timeout: 5000 });
        await createButton.click();
        await page.waitForTimeout(500);

        // Fill in the form
        const nameInput = page.getByLabel('Key Name');
        await nameInput.waitFor({ state: 'visible', timeout: 5000 });
        await nameInput.fill('Production App');
        
        // Select expiration by clicking the "30 days" button (to show the change from default 90 days)
        const expirationButton = page.getByRole('button', { name: /30 days/i });
        await expirationButton.click();
        await page.waitForTimeout(300);

        const screenshotPath: string = getScreenshotPath('05-create-filled', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('settings navigation tabs', async ({ page }, testInfo) => {
        // Check if navigation tabs are present
        const profileLink = page.getByRole('link', { name: 'Profile' });
        await profileLink.waitFor({ state: 'visible', timeout: 5000 });
        
        // Hover over profile tab
        await profileLink.hover();
        await page.waitForTimeout(300);

        const screenshotPath: string = getScreenshotPath('06-navigation', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('create API key and see success modal', async ({ page }, testInfo) => {
        // Click create API key button
        const createButton = page.getByRole('button', { name: /new key|create key/i }).first();
        await createButton.waitFor({ state: 'visible', timeout: 5000 });
        await createButton.click();
        await page.waitForTimeout(500);

        // Fill in the form
        const nameInput = page.getByLabel('Key Name');
        await nameInput.waitFor({ state: 'visible', timeout: 5000 });
        await nameInput.fill('Test API Key');
        
        // Select expiration by clicking the "30 days" button
        const expirationButton = page.getByRole('button', { name: /30 days/i });
        await expirationButton.click();
        await page.waitForTimeout(300);

        // Click Create Key button (inside the sheet)
        const submitButton = page.getByLabel('Create API Key').getByRole('button', { name: 'Create Key' });
        await submitButton.click();

        // Wait for success modal to appear
        await page.waitForTimeout(1500);

        const screenshotPath: string = getScreenshotPath('07-key-created-modal', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('API key list with keys', async ({ page }, testInfo) => {
        // Wait for page to load with keys
        await page.waitForTimeout(1000);

        // Check if we have API keys displayed
        const keyCards = page.locator('[class*="Card"]').filter({ hasText: /evt_/ });
        
        const screenshotPath: string = getScreenshotPath('08-keys-list', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });

    test('revoke API key confirmation dialog', async ({ page }, testInfo) => {
        // Wait for keys to load
        await page.waitForTimeout(1000);

        // Find the trash icon button (revoke trigger) - it's inside the card header
        const trashButton = page.locator('button:has(svg.lucide-trash-2)').first();
        
        // Check if trash button exists
        const trashExists = await trashButton.isVisible().catch(() => false);
        
        if (trashExists) {
            await trashButton.click();
            await page.waitForTimeout(500); // Wait for dialog animation
        }

        const screenshotPath: string = getScreenshotPath('09-revoke-dialog', testInfo.project.name);

        await page.screenshot({
            path: screenshotPath,
            fullPage: true
        });

        expect(existsSync(screenshotPath)).toBeTruthy();
        console.log(`Screenshot saved: ${screenshotPath}`);
    });
});
