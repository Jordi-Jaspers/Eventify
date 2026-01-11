/**
 * Reset Password Page Screenshot Tests
 *
 * Tests the reset password page with token parameter in both dark and light modes.
 * This is a public page - no authentication required.
 */
import { test, expect, setTheme, ANIMATION_SETTLE_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'reset-password';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Reset Password Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await page.goto('/reset-password?token=test-token-123');
				await page.waitForLoadState('domcontentloaded');
				await setTheme(page, theme);
				await page.waitForTimeout(ANIMATION_SETTLE_MS);
			});

			test(`default state with token`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`password strength meter`, async ({ page }, testInfo) => {
				// Type a weak password first to show strength meter
				await page.fill('input#newPassword', 'weak');
				await page.waitForTimeout(300);
				await page.screenshot({
					path: getScreenshot(`02-strength-weak-${theme}`, testInfo.project.name),
					fullPage: true
				});

				// Type a strong password to show full strength
				await page.fill('input#newPassword', 'StrongPass123!');
				await page.waitForTimeout(300);
				await page.screenshot({
					path: getScreenshot(`03-strength-strong-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`filled passwords with match indicator`, async ({ page }, testInfo) => {
				await page.fill('input#newPassword', 'StrongPass123!');
				await page.fill('input#confirmPassword', 'StrongPass123!');
				await page.waitForTimeout(300);
				await page.screenshot({
					path: getScreenshot(`04-filled-passwords-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`password visibility toggle`, async ({ page }, testInfo) => {
				await page.fill('input#newPassword', 'StrongPass123!');
				// Click the eye icon to show password
				const toggleButtons = page.locator('button[aria-label*="password"]');
				await toggleButtons.first().click();
				await page.waitForTimeout(300);
				await page.screenshot({
					path: getScreenshot(`05-password-visible-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`validation error - passwords mismatch`, async ({ page }, testInfo) => {
				await page.fill('input#newPassword', 'StrongPass123!');
				await page.fill('input#confirmPassword', 'DifferentPass456!');
				await page.waitForTimeout(300);
				await page.screenshot({
					path: getScreenshot(`06-validation-mismatch-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});

		test.describe(`${theme} mode - missing token`, () => {
			test.beforeEach(async ({ page }) => {
				await page.goto('/reset-password');
				await page.waitForLoadState('domcontentloaded');
				await setTheme(page, theme);
				await page.waitForTimeout(ANIMATION_SETTLE_MS);
			});

			test(`missing token error`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`07-missing-token-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
