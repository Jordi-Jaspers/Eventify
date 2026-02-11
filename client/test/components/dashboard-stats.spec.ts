import {
    test,
    expect,
    setTheme,
    loginAndNavigate,
    DATA_LOAD_MS,
    COLD_START_TIMEOUT_MS
} from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { THEMES } from '../utils/constants';

const PAGE_NAME = 'dashboard';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Dashboard Stats Screenshots', () => {
    test.setTimeout(COLD_START_TIMEOUT_MS);

    for (const theme of THEMES) {
        test.describe(`${theme} mode`, () => {
            test.beforeEach(async ({ page }) => {
                await setTheme(page, theme);
                await loginAndNavigate(page, '/dashboard');
            });

            test(`stats cards visible`, async ({ page }, testInfo) => {
                await page.waitForTimeout(DATA_LOAD_MS);
                
                await expect(page.getByText('Events Today')).toBeVisible();
                await expect(page.getByText('Active Channels')).toBeVisible();
                await expect(page.getByText('Error Rate')).toBeVisible();
                
                await page.screenshot({
                    path: getScreenshot(`01-user-dashboard-${theme}`, testInfo.project.name),
                    fullPage: true
                });
            });
        });
    }
});

test.describe('Organization Dashboard Stats Screenshots', () => {
    test.setTimeout(COLD_START_TIMEOUT_MS);

    for (const theme of THEMES) {
        test.describe(`${theme} mode`, () => {
            test.beforeEach(async ({ page }) => {
                await setTheme(page, theme);
                // We know org 1 exists from seeded data
                await loginAndNavigate(page, '/organizations/1/dashboard');
            });

            test(`org stats cards visible`, async ({ page }, testInfo) => {
                await page.waitForTimeout(DATA_LOAD_MS);
                
                await expect(page.getByText('Events Today')).toBeVisible();
                await expect(page.getByText('Active Channels')).toBeVisible();
                
                await page.screenshot({
                    path: getScreenshot(`02-org-dashboard-${theme}`, testInfo.project.name),
                    fullPage: true
                });
            });
        });
    }
});
