/**
 * Organization Channels Screenshot Tests
 *
 * Tests the organization channels page in both dark and light modes.
 * This is an authenticated page requiring organization membership or global admin.
 */
import {
	test,
	expect,
	setTheme,
	loginAndNavigate,
	ANIMATION_SETTLE_MS,
	DATA_LOAD_MS
} from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'org-channels';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Channels Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				// Navigate to org channels page - using org ID 1 (created in test seed data)
				await loginAndNavigate(page, '/organizations/1/channels');
			});

			test(`page layout`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create channel sheet`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Open create sheet
				const createButton = page.getByRole('button', { name: /New Channel/i });
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await createButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-create-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create channel form filled`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Open create sheet
				const createButton = page.getByRole('button', { name: /New Channel/i });
				await createButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await createButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				// Fill form
				await page.locator('#channel-name').fill('Test Organization Channel');
				await page.locator('#channel-description').fill('This is a test channel for the organization');
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`03-create-form-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`channel row hover`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Wait for data table to load
				const channelRow = page.locator('[class*="grid"][class*="rounded-lg"]').first();
				await channelRow.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

				// Hover over first channel row
				await channelRow.hover();
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`04-channel-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
