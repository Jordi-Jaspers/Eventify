import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Screenshots folder: test/resources/screenshots/org-settings/
const screenshotsDir = join(__dirname, '../resources/screenshots/org-settings');
if (!existsSync(screenshotsDir)) {
	mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
	const suffix = projectName.replace(/\s+/g, '-').toLowerCase();
	return join(screenshotsDir, `${name}-${suffix}.png`);
}

async function loginAndNavigate(page: import('@playwright/test').Page): Promise<void> {
	// Go to login page
	await page.goto('/login');
	await page.waitForLoadState('domcontentloaded');

	// Wait for dev credentials button and click it
	const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
	await fillButton.waitFor({ state: 'visible', timeout: 10000 });
	await fillButton.click();

	// Submit login form
	await page.getByRole('button', { name: 'Sign In' }).click();

	// Wait for redirect to dashboard (login success)
	await page.waitForURL('/dashboard', { timeout: 15000 });

	// Navigate to first organization's settings (orgId=1)
	await page.goto('/organizations/1/settings');
	await page.waitForLoadState('domcontentloaded');
	await page.waitForTimeout(1000);
}

test.describe('Organization Settings Screenshots', () => {
	// Increase timeout since login adds time
	test.setTimeout(30000);

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await loginAndNavigate(page);
		});

		test('01 - general tab', async ({ page }, testInfo) => {
			// Should redirect to /general
			await page.waitForURL('/organizations/1/settings/general', { timeout: 5000 });
			await page.waitForTimeout(500);

			const screenshotPath = getScreenshotPath('01-general-dark', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('02 - api keys tab', async ({ page }, testInfo) => {
			// Navigate to API keys using link
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(1000);

			const screenshotPath = getScreenshotPath('02-api-keys-dark', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('03 - create api key modal', async ({ page }, testInfo) => {
			// Navigate to API keys
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(500);

			// Click Create API Key button
			await page.getByRole('button', { name: 'Create API Key' }).click();
			await page.waitForTimeout(300);

			const screenshotPath = getScreenshotPath('03-create-modal-dark', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('04 - create and revoke api key', async ({ page }, testInfo) => {
			// Navigate to API keys
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(500);

			// Click Create API Key button
			await page.getByRole('button', { name: 'Create API Key' }).click();
			await page.waitForTimeout(300);

			// Fill in the key name
			const keyName = `Key ${Date.now()}`;
			await page.locator('#key-name').fill(keyName);

			// Click Create Key button (within the sheet) - scope to the sheet
			const sheet = page.getByLabel('Create API Key');
			await sheet.getByRole('button', { name: 'Create Key' }).click();

			// Wait for success modal
			await page.waitForSelector('text=API Key Created', { timeout: 5000 });

			// Screenshot of key created modal
			const keyCreatedPath = getScreenshotPath('04-key-created-modal-dark', testInfo.project.name);
			await page.screenshot({ path: keyCreatedPath, fullPage: true });
			expect(existsSync(keyCreatedPath)).toBeTruthy();

			// Close the modal
			await page.getByRole('button', { name: 'Done' }).click();
			await page.waitForTimeout(500);

			// Screenshot of keys list
			const keysListPath = getScreenshotPath('05-api-keys-list-dark', testInfo.project.name);
			await page.screenshot({ path: keysListPath, fullPage: true });
			expect(existsSync(keysListPath)).toBeTruthy();
		});
	});

	test.describe('Light Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set light mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'light' });
			await loginAndNavigate(page);
		});

		test('01 - general tab', async ({ page }, testInfo) => {
			// Should redirect to /general
			await page.waitForURL('/organizations/1/settings/general', { timeout: 5000 });
			await page.waitForTimeout(500);

			const screenshotPath = getScreenshotPath('01-general-light', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('02 - api keys tab', async ({ page }, testInfo) => {
			// Navigate to API keys using link
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(1000);

			const screenshotPath = getScreenshotPath('02-api-keys-light', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('03 - create api key modal', async ({ page }, testInfo) => {
			// Navigate to API keys
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(500);

			// Click Create API Key button
			await page.getByRole('button', { name: 'Create API Key' }).click();
			await page.waitForTimeout(300);

			const screenshotPath = getScreenshotPath('03-create-modal-light', testInfo.project.name);
			await page.screenshot({ path: screenshotPath, fullPage: true });
			expect(existsSync(screenshotPath)).toBeTruthy();
		});

		test('04 - create and revoke api key', async ({ page }, testInfo) => {
			// Navigate to API keys
			const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
			await apiKeysLink.click();
			await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
			await page.waitForTimeout(500);

			// Click Create API Key button
			await page.getByRole('button', { name: 'Create API Key' }).click();
			await page.waitForTimeout(300);

			// Fill in the key name
			const keyName = `Key ${Date.now()}`;
			await page.locator('#key-name').fill(keyName);

			// Click Create Key button (within the sheet) - scope to the sheet
			const sheet = page.getByLabel('Create API Key');
			await sheet.getByRole('button', { name: 'Create Key' }).click();

			// Wait for success modal
			await page.waitForSelector('text=API Key Created', { timeout: 5000 });

			// Screenshot of key created modal
			const keyCreatedPath = getScreenshotPath('04-key-created-modal-light', testInfo.project.name);
			await page.screenshot({ path: keyCreatedPath, fullPage: true });
			expect(existsSync(keyCreatedPath)).toBeTruthy();

			// Close the modal
			await page.getByRole('button', { name: 'Done' }).click();
			await page.waitForTimeout(500);

			// Screenshot of keys list
			const keysListPath = getScreenshotPath('05-api-keys-list-light', testInfo.project.name);
			await page.screenshot({ path: keysListPath, fullPage: true });
			expect(existsSync(keysListPath)).toBeTruthy();
		});
	});
});
