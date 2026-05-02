/**
 * Test constants and timing values.
 * Centralizes magic numbers for maintainability.
 */

/** Time to wait for animations/transitions to complete */
export const ANIMATION_SETTLE_MS = 500;

/** Time to wait for theme changes to apply */
export const THEME_CHANGE_MS = 100;

/** Time to wait for page to fully settle after navigation */
export const PAGE_SETTLE_MS = 800;

/** Time to wait for data to load from API */
export const DATA_LOAD_MS = 1000;

/** Extended timeout for cold start (dev server initialization) */
export const COLD_START_TIMEOUT_MS = 30000;

/** Timeout for waiting for elements to appear */
export const ELEMENT_WAIT_TIMEOUT_MS = 5000;

/** Timeout for login flow to complete */
export const LOGIN_TIMEOUT_MS = 15000;

/** Theme options for screenshot tests */
export const THEMES = ['dark', 'light'] as const;
export type Theme = (typeof THEMES)[number];
