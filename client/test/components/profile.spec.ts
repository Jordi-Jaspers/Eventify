import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname
const __filename: string = fileURLToPath(import.meta.url);
const __dirname: string = dirname(__filename);

// Screenshots go to screenshots/profile/ folder
const screenshotsDir: string = join(__dirname, '../resources/screenshots/profile');
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

	// Navigate to profile page
	await page.goto('/profile');
	await page.waitForLoadState('domcontentloaded');

	// Wait for page to settle
	await page.waitForTimeout(800);
}

test.describe('Profile Page Screenshots', () => {
	test.setTimeout(30000);

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await loginAndNavigate(page);
		});

		test('default state - profile view', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-default-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('editable field focused', async ({ page }, testInfo) => {
			// Try to click an edit button if available
			const pencilButtons = page.locator('button:has(svg.lucide-pencil)');
			const count = await pencilButtons.count();

			if (count > 0) {
				await pencilButtons.first().click();
				await page.waitForTimeout(300);
			}

			const screenshotPath: string = getScreenshotPath('02-edit-mode-dark', testInfo.project.name);

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

		test('default state - profile view', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-default-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('editable field focused', async ({ page }, testInfo) => {
			// Try to click an edit button if available
			const pencilButtons = page.locator('button:has(svg.lucide-pencil)');
			const count = await pencilButtons.count();

			if (count > 0) {
				await pencilButtons.first().click();
				await page.waitForTimeout(300);
			}

			const screenshotPath: string = getScreenshotPath('02-edit-mode-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});
	});
});
