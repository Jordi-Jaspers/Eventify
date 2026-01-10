/**
 * Login Page Screenshot Tests
 *
 * Tests the login page in both dark and light modes.
 * This is a public page - no authentication required.
 */
import { test, expect, setTheme, ANIMATION_SETTLE_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'login';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Login Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await page.goto('/login');
				await page.waitForLoadState('domcontentloaded');
				await setTheme(page, theme);
				await page.waitForTimeout(ANIMATION_SETTLE_MS);
			});

			test(`default state - empty form`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`form with email filled`, async ({ page }, testInfo) => {
				await page.getByLabel('Email').fill('user@example.com');

				await page.screenshot({
					path: getScreenshot(`02-with-email-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`form with credentials filled`, async ({ page }, testInfo) => {
				await page.getByLabel('Email').fill('user@example.com');
				await page.locator('#password').fill('SecurePassword123!');

				await page.screenshot({
					path: getScreenshot(`03-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`password visibility toggled`, async ({ page }, testInfo) => {
				await page.getByLabel('Email').fill('user@example.com');
				await page.locator('#password').fill('SecurePassword123!');
				await page.getByRole('button', { name: 'Show password' }).click();

				await page.screenshot({
					path: getScreenshot(`04-password-visible-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`focused email input`, async ({ page }, testInfo) => {
				await page.getByLabel('Email').focus();

				await page.screenshot({
					path: getScreenshot(`05-email-focused-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
