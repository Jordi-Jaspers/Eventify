import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/org-members/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/org-members');
if (!existsSync(screenshotsDir)) {
	mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
	const suffix: string = projectName.replace(/\s+/g, '-').toLowerCase();
	return join(screenshotsDir, `${name}-${suffix}.png`);
}

async function loginAndNavigate(page: import('@playwright/test').Page): Promise<void> {
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

	// Navigate to organization members page (org ID 1 = Acme Corporation)
	await page.goto('/organizations/1/members');
	await page.waitForLoadState('domcontentloaded');

	// Wait for page to settle and data to load
	await page.waitForTimeout(1000);
}

test.describe('Organization Members Page Screenshots', () => {
	test.setTimeout(30000);

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await loginAndNavigate(page);
		});

		test('members list', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-members-list-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('add member sheet opened', async ({ page }, testInfo) => {
			// Click add member button
			const addButton = page.getByRole('button', { name: 'Add Member' });
			await addButton.waitFor({ state: 'visible', timeout: 5000 });
			await addButton.click();
			await page.waitForTimeout(500); // Wait for sheet animation

			const screenshotPath: string = getScreenshotPath('02-add-member-sheet-dark', testInfo.project.name);

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
			await loginAndNavigate(page);
		});

		test('members list', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-members-list-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('add member sheet opened', async ({ page }, testInfo) => {
			// Click add member button
			const addButton = page.getByRole('button', { name: 'Add Member' });
			await addButton.waitFor({ state: 'visible', timeout: 5000 });
			await addButton.click();
			await page.waitForTimeout(500); // Wait for sheet animation

			const screenshotPath: string = getScreenshotPath('02-add-member-sheet-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});
	});
});
