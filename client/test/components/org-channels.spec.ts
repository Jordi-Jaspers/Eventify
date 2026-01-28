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
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible) {
					await createButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`02-create-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create channel form filled`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Open create sheet
				const createButton = page.getByRole('button', { name: /New Channel/i });
				const isVisible = await createButton.isVisible().catch(() => false);

				if (isVisible) {
					await createButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					// Fill form using labels
					const nameInput = page.getByLabel(/Channel Name/i);
					if (await nameInput.isVisible()) {
						await nameInput.fill('Test Organization Channel');
					}

					const descInput = page.getByLabel(/Description/i);
					if (await descInput.isVisible()) {
						await descInput.fill('This is a test channel for the organization');
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`03-create-form-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`channel list view`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Just capture the channels list - this is more reliable than hover tests
				await page.screenshot({
					path: getScreenshot(`04-channels-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
