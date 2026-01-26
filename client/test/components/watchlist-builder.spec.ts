/**
 * Watchlist Builder Screenshot Tests
 *
 * Tests the click-based watchlist builder in both dark and light modes.
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
import { createScreenshotHelper } from '../utils';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils';

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

				test(`initial layout`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Verify the Configuration card with Add buttons is visible
					const configCard = page.getByText('Configuration');
					await configCard.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					// Verify Add Channel and Add Group buttons are in the configurator
					const addChannelBtn = page.getByRole('button', { name: /add channel/i });
					await addChannelBtn.first().waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

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

					// Expand description - look for the toggle button within DetailsCard (exact "Add" text, not "Add Channel")
					// The button is inside the Description label row and shows "Add" or "Hide"
					const descriptionSection = page.locator('label:has-text("Description")').locator('..');
					const addDescBtn = descriptionSection.getByRole('button');
					if (await addDescBtn.isVisible()) {
						const buttonText = await addDescBtn.textContent();
						if (buttonText?.includes('Add')) {
							await addDescBtn.click();
							await page.waitForTimeout(ANIMATION_SETTLE_MS);
						}
					}

					const descriptionInput = page.locator('#description');
					if (await descriptionInput.isVisible()) {
						await descriptionInput.fill('Monitor all production channels for critical alerts');
					}

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

					// Click the Add Channel button
					const addChannelBtn = page.getByRole('button', { name: /add channel/i }).first();
					await addChannelBtn.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					// Wait for sheet to be visible
					const sheetTitle = page.getByText('Select Channel');
					await sheetTitle.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					await page.screenshot({
						path: getScreenshot(`03-channel-sheet-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`group name sheet open`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Click the Add Group button
					const addGroupBtn = page.getByRole('button', { name: /add group/i }).first();
					await addGroupBtn.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					// Wait for sheet to be visible
					const sheetTitle = page.getByText('Create Group');
					await sheetTitle.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					await page.screenshot({
						path: getScreenshot(`04-group-sheet-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`watchlist with channels and groups`, async ({ page }, testInfo) => {
					// Navigate to watchlists list (already logged in from beforeEach)
					await page.goto('/watchlists');
					await page.waitForTimeout(DATA_LOAD_MS);

					// Wait for the list to load
					const watchlistName = page.getByText('All Channels Overview');
					await watchlistName.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					// Find all edit buttons and click the second one (All Channels Overview is the second row)
					const editButtons = page.getByRole('button', { name: 'Edit watchlist' });
					await editButtons.nth(1).click(); // Second row (0-indexed)
					await page.waitForTimeout(DATA_LOAD_MS);

					// Verify we're on the edit page with configuration loaded
					const configCard = page.getByText('Configuration');
					await configCard.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					await page.screenshot({
						path: getScreenshot(`05-with-components-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});
			});

		});
	}
});
