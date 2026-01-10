import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const screenshotsDir = join(__dirname, '../resources/screenshots/admin-api-keys');
if (!existsSync(screenshotsDir)) {
	mkdirSync(screenshotsDir, { recursive: true });
}

function screenshotPath(name: string, project: string): string {
	return join(screenshotsDir, `${name}-${project.replace(/\s+/g, '-').toLowerCase()}.png`);
}

async function loginAndNavigate(page: import('@playwright/test').Page): Promise<void> {
	// Login flow
	await page.goto('/login');
	await page.waitForLoadState('domcontentloaded');

	// Use dev credentials button
	const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
	await fillButton.waitFor({ state: 'visible', timeout: 10000 });
	await fillButton.click();

	// Submit
	await page.getByRole('button', { name: 'Sign In' }).click();

	// Wait for redirect
	await page.waitForURL('/dashboard', { timeout: 15000 });

	// Navigate to admin API keys page
	await page.goto('/admin/api-keys');
	await page.waitForLoadState('domcontentloaded');
	await page.waitForTimeout(500);
}

test.describe('Admin API Keys Screenshots', () => {
	test.setTimeout(30000); // Allow time for login

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await loginAndNavigate(page);
		});

		test('default state', async ({ page }, testInfo) => {
			const path = screenshotPath('01-default-dark', testInfo.project.name);
			await page.screenshot({ path, fullPage: true });
			expect(existsSync(path)).toBeTruthy();
		});

		test('stats cards loaded', async ({ page }, testInfo) => {
			// Wait for stats to load
			await page.waitForSelector('text=Total Keys', { timeout: 5000 });
			const path = screenshotPath('02-stats-loaded-dark', testInfo.project.name);
			await page.screenshot({ path, fullPage: true });
			expect(existsSync(path)).toBeTruthy();
		});

		test('revoke dialog', async ({ page }, testInfo) => {
			// Wait for table to load
			await page.waitForSelector('text=All API Keys', { timeout: 5000 });
			await page.waitForTimeout(1000);

			// Try to find and click the first actions button
			const actionsButton = page.locator('[aria-label="Actions"]').first();
			if ((await actionsButton.count()) > 0) {
				await actionsButton.click();
				await page.waitForTimeout(300);

				// Click revoke option
				const revokeOption = page.getByText('Revoke Key');
				if ((await revokeOption.count()) > 0) {
					await revokeOption.click();
					await page.waitForTimeout(500);

					const path = screenshotPath('03-revoke-dialog-dark', testInfo.project.name);
					await page.screenshot({ path, fullPage: true });
					expect(existsSync(path)).toBeTruthy();
				}
			}
		});
	});

	test.describe('Light Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set light mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'light' });
			await loginAndNavigate(page);
		});

		test('default state', async ({ page }, testInfo) => {
			const path = screenshotPath('01-default-light', testInfo.project.name);
			await page.screenshot({ path, fullPage: true });
			expect(existsSync(path)).toBeTruthy();
		});

		test('stats cards loaded', async ({ page }, testInfo) => {
			// Wait for stats to load
			await page.waitForSelector('text=Total Keys', { timeout: 5000 });
			const path = screenshotPath('02-stats-loaded-light', testInfo.project.name);
			await page.screenshot({ path, fullPage: true });
			expect(existsSync(path)).toBeTruthy();
		});

		test('revoke dialog', async ({ page }, testInfo) => {
			// Wait for table to load
			await page.waitForSelector('text=All API Keys', { timeout: 5000 });
			await page.waitForTimeout(1000);

			// Try to find and click the first actions button
			const actionsButton = page.locator('[aria-label="Actions"]').first();
			if ((await actionsButton.count()) > 0) {
				await actionsButton.click();
				await page.waitForTimeout(300);

				// Click revoke option
				const revokeOption = page.getByText('Revoke Key');
				if ((await revokeOption.count()) > 0) {
					await revokeOption.click();
					await page.waitForTimeout(500);

					const path = screenshotPath('03-revoke-dialog-light', testInfo.project.name);
					await page.screenshot({ path, fullPage: true });
					expect(existsSync(path)).toBeTruthy();
				}
			}
		});
	});
});
