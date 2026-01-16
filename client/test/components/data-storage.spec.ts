/**
 * Data & Storage Screenshot Tests
 *
 * Tests the data retention settings pages (user and organization) in both dark and light modes.
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

const PAGE_NAME = 'data-storage';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Data & Storage Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.describe('User Settings', () => {
				test.beforeEach(async ({ page }) => {
					await setTheme(page, theme);
					await loginAndNavigate(page, '/profile/data-storage');
				});

				test(`user page layout`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					await page.screenshot({
						path: getScreenshot(`01-user-layout-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`user slider interaction`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Find the slider and adjust it
					const slider: any = page.locator('[data-slot="slider"]');
					await slider.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					// Get slider bounding box
					const box: any = await slider.boundingBox();
					if (box) {
						// Click at 75% position (closer to 3 years)
						await page.mouse.click(box.x + box.width * 0.75, box.y + box.height / 2);
					}

					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`02-user-slider-adjusted-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`user warning alert`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Find the slider and move it to a lower value (3mo)
					const slider: any = page.locator('[data-slot="slider"]');
					await slider.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					const box: any = await slider.boundingBox();
					if (box) {
						// Click at start (3 months)
						await page.mouse.click(box.x, box.y + box.height / 2);
					}

					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`03-user-warning-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});
			});

			test.describe('Organization Settings', () => {
				test.beforeEach(async ({ page }) => {
					await setTheme(page, theme);
					// Navigate directly to org 1 data storage settings
					await loginAndNavigate(page, '/organizations/1/settings/data-storage');
				});

				test(`org page layout`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					await page.screenshot({
						path: getScreenshot(`04-org-layout-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});

				test(`org slider interaction`, async ({ page }, testInfo) => {
					await page.waitForTimeout(DATA_LOAD_MS);

					// Find the slider and adjust it
					const slider: any = page.locator('[data-slot="slider"]');
					await slider.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });

					const box: any = await slider.boundingBox();
					if (box) {
						// Click at 50% position
						await page.mouse.click(box.x + box.width * 0.5, box.y + box.height / 2);
					}

					await page.waitForTimeout(ANIMATION_SETTLE_MS);

					await page.screenshot({
						path: getScreenshot(`05-org-slider-adjusted-${theme}`, testInfo.project.name),
						fullPage: true
					});
				});
			});
		});
	}
});
