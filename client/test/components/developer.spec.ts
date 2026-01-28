/**
 * Developer Page Screenshot Tests
 *
 * Tests the developer/API keys page in both dark and light modes.
 * This is an authenticated page - requires login.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';
import type { Page } from '@playwright/test';

const PAGE_NAME = 'developer';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

/**
 * Ensures there's space to create new API keys by revoking one if at limit.
 */
async function ensureCanCreateKey(page: Page): Promise<boolean> {
	// Check if the New Key button is disabled (at limit)
	const newKeyButton = page.getByRole('button', { name: /New Key/i });
	const isDisabled = await newKeyButton.isDisabled().catch(() => false);

	if (isDisabled) {
		// Try to revoke an existing key
		const revokeButton = page.locator('[aria-label*="Revoke"], button:has(svg.lucide-trash-2)').first();
		const canRevoke = await revokeButton.isVisible().catch(() => false);

		if (canRevoke) {
			await revokeButton.click({ force: true });
			await page.waitForTimeout(ANIMATION_SETTLE_MS);

			// Click the confirm revoke button in the dialog
			const confirmBtn = page.getByRole('button', { name: /Revoke/i }).last();
			await confirmBtn.click({ force: true });
			await page.waitForTimeout(1500);
			return true;
		}
		return false;
	}
	return true;
}

test.describe('Developer Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/developer');
			});

			test(`page layout and navigation`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`empty state or keys list`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// This captures either empty state or keys list depending on data
				await page.screenshot({
					path: getScreenshot(`02-content-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key button hover`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Find any visible New Key or Create Key button
				const createButton = page.getByRole('button', { name: /New Key|Create Key/i }).first();
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible) {
					const box = await createButton.boundingBox();
					if (box) {
						await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`03-button-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key sheet opened`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);
				await ensureCanCreateKey(page);

				// Click the New Key button (either in header or empty state)
				const createButton = page.getByRole('button', { name: /New Key|Create Key/i }).first();
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible && !(await createButton.isDisabled())) {
					await createButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`04-create-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key form filled`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);
				await ensureCanCreateKey(page);

				// Open the sheet
				const createButton = page.getByRole('button', { name: /New Key|Create Key/i }).first();
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible && !(await createButton.isDisabled())) {
					await createButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					// Fill the name input
					const nameInput = page.getByLabel(/Key Name/i);
					await nameInput.fill('Production App');

					// Select 30 days expiration option
					const expirationOption = page.getByRole('button', { name: /30 days/i });
					if (await expirationOption.isVisible()) {
						await expirationOption.click();
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`05-create-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`settings navigation tabs`, async ({ page }, testInfo) => {
				const profileLink = page.getByRole('link', { name: 'Profile' });
				const isVisible = await profileLink.isVisible().catch(() => false);

				if (isVisible) {
					const box = await profileLink.boundingBox();
					if (box) {
						await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`06-navigation-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`API key list view`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Just capture current state - may have keys or not
				await page.screenshot({
					path: getScreenshot(`07-keys-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`revoke API key confirmation dialog`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Look for any revoke/trash button
				const trashButton = page.locator('[aria-label*="Revoke"], button:has(svg.lucide-trash-2)').first();
				const trashExists = await trashButton.isVisible().catch(() => false);

				if (trashExists) {
					await trashButton.click({ force: true });
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`08-revoke-dialog-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
