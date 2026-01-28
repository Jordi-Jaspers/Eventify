/**
 * Admin Organizations Page Screenshot Tests
 *
 * Tests the admin organizations page in both dark and light modes.
 * This is an admin page - requires login with admin privileges.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';
import { login } from '../utils/auth';

const PAGE_NAME = 'admin-organizations';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Admin Organizations Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await login(page);
			});

			test(`organizations table default state`, async ({ page }, testInfo) => {
				await page.goto('/admin/organizations');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create organization sheet - open`, async ({ page }, testInfo) => {
				await page.goto('/admin/organizations');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(DATA_LOAD_MS);

				// Click "New Organization" button to open the sheet
				await page.getByRole('button', { name: /New Organization/i }).click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-create-sheet-open-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create organization sheet - filled`, async ({ page }, testInfo) => {
				await page.goto('/admin/organizations');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(DATA_LOAD_MS);

				// Click "New Organization" button to open the sheet
				await page.getByRole('button', { name: /New Organization/i }).click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				// Fill in the organization name
				await page.getByLabel('Organization Name').fill('New Test Organization');

				await page.screenshot({
					path: getScreenshot(`03-create-sheet-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
