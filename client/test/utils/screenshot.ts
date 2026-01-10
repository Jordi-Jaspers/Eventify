/**
 * Screenshot utility functions for Playwright tests.
 * Provides consistent screenshot path generation and directory management.
 */
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

// ES Module compatible __dirname for this utility file
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/** Base directory for all screenshots */
export const SCREENSHOTS_BASE = join(__dirname, '../resources/screenshots');

/**
 * Gets or creates the screenshots directory for a specific page.
 * @param pageName - The page identifier (e.g., 'login', 'dashboard', 'org-members')
 * @returns The absolute path to the screenshots directory
 */
export function getScreenshotsDir(pageName: string): string {
	const dir = join(SCREENSHOTS_BASE, pageName);
	if (!existsSync(dir)) {
		mkdirSync(dir, { recursive: true });
	}
	return dir;
}

/**
 * Generates a screenshot file path with consistent naming.
 * @param pageName - The page identifier (e.g., 'login', 'dashboard')
 * @param screenshotName - The screenshot name (e.g., '01-default-dark')
 * @param projectName - The Playwright project name (e.g., 'Desktop Chrome')
 * @returns The absolute path for the screenshot file
 */
export function getScreenshotPath(pageName: string, screenshotName: string, projectName: string): string {
	const suffix = projectName.replace(/\s+/g, '-').toLowerCase();
	return join(getScreenshotsDir(pageName), `${screenshotName}-${suffix}.png`);
}

/**
 * Creates a screenshot helper bound to a specific page name.
 * Useful for reducing repetition in test files.
 * @param pageName - The page identifier
 * @returns A function that generates screenshot paths for that page
 */
export function createScreenshotHelper(pageName: string): (name: string, projectName: string) => string {
	getScreenshotsDir(pageName);
	return (name: string, projectName: string) => getScreenshotPath(pageName, name, projectName);
}
