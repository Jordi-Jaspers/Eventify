/**
 * Organization Settings Page Screenshot Tests
 *
 * Tests the organization settings page in both dark and light modes.
 * This is an authenticated page - requires login.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'org-settings';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Settings Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/organizations/1/settings', DATA_LOAD_MS);
			});

			test(`general tab`, async ({ page }, testInfo) => {
				await page.waitForURL('/organizations/1/settings/general', { timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`01-general-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`api keys tab`, async ({ page }, testInfo) => {
				const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
				await apiKeysLink.click();
				await page.waitForURL('/organizations/1/settings/api-keys', { timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`02-api-keys-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create api key modal`, async ({ page }, testInfo) => {
				const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
				await apiKeysLink.click();
				await page.waitForURL('/organizations/1/settings/api-keys', { timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.getByRole('button', { name: 'Create API Key' }).click();
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`03-create-modal-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create and view api key`, async ({ page }, testInfo) => {
				const apiKeysLink = page.getByRole('link', { name: 'API Keys' });
				await apiKeysLink.click();
				await page.waitForURL('/organizations/1/settings/api-keys', { timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.getByRole('button', { name: 'Create API Key' }).click();
				await page.waitForTimeout(300);

				// Fill in the key name
				const keyName = `Key ${Date.now()}`;
				await page.locator('#key-name').fill(keyName);

				// Click Create Key button (within the sheet)
				const sheet = page.getByLabel('Create API Key');
				await sheet.getByRole('button', { name: 'Create Key' }).click();

				// Wait for success modal
				await page.waitForSelector('text=API Key Created', { timeout: ELEMENT_WAIT_TIMEOUT_MS });

				// Screenshot of key created modal
				await page.screenshot({
					path: getScreenshot(`04-key-created-modal-${theme}`, testInfo.project.name),
					fullPage: true
				});

				// Close the modal
				await page.getByRole('button', { name: 'Done' }).click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				// Screenshot of keys list
				await page.screenshot({
					path: getScreenshot(`05-api-keys-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
