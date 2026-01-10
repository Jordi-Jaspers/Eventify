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
	const disabledButton = page.locator('button:has-text("New Key")[disabled]');
	const isAtLimit = (await disabledButton.count()) > 0;

	if (isAtLimit) {
		const revokeButton = page.locator('button:has(svg.lucide-trash-2)').first();
		const canRevoke = (await revokeButton.count()) > 0;

		if (canRevoke) {
			await revokeButton.click({ force: true });
			await page.waitForTimeout(ANIMATION_SETTLE_MS);

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
 * Gets the enabled "New Key" or "Create Key" button.
 */
function getCreateButton(page: Page) {
	return page.locator('button:has-text("New Key"):not([disabled]), button:has-text("Create Key"):not([disabled])').first();
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
				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`empty state visible`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`02-empty-state-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key button hover`, async ({ page }, testInfo) => {
				const createButton = page.locator('button:has-text("New Key"), button:has-text("Create Key")').first();
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

				const box = await createButton.boundingBox();
				if (box) {
					await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
				}
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`03-button-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key sheet opened`, async ({ page }, testInfo) => {
				await ensureCanCreateKey(page);

				const createButton = getCreateButton(page);
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await createButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`04-create-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key form filled`, async ({ page }, testInfo) => {
				await ensureCanCreateKey(page);

				const createButton = getCreateButton(page);
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await createButton.click();
				await page.waitForTimeout(DATA_LOAD_MS);

				const nameInput = page.locator('#key-name');
				await nameInput.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await nameInput.fill('Production App');

				const expirationButton = page.locator('button:has-text("30 days")');
				await expirationButton.click();
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`05-create-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`settings navigation tabs`, async ({ page }, testInfo) => {
				const profileLink = page.getByRole('link', { name: 'Profile' });
				await profileLink.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

				const box = await profileLink.boundingBox();
				if (box) {
					await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
				}
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`06-navigation-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create API key and see success modal`, async ({ page }, testInfo) => {
				await ensureCanCreateKey(page);

				const createButton = getCreateButton(page);
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await createButton.click();
				await page.waitForTimeout(DATA_LOAD_MS);

				const nameInput = page.locator('#key-name');
				await nameInput.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await nameInput.fill('Test API Key');

				const expirationButton = page.locator('button:has-text("30 days")');
				await expirationButton.click();
				await page.waitForTimeout(300);

				const submitButton = page.locator('button:has-text("Create Key")').last();
				await submitButton.click();
				await page.waitForTimeout(1500);

				await page.screenshot({
					path: getScreenshot(`07-key-created-modal-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`API key list with keys`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`08-keys-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`revoke API key confirmation dialog`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				const trashButton = page.locator('button:has(svg.lucide-trash-2)').first();
				const trashExists = await trashButton.isVisible().catch(() => false);

				if (trashExists) {
					await trashButton.click({ force: true });
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`09-revoke-dialog-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
