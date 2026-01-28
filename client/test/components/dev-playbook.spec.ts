/**
 * Dev Playbook Screenshot Tests
 *
 * Tests the component playbook page in both dark and light modes.
 * This is a public page but only accessible in dev mode.
 */
import { test, expect, setTheme, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'dev-playbook';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Dev Playbook Screenshots', () => {
	test.setTimeout(COLD_START_TIMEOUT_MS);

	for (const theme of THEMES) {
		test.describe(`${theme} mode`, () => {
			test.beforeEach(async ({ page }) => {
				await setTheme(page, theme);
				await page.goto('/dev-playbook');
				await page.waitForLoadState('domcontentloaded');
				await page.waitForTimeout(DATA_LOAD_MS);
			});

			test(`page layout with sidebar`, async ({ page }, testInfo) => {
				await page.screenshot({
					path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
					fullPage: true
				});
			});

			test(`logo section`, async ({ page }, testInfo) => {
				// Scroll to logo section
				const logoSection = page.locator('#logo');
				await logoSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`02-logo-section-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`buttons section`, async ({ page }, testInfo) => {
				// Scroll to buttons section
				const buttonsSection = page.locator('#buttons');
				await buttonsSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`03-buttons-section-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`cards section`, async ({ page }, testInfo) => {
				// Scroll to cards section
				const cardsSection = page.locator('#cards');
				await cardsSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`04-cards-section-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`stat-card section`, async ({ page }, testInfo) => {
				// Scroll to stat-card section
				const statCardSection = page.locator('#stat-card');
				await statCardSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`05-stat-card-section-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`loading and access-denied cards`, async ({ page }, testInfo) => {
				// Scroll to loading-card section
				const loadingSection = page.locator('#loading-card');
				await loadingSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`06-loading-card-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`status-indicator section`, async ({ page }, testInfo) => {
				// Scroll to status-indicator section
				const statusSection = page.locator('#status-indicator');
				await statusSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`07-status-indicator-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});

			test(`empty-state section`, async ({ page }, testInfo) => {
				// Scroll to empty-state section
				const emptyStateSection = page.locator('#empty-state');
				await emptyStateSection.scrollIntoViewIfNeeded();
				await page.waitForTimeout(ANIMATION_SETTLE_MS);

				await page.screenshot({
					path: getScreenshot(`08-empty-state-${theme}`, testInfo.project.name),
					fullPage: false
				});
			});
		});
	}
});
