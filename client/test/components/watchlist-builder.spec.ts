/**
 * Watchlist Builder Screenshot Tests
 *
 * Tests the watchlist builder (create and edit modes) in both dark and light modes.
 * This is an authenticated page - requires login.
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
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'watchlist-builder';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Watchlist Builder Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.describe('Create Page', () => {
				test.beforeEach(async ({ page }) => {
					await setTheme(page, theme);
					await loginAndNavigate(page, '/watchlists/new');
				});

				test(`create form layout`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					await page.screenshot({
						path: getScreenshot(`01-create-layout-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`create form with filled fields`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Fill out the form
					await page.locator('#name').fill('Critical Incidents Watchlist');
					await page
						.locator('#description')
						.fill('Monitor all critical incidents across production channels');

					// Select time range button
					const sevenDaysButton = page.getByRole('button', { name: '7d' });
					await sevenDaysButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`02-create-filled-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`validation error - empty name`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Try to submit without a name
					const createButton = page.getByRole('button', { name: /Create Watchlist/i });
					await createButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`03-create-validation-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});
			});

			test.describe('Edit Page', () => {
				test.beforeEach(async ({ page }) => {
					await setTheme(page, theme);
					await loginAndNavigate(page, '/watchlists');
					await page.waitForTimeout(DATA_LOAD_MS);

					// Click on first watchlist to edit (assuming one exists)
					const editButton = page.getByRole('button', { name: 'Edit watchlist' }).first();
					if (await editButton.isVisible()) {
						await editButton.click();
						await page.waitForTimeout(DATA_LOAD_MS);
					}
				});

				test(`edit form layout`, async ({ page }, testInfo) => {
					// Check if we're on an edit page
					const url: string = page.url();
					if (url.includes('/watchlists/') && !url.endsWith('/new')) {
						await page.screenshot({
							path: getScreenshot(`04-edit-layout-${theme}`, testInfo.project.name),
							fullPage: true
						});
					}
				});

				test(`edit with auto-save indicator`, async ({ page }, testInfo) => {
					const url: string = page.url();
					if (url.includes('/watchlists/') && !url.endsWith('/new')) {
						// Modify the name to trigger auto-save
						const nameInput = page.locator('#name');
						await nameInput.fill('Updated Watchlist Name');
						await page.waitForTimeout(ANIMATION_SETTLE_MS);

						await page.screenshot({
							path: getScreenshot(`05-edit-saving-${theme}`, testInfo.project.name),
							fullPage: true
						});
					}
				});
			});
		});
	}
});
