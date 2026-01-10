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

/**
 * Ensures there's space to create new API keys by revoking one if at limit.
 * Returns true if we can create a new key, false otherwise.
 */
async function ensureCanCreateKey(page: import('@playwright/test').Page): Promise<boolean> {
	// Check if the New Key button is disabled (at limit)
	const disabledButton = page.locator('button:has-text("New Key")[disabled]');
	const isAtLimit: boolean = await disabledButton.count() > 0;
	
	if (isAtLimit) {
		// Revoke a key to make space
		const revokeButton = page.locator('button:has(svg.lucide-trash-2)').first();
		const canRevoke: boolean = await revokeButton.count() > 0;
		
		if (canRevoke) {
			await revokeButton.click({ force: true });
			await page.waitForTimeout(500);
			
			// Click confirm in the dialog
			const confirmBtn = page.locator('button:has-text("Revoke")').last();
			await confirmBtn.click({ force: true });
			await page.waitForTimeout(1500);
			return true;
		}
		return false;
	}
	return true;
}

/**
 * Gets the enabled "New Key" or "Create Key" button
 */
function getCreateButton(page: import('@playwright/test').Page) {
	return page.locator('button:has-text("New Key"):not([disabled]), button:has-text("Create Key"):not([disabled])').first();
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

	// Navigate to developer page
	await page.goto('/developer');
	await page.waitForLoadState('domcontentloaded');

	// Wait for page to settle
	await page.waitForTimeout(800);
}

test.describe('Developer Page Screenshots', () => {
	// Increased timeout to account for login + page load on first test
	test.setTimeout(30000);

	test.describe('Dark Mode', () => {
		test.beforeEach(async ({ page }) => {
			// Set dark mode BEFORE navigation
			await page.emulateMedia({ colorScheme: 'dark' });
			await loginAndNavigate(page);
		});

		test('page layout and navigation', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-layout-dark', testInfo.project.name);

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

			const screenshotPath: string = getScreenshotPath('02-empty-state-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key button hover', async ({ page }, testInfo) => {
			// Find the create button (may be "New Key" or "Create Key" in empty state)
			const createButton = page.locator('button:has-text("New Key"), button:has-text("Create Key")').first();
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			// Use mouse.move instead of hover() to avoid Playwright hanging issue
			const box = await createButton.boundingBox();
			if (box) {
				await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
			}
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('03-button-hover-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key sheet opened', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			await page.waitForTimeout(500); // Wait for sheet animation

			const screenshotPath: string = getScreenshotPath('04-create-sheet-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key form filled', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			
			// Wait for sheet to appear
			await page.waitForTimeout(1000);

			// Fill in the form - use id selector for reliability
			const nameInput = page.locator('#key-name');
			await nameInput.waitFor({ state: 'visible', timeout: 5000 });
			await nameInput.fill('Production App');

			// Select expiration by clicking the "30 days" button (to show the change from default 90 days)
			const expirationButton = page.locator('button:has-text("30 days")');
			await expirationButton.click();
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('05-create-filled-dark', testInfo.project.name);

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

			// Use mouse.move instead of hover() to avoid Playwright hanging issue
			const box = await profileLink.boundingBox();
			if (box) {
				await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
			}
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('06-navigation-dark', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key and see success modal', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			
			// Wait for sheet to appear
			await page.waitForTimeout(1000);

			// Fill in the form - use id selector for reliability
			const nameInput = page.locator('#key-name');
			await nameInput.waitFor({ state: 'visible', timeout: 5000 });
			await nameInput.fill('Test API Key');

			// Select expiration by clicking the "30 days" button
			const expirationButton = page.locator('button:has-text("30 days")');
			await expirationButton.click();
			await page.waitForTimeout(300);

			// Click Create Key button (inside the sheet) - use locator for reliability
			const submitButton = page.locator('button:has-text("Create Key")').last();
			await submitButton.click();

			// Wait for success modal to appear
			await page.waitForTimeout(1500);

			const screenshotPath: string = getScreenshotPath('07-key-created-modal-dark', testInfo.project.name);

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

			const screenshotPath: string = getScreenshotPath('08-keys-list-dark', testInfo.project.name);

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
				await trashButton.click({ force: true });
				await page.waitForTimeout(500); // Wait for dialog animation
			}

			const screenshotPath: string = getScreenshotPath('09-revoke-dialog-dark', testInfo.project.name);

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

		test('page layout and navigation', async ({ page }, testInfo) => {
			const screenshotPath: string = getScreenshotPath('01-layout-light', testInfo.project.name);

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

			const screenshotPath: string = getScreenshotPath('02-empty-state-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key button hover', async ({ page }, testInfo) => {
			// Find the create button (may be "New Key" or "Create Key" in empty state)
			const createButton = page.locator('button:has-text("New Key"), button:has-text("Create Key")').first();
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			// Use mouse.move instead of hover() to avoid Playwright hanging issue
			const box = await createButton.boundingBox();
			if (box) {
				await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
			}
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('03-button-hover-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key sheet opened', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			await page.waitForTimeout(500); // Wait for sheet animation

			const screenshotPath: string = getScreenshotPath('04-create-sheet-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key form filled', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			
			// Wait for sheet to appear
			await page.waitForTimeout(1000);

			// Fill in the form - use id selector for reliability
			const nameInput = page.locator('#key-name');
			await nameInput.waitFor({ state: 'visible', timeout: 5000 });
			await nameInput.fill('Production App');

			// Select expiration by clicking the "30 days" button (to show the change from default 90 days)
			const expirationButton = page.locator('button:has-text("30 days")');
			await expirationButton.click();
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('05-create-filled-light', testInfo.project.name);

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

			// Use mouse.move instead of hover() to avoid Playwright hanging issue
			const box = await profileLink.boundingBox();
			if (box) {
				await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
			}
			await page.waitForTimeout(300);

			const screenshotPath: string = getScreenshotPath('06-navigation-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});

		test('create API key and see success modal', async ({ page }, testInfo) => {
			// Ensure we can create a key (revoke one if at limit)
			await ensureCanCreateKey(page);
			
			// Click create API key button
			const createButton = getCreateButton(page);
			await createButton.waitFor({ state: 'visible', timeout: 5000 });
			await createButton.click();
			
			// Wait for sheet to appear
			await page.waitForTimeout(1000);

			// Fill in the form - use id selector for reliability
			const nameInput = page.locator('#key-name');
			await nameInput.waitFor({ state: 'visible', timeout: 5000 });
			await nameInput.fill('Test API Key');

			// Select expiration by clicking the "30 days" button
			const expirationButton = page.locator('button:has-text("30 days")');
			await expirationButton.click();
			await page.waitForTimeout(300);

			// Click Create Key button (inside the sheet) - use locator for reliability
			const submitButton = page.locator('button:has-text("Create Key")').last();
			await submitButton.click();

			// Wait for success modal to appear
			await page.waitForTimeout(1500);

			const screenshotPath: string = getScreenshotPath('07-key-created-modal-light', testInfo.project.name);

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

			const screenshotPath: string = getScreenshotPath('08-keys-list-light', testInfo.project.name);

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
				await trashButton.click({ force: true });
				await page.waitForTimeout(500); // Wait for dialog animation
			}

			const screenshotPath: string = getScreenshotPath('09-revoke-dialog-light', testInfo.project.name);

			await page.screenshot({
				path: screenshotPath,
				fullPage: true
			});

			expect(existsSync(screenshotPath)).toBeTruthy();
			console.log(`Screenshot saved: ${screenshotPath}`);
		});
	});
});
