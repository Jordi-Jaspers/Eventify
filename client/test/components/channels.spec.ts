/**
 * Channels Page Screenshot Tests
 *
 * Tests the personal channels management page with DataTable in both dark and light modes.
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

const PAGE_NAME = 'channels';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Channels Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/channels');
			});

			test(`page layout with channels table`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`new channel button hover`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				const newChannelButton = page.getByRole('button', { name: /New Channel/i }).first();
				await newChannelButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

				const box = await newChannelButton.boundingBox();
				if (box) {
					await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
				}
				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`02-button-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create channel sheet opened`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				const newChannelButton = page.getByRole('button', { name: /New Channel/i }).first();
				await newChannelButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await newChannelButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`03-create-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`create channel form filled`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				const newChannelButton = page.getByRole('button', { name: /New Channel/i }).first();
				await newChannelButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await newChannelButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				// Fill in the form
				const nameInput = page.locator('#channel-name');
				await nameInput.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await nameInput.fill('Production Events');

				const slugInput = page.locator('#channel-slug');
				await slugInput.fill('myapp.prod.events');

				const descriptionInput = page.locator('#channel-description');
				await descriptionInput.fill('All production environment events and alerts');

				await page.waitForTimeout(300);

				await page.screenshot({
					path: getScreenshot(`04-create-filled-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`channel row hover state`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Find first channel row (glassmorphism card in DataTable)
				const channelRow = page.locator('.bg-card\\/50').first();
				const rowExists = await channelRow.isVisible().catch(() => false);

				if (rowExists) {
					const box = await channelRow.boundingBox();
					if (box) {
						await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
					}
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`05-row-hover-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`actions dropdown menu`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Find first actions button (MoreVertical icon)
				const actionsButton = page.getByRole('button').filter({ has: page.locator('svg') }).first();
				const buttonExists = await actionsButton.isVisible().catch(() => false);

				if (buttonExists) {
					await actionsButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`06-actions-dropdown-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`edit channel sheet`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Open actions dropdown and click Edit
				const actionsButton = page.getByRole('button').filter({ has: page.locator('svg') }).first();
				const buttonExists = await actionsButton.isVisible().catch(() => false);

				if (buttonExists) {
					await actionsButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					const editMenuItem = page.getByRole('menuitem', { name: /Edit/i });
					const editExists = await editMenuItem.isVisible().catch(() => false);
					if (editExists) {
						await editMenuItem.click();
						await page.waitForTimeout(ANIMATION_SETTLE_MS);
					}
				}

				await page.screenshot({
					path: getScreenshot(`07-edit-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`status badges in table`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Look for status badges in the table
				const activeBadge = page.locator('text=Active').first();
				const badgeExists = await activeBadge.isVisible().catch(() => false);

				if (badgeExists) {
					await activeBadge.scrollIntoViewIfNeeded();
					await page.waitForTimeout(300);
				}

				await page.screenshot({
					path: getScreenshot(`08-status-badges-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`filter panel`, async ({ page }, testInfo) => {
				await page.waitForTimeout(DATA_LOAD_MS);

				// Try to find and open filter panel if it exists
				const filterButton = page.getByRole('button', { name: /filter/i }).first();
				const filterExists = await filterButton.isVisible().catch(() => false);

				if (filterExists) {
					await filterButton.click();
					await page.waitForTimeout(ANIMATION_SETTLE_MS);
				}

				await page.screenshot({
					path: getScreenshot(`09-filters-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
