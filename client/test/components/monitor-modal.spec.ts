/**
 * Monitor Modal Screenshot Tests
 *
 * Tests the Duration Details Modal in both dark and light modes.
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

const PAGE_NAME = 'monitor-modal';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Monitor Modal Screenshots', () => {
    test.setTimeout(COLD_START_TIMEOUT_MS);

    for (const theme of THEMES) {
        test.describe(`${theme} mode`, () => {
            test.beforeEach(async ({ page }) => {
                await setTheme(page, theme);
                await loginAndNavigate(page, '/watchlists/monitor');
            });

            test(`modal open with events`, async ({ page }, testInfo) => {
                await page.waitForTimeout(DATA_LOAD_MS);

                // Wait for any timeline segment button to be visible
                const segment = page.locator('button[aria-label*="segment"]').first();
                await segment.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
                
                // Click to open modal
                await segment.click();
                await page.waitForTimeout(ANIMATION_SETTLE_MS);
                
                // Wait for modal content
                await page.waitForSelector('[role="dialog"]', { state: 'visible' });

                await page.screenshot({
                    path: getScreenshot(`01-modal-open-${theme}`, testInfo.project.name),
                    fullPage: true
                });
            });
        });
    }
});
