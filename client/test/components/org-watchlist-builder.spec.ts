/**
 * Organization Watchlist Builder Screenshot Tests
 *
 * Tests the organization watchlist builder pages (create and edit) in both dark and light modes.
 * These are authenticated pages requiring OWNER/ADMIN role.
 *
 * NOTE: These tests navigate to org watchlist builder pages. If the user doesn't have permission,
 * they will be redirected. Tests are defensive and will capture whatever state results.
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

const PAGE_NAME: string = 'org-watchlist-builder';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Watchlist Builder Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.describe('Create Page', () => {
				test(`create page state`, async ({ page }, testInfo) => {
					await setTheme(page, theme);
					await loginAndNavigate(page, '/organizations/1/watchlists/new');
					await page.waitForTimeout(DATA_LOAD_MS);

					// Take screenshot of whatever state we're in
					// If redirected, we'll see that. If on the page, we'll see the builder.
					await page.screenshot({
						path: getScreenshot(`01-create-page-${theme}`, testInfo.project.name),
						fullPage: true
					});

					// Try to verify we're on the correct page
					const url: string = page.url();
					console.log(`Create page URL: ${url}`);

					// If we're on the actual create page, try to interact
					if (url.includes('/watchlists/new')) {
						// Try to fill out form
						const nameInput: import('@playwright/test').Locator = page.locator('#name');
						if (await nameInput.isVisible().catch(() => false)) {
							await nameInput.fill('Production Monitoring');
							await page.waitForTimeout(300);

							await page.screenshot({
								path: getScreenshot(`02-create-with-name-${theme}`, testInfo.project.name),
								fullPage: true
							});
						}
					}
				});
			});

			test.describe('Edit Page', () => {
				test(`edit page via list`, async ({ page }, testInfo) => {
					await setTheme(page, theme);
					// Navigate to org watchlists list first
					await loginAndNavigate(page, '/organizations/1/watchlists');
					await page.waitForTimeout(DATA_LOAD_MS);

					// Take screenshot of list page
					await page.screenshot({
						path: getScreenshot(`03-watchlists-list-${theme}`, testInfo.project.name),
						fullPage: true
					});

					const url: string = page.url();
					console.log(`Watchlists list URL: ${url}`);

					// If we're on the list page, try to find an edit button
					if (url.includes('/organizations/1/watchlists')) {
						const editButtons: import('@playwright/test').Locator = page.getByRole('button', { name: 'Edit watchlist' });
						const count: number = await editButtons.count();

						if (count > 0) {
							// Click first edit button
							await editButtons.first().click();
							await page.waitForTimeout(DATA_LOAD_MS);

							await page.screenshot({
								path: getScreenshot(`04-edit-page-${theme}`, testInfo.project.name),
								fullPage: true
							});
						} else {
							// No watchlists to edit - try creating one by clicking new button if present
							const newButton: import('@playwright/test').Locator = page.getByRole('button', { name: /new watchlist/i });
							if (await newButton.isVisible().catch(() => false)) {
								await newButton.click();
								await page.waitForTimeout(DATA_LOAD_MS);

								await page.screenshot({
									path: getScreenshot(`04-create-from-list-${theme}`, testInfo.project.name),
									fullPage: true
								});
							}
						}
					}
				});
			});
		});
	}
});
