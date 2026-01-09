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

test.describe('Organization Settings Screenshots', () => {
	// Increase timeout since login adds time
	test.setTimeout(15000);

	test.beforeEach(async ({ page }) => {
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
	});

	test('01 - general tab', async ({ page }, testInfo) => {
		// Should redirect to /general
		await page.waitForURL('/organizations/1/settings/general', { timeout: 5000 });
		await page.waitForTimeout(500);

		const screenshotPath = getScreenshotPath('01-general', testInfo.project.name);
		await page.screenshot({ path: screenshotPath, fullPage: true });
		expect(existsSync(screenshotPath)).toBeTruthy();
	});

	test('02 - api keys tab empty', async ({ page }, testInfo) => {
		// Navigate to API keys
		await page.click('text=API Keys');
		await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
		await page.waitForTimeout(1000);

		const screenshotPath = getScreenshotPath('02-api-keys-empty', testInfo.project.name);
		await page.screenshot({ path: screenshotPath, fullPage: true });
		expect(existsSync(screenshotPath)).toBeTruthy();
	});

	test('03 - create api key modal', async ({ page }, testInfo) => {
		// Navigate to API keys
		await page.click('text=API Keys');
		await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
		await page.waitForTimeout(500);

		// Click Create API Key button
		await page.getByRole('button', { name: 'Create API Key' }).click();
		await page.waitForTimeout(300);

		const screenshotPath = getScreenshotPath('03-create-modal', testInfo.project.name);
		await page.screenshot({ path: screenshotPath, fullPage: true });
		expect(existsSync(screenshotPath)).toBeTruthy();
	});

	test('04 - create api key success', async ({ page }, testInfo) => {
		// Navigate to API keys
		await page.click('text=API Keys');
		await page.waitForURL('/organizations/1/settings/api-keys', { timeout: 5000 });
		await page.waitForTimeout(500);

		// Click Create API Key button
		await page.getByRole('button', { name: 'Create API Key' }).click();
		await page.waitForTimeout(300);

		// Fill in the key name
		const keyName = `Key to Revoke ${Date.now()}`;
		await page.locator('#key-name').fill(keyName);
		
		// Click Create Key button (within the sheet) - scope to the sheet
		const sheet = page.getByLabel('Create API Key');
		await sheet.getByRole('button', { name: 'Create Key' }).click();

		// Wait for success modal and close it
		await page.waitForSelector('text=API Key Created', { timeout: 5000 });
		await page.getByRole('button', { name: 'Done' }).click();
		await page.waitForTimeout(500);

		// Find the key row in the main content - use the grid structure
		// Each row is a grid with the key name in a p.font-medium element
		const mainContent = page.locator('main');
		const keyRow = mainContent.locator('div.grid').filter({ hasText: keyName }).first();
		await expect(keyRow).toBeVisible();

		// The revoke button is a small outline button with a trash icon in the last column
		const revokeButton = keyRow.locator('button[data-slot="button"]').filter({ has: page.locator('svg') });
		await revokeButton.click();
		await page.waitForTimeout(300);

		// Take screenshot of the revoke confirmation dialog
		const confirmScreenshotPath = getScreenshotPath('06-revoke-confirm', testInfo.project.name);
		await page.screenshot({ path: confirmScreenshotPath, fullPage: true });
		expect(existsSync(confirmScreenshotPath)).toBeTruthy();

		// Verify the confirmation dialog shows the key name
		await expect(page.getByRole('alertdialog')).toBeVisible();
		await expect(page.getByRole('alertdialog').getByText('Revoke API Key')).toBeVisible();

		// Click Revoke Key to confirm
		await page.getByRole('button', { name: 'Revoke Key' }).click();
		await page.waitForTimeout(500);

		// Verify the key is no longer in the list
		await expect(mainContent.getByText(keyName)).not.toBeVisible();

		// Take screenshot of the list after revocation
		const afterRevokeScreenshotPath = getScreenshotPath('07-after-revoke', testInfo.project.name);
		await page.screenshot({ path: afterRevokeScreenshotPath, fullPage: true });
		expect(existsSync(afterRevokeScreenshotPath)).toBeTruthy();
	});
});
