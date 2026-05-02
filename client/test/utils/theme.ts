/**
 * Theme utilities for Playwright tests.
 * Provides helpers for dark/light mode testing.
 */
import type { Page } from '@playwright/test';
import { THEME_CHANGE_MS, type Theme } from './constants';

/**
 * Sets the color scheme for the page.
 * @param page - The Playwright page object
 * @param theme - The theme to set ('dark' or 'light')
 */
export async function setTheme(page: Page, theme: Theme): Promise<void> {
	await page.emulateMedia({ colorScheme: theme });
	await page.waitForTimeout(THEME_CHANGE_MS);
}

/**
 * Generates a theme suffix for screenshot names.
 * @param theme - The current theme
 * @returns The suffix string (e.g., '-dark' or '-light')
 */
export function getThemeSuffix(theme: Theme): string {
	return `-${theme}`;
}
