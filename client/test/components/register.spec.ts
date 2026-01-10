/**
 * Register Page Screenshot Tests
 *
 * Tests the registration page in both dark and light modes.
 * This is a public page - no authentication required.
 */
import { test, expect, setTheme, ANIMATION_SETTLE_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'register';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Register Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await page.goto('/register');
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

			test(`form with data filled`, async ({ page }, testInfo) => {
				await page.getByLabel('First Name').fill('John');
				await page.getByLabel('Last Name').fill('Doe');
				await page.getByLabel('Email').fill('john.doe@example.com');
				await page.locator('#password').fill('SecurePassword123!');
				await page.locator('#passwordConfirmation').fill('SecurePassword123!');

				await page.screenshot({
					path: getScreenshot(`02-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`password strength indicator - weak password`, async ({ page }, testInfo) => {
				await page.locator('#password').fill('weak');
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`03-password-weak-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`password strength indicator - strong password`, async ({ page }, testInfo) => {
				await page.locator('#password').fill('SecurePassword123!');
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`04-password-strong-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`passwords match indicator`, async ({ page }, testInfo) => {
				await page.locator('#password').fill('SecurePassword123!');
				await page.locator('#passwordConfirmation').fill('SecurePassword123!');
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`05-passwords-match-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
