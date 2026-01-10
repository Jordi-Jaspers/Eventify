import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/admin-organizations/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/admin-organizations');
if (!existsSync(screenshotsDir)) {
	mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
	const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
	return join(screenshotsDir, `${name}-${suffix}.png`);
}

async function login(page: import('@playwright/test').Page): Promise<void> {
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
}

test.describe('Admin Organizations Page Screenshots', () => {
	test.setTimeout(30000);

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await login(page);
		});

		test('organizations table default state', async ({ page }, testInfo) => {
			// Navigate to admin organizations page
			await page.goto('/admin/organizations');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle and data to load
			await page.waitForTimeout(1000);

			const screenshotPath: string = getScreenshotPath('01-default-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create organization form - empty', async ({ page }, testInfo) => {
			// Navigate to create organization page
			await page.goto('/admin/organizations/new');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle
			await page.waitForTimeout(500);

			const screenshotPath: string = getScreenshotPath('02-create-empty-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create organization form - filled', async ({ page }, testInfo) => {
			// Navigate to create organization page
			await page.goto('/admin/organizations/new');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle
			await page.waitForTimeout(500);

			// Fill in the organization name
			await page.getByLabel('Organization Name').fill('New Test Organization');

			const screenshotPath: string = getScreenshotPath('03-create-filled-dark', testInfo.project.name);

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
			// Set light mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'light' });
			await login(page);
		});

		test('organizations table default state', async ({ page }, testInfo) => {
			// Navigate to admin organizations page
			await page.goto('/admin/organizations');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle and data to load
			await page.waitForTimeout(1000);

			const screenshotPath: string = getScreenshotPath('01-default-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create organization form - empty', async ({ page }, testInfo) => {
			// Navigate to create organization page
			await page.goto('/admin/organizations/new');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle
			await page.waitForTimeout(500);

			const screenshotPath: string = getScreenshotPath('02-create-empty-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create organization form - filled', async ({ page }, testInfo) => {
			// Navigate to create organization page
			await page.goto('/admin/organizations/new');
			await page.waitForLoadState('domcontentloaded');

			// Wait for page to settle
			await page.waitForTimeout(500);

			// Fill in the organization name
			await page.getByLabel('Organization Name').fill('New Test Organization');

			const screenshotPath: string = getScreenshotPath('03-create-filled-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});
	});
});
