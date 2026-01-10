/**
 * Organization Members Page Screenshot Tests
 *
 * Tests the organization members page in both dark and light modes.
 * This is an authenticated page - requires login.
 */
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'org-members';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Organization Members Page Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await loginAndNavigate(page, '/organizations/1/members', DATA_LOAD_MS);
			});

			test(`members list`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-members-list-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`add member sheet opened`, async ({ page }, testInfo) => {
				const addButton = page.getByRole('button', { name: 'Add Member' });
				await addButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
				await addButton.click();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-add-member-sheet-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});
		});
	}
});
