/**
 * Authentication utilities for Playwright tests.
 * Provides login helpers for authenticated page testing.
 */
import type { Page } from '@playwright/test';
import { LOGIN_TIMEOUT_MS, PAGE_SETTLE_MS } from './constants';

/**
 * Performs login using dev credentials.
 * This is the base login function that other helpers build upon.
 * @param page - The Playwright page object
 */
export async function login(page: Page): Promise<void> {
	await page.goto('/login');
	await page.waitForLoadState('domcontentloaded');

	const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
	await fillButton.waitFor({ state: 'visible', timeout: LOGIN_TIMEOUT_MS });
	await fillButton.click();

	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.waitForURL('/dashboard', { timeout: LOGIN_TIMEOUT_MS });
}

/**
 * Logs in and navigates to a specific page.
 * Use this for authenticated page tests.
 * @param page - The Playwright page object
 * @param targetPath - The path to navigate to after login (e.g., '/profile', '/admin/users')
 * @param settleTime - Time to wait for page to settle (default: PAGE_SETTLE_MS)
 */
export async function loginAndNavigate(page: Page, targetPath: string, settleTime = PAGE_SETTLE_MS): Promise<void> {
	await login(page);
	if (targetPath !== '/dashboard') {
		await page.goto(targetPath);
		await page.waitForLoadState('domcontentloaded');
	}
	await page.waitForTimeout(settleTime);
}
