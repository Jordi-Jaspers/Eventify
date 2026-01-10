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

			test(`create organization form - empty`, async ({ page }, testInfo) => {
				await page.goto('/admin/organizations/new');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-create-empty-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create organization form - filled`, async ({ page }, testInfo) => {
				await page.goto('/admin/organizations/new');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.getByLabel('Organization Name').fill('New Test Organization');

				await page.screenshot({
					path: getScreenshot(`03-create-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
