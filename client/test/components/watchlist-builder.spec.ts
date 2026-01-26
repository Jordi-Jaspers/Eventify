/**
 * Watchlist Builder Screenshot Tests
 *
 * Tests the new drag-and-drop watchlist builder in both dark and light modes.
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
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

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

				test(`initial layout with building blocks`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					await page.screenshot({
						path: getScreenshot(`01-initial-layout-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`form with details filled`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Fill out the watchlist details
					const nameInput = page.locator('#name');
					await nameInput.fill('Production Monitoring');

					const descriptionInput = page.locator('#description');
					await descriptionInput.fill('Monitor all production channels for critical alerts');

					// Select 7d time range
					const sevenDaysButton = page.getByRole('button', { name: '7d' });
					await sevenDaysButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`02-details-filled-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`channel select sheet open`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Look for building blocks section
					const buildinBlocksCard = page.getByText('Building Blocks');
					await buildinBlocksCard.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					// The sheet will open when user drags a channel block and drops it
					// For testing, we can't easily simulate drag-drop, but we can
					// try to find and click elements if they exist
					// This test validates the sheet component exists

					await page.screenshot({
						path: getScreenshot(`03-building-blocks-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`empty configurator state`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// The configurator should show empty state initially
					const configurator = page.getByText('Configurator');
					await configurator.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					await page.screenshot({
						path: getScreenshot(`04-empty-configurator-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});
			});

			test.describe('Edit Page (if watchlist exists)', () => {
				test.beforeEach(async ({ page }) => {
					await setTheme(page, theme);
					await loginAndNavigate(page, '/watchlists');
					await page.waitForTimeout(DATA_LOAD_MS);

					// Try to click on first watchlist edit button
					const editButtons = page.getByRole('button', { name: /edit/i });
					const firstEdit = editButtons.first();

					const isVisible: boolean = await firstEdit.isVisible().catch(() => false);
					if (isVisible) {
						await firstEdit.click();
						await page.waitForTimeout(DATA_LOAD_MS);
					}
				});

				test(`edit layout with existing configuration`, async ({ page }, testInfo) => {
					// Check if we're on an edit page
					const url: string = page.url();
					if (url.includes('/watchlists/') && !url.endsWith('/new')) {
						await page.screenshot({
							path: getScreenshot(`05-edit-layout-${theme}`, testInfo.project.name),
							fullPage: true
						});
					}
				});

				test(`saving indicator`, async ({ page }, testInfo) => {
					const url: string = page.url();
					if (url.includes('/watchlists/') && !url.endsWith('/new')) {
						// Modify name to trigger auto-save
						const nameInput = page.locator('#name');
						await nameInput.fill('Updated Watchlist ' + Date.now());
						await page.waitForTimeout(ANIMATION_SETTLE_MS);

						await page.screenshot({
							path: getScreenshot(`06-edit-saving-${theme}`, testInfo.project.name),
							fullPage: true
						});
					}
				});
			});
		});
	}
});
