---
name: screenshot-tests
description: Create Playwright screenshot tests for new pages. Used by Frontend Agent.
compatibility: opencode
metadata:
  phase: testing
  skill-type: frontend
  framework: playwright
---

# Screenshot Tests Skill

Create Playwright screenshot tests for every new page. Tests must pass before returning.

## When to Use

- Creating a new page/route
- Modifying an existing page significantly
- Adding new UI states (modals, forms, etc.)

## Test File Location

```
client/test/components/[page].spec.ts
```

Screenshots output to:
```
client/test/resources/screenshots/[page]/
```

## Test Utilities

The project provides shared utilities for consistent screenshot tests. **Always use these instead of manual setup.**

### File Structure

```
client/test/
├── fixtures/
│   └── test-fixtures.ts     # Extended test with theme/auth helpers
├── utils/
│   ├── index.ts             # Re-exports all utilities
│   ├── constants.ts         # Timing constants
│   ├── screenshot.ts        # Screenshot path helpers
│   ├── auth.ts              # Login utilities
│   └── theme.ts             # Theme management
└── components/
    └── [page].spec.ts       # Your test files
```

### Available Imports

```typescript
// From fixtures - extended test with helpers
import { test, expect, setTheme, loginAndNavigate, ANIMATION_SETTLE_MS, DATA_LOAD_MS } from '../fixtures/test-fixtures';

// From utils - screenshot helper
import { createScreenshotHelper } from '../utils/screenshot';

// From utils - constants
import { COLD_START_TIMEOUT_MS, ELEMENT_WAIT_TIMEOUT_MS, THEMES } from '../utils/constants';
```

### Constants Reference

| Constant | Value | Purpose |
|----------|-------|---------|
| `ANIMATION_SETTLE_MS` | 500ms | Wait for CSS animations/transitions |
| `DATA_LOAD_MS` | 1000ms | Wait for API data to load |
| `PAGE_SETTLE_MS` | 800ms | Wait for page to fully settle |
| `COLD_START_TIMEOUT_MS` | 30000ms | Test timeout for cold start |
| `ELEMENT_WAIT_TIMEOUT_MS` | 5000ms | Wait for element to appear |
| `LOGIN_TIMEOUT_MS` | 15000ms | Login flow timeout |
| `THEMES` | `['dark', 'light']` | Available themes for iteration |

## Complete Test Template

```typescript
/**
 * [Page Name] Screenshot Tests
 *
 * Tests the [description] in both dark and light modes.
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

const PAGE_NAME = '[page]';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('[Page] Screenshots', () => {
    test.setTimeout(COLD_START_TIMEOUT_MS);

    for (const theme of THEMES) {
        test.describe(`${theme} mode`, () => {
            test.beforeEach(async ({ page }) => {
                await setTheme(page, theme);
                await loginAndNavigate(page, '/[route]');
            });

            test(`page layout`, async ({ page }, testInfo) => {
                await page.waitForTimeout(DATA_LOAD_MS);

                await page.screenshot({
                    path: getScreenshot(`01-layout-${theme}`, testInfo.project.name),
                    fullPage: true
                });
            });

            // Add more tests as needed...
        });
    }
});
```

## Unauthenticated Pages

For public pages (login, signup, etc.):

```typescript
import {
    test,
    expect,
    setTheme,
    ANIMATION_SETTLE_MS,
    DATA_LOAD_MS
} from '../fixtures/test-fixtures';
import { createScreenshotHelper } from '../utils/screenshot';
import { COLD_START_TIMEOUT_MS, THEMES } from '../utils/constants';

const PAGE_NAME = 'login';
const getScreenshot = createScreenshotHelper(PAGE_NAME);

test.describe('Login Screenshots', () => {
    test.setTimeout(COLD_START_TIMEOUT_MS);

    for (const theme of THEMES) {
        test.describe(`${theme} mode`, () => {
            test.beforeEach(async ({ page }) => {
                await setTheme(page, theme);
                await page.goto('/login');
                await page.waitForLoadState('domcontentloaded');
                await page.waitForTimeout(DATA_LOAD_MS);
            });

            test(`default state`, async ({ page }, testInfo) => {
                await page.screenshot({
                    path: getScreenshot(`01-default-${theme}`, testInfo.project.name),
                    fullPage: true
                });
            });
        });
    }
});
```

## Common States to Capture

### Forms
```typescript
test(`empty form`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    await page.screenshot({
        path: getScreenshot(`01-empty-form-${theme}`, testInfo.project.name),
        fullPage: true
    });
});

test(`validation errors`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    await page.getByRole('button', { name: 'Submit' }).click();
    await page.waitForTimeout(ANIMATION_SETTLE_MS);
    await page.screenshot({
        path: getScreenshot(`02-validation-errors-${theme}`, testInfo.project.name),
        fullPage: true
    });
});

test(`form filled`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    await page.locator('#name').fill('Example Name');
    await page.locator('#email').fill('example@email.com');
    await page.waitForTimeout(300);
    await page.screenshot({
        path: getScreenshot(`03-form-filled-${theme}`, testInfo.project.name),
        fullPage: true
    });
});
```

### Modals/Sheets
```typescript
test(`sheet opened`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    
    const openButton = page.getByRole('button', { name: /New Item/i });
    await openButton.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
    await openButton.click();
    await page.waitForTimeout(ANIMATION_SETTLE_MS);
    
    await page.screenshot({
        path: getScreenshot(`03-sheet-open-${theme}`, testInfo.project.name),
        fullPage: true
    });
});
```

### Hover States
```typescript
test(`button hover`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    
    const button = page.getByRole('button', { name: /Action/i });
    await button.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
    
    const box = await button.boundingBox();
    if (box) {
        await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
    }
    await page.waitForTimeout(300);
    
    await page.screenshot({
        path: getScreenshot(`02-button-hover-${theme}`, testInfo.project.name),
        fullPage: true
    });
});
```

### Data Table States
```typescript
test(`table with data`, async ({ page }, testInfo) => {
    await page.waitForTimeout(DATA_LOAD_MS);
    
    // Wait for table to populate
    const table = page.locator('table');
    await table.waitFor({ state: 'visible', timeout: ELEMENT_WAIT_TIMEOUT_MS });
    
    await page.screenshot({
        path: getScreenshot(`01-table-data-${theme}`, testInfo.project.name),
        fullPage: true
    });
});

test(`table empty state`, async ({ page }, testInfo) => {
    // Navigate with filter that returns no results
    await page.goto('/items?search=nonexistent');
    await page.waitForTimeout(DATA_LOAD_MS);
    
    await page.screenshot({
        path: getScreenshot(`02-table-empty-${theme}`, testInfo.project.name),
        fullPage: true
    });
});
```

## Running Tests

```bash
cd client

# Run specific test file
bun run test -- test/components/[page].spec.ts

# Run all tests
bun run test

# Run with pattern
bun run test -- --grep "[Page]"
```

The test command auto-starts the backend if needed.

## Critical Rules

1. **Navigate to REAL pages** - Never use mock HTML
   ```typescript
   // ✅ CORRECT
   await page.goto('/dashboard');
   
   // ❌ WRONG
   await page.setContent('<html>...</html>');
   ```

2. **Use shared utilities** - Never manually create directories
   ```typescript
   // ✅ CORRECT - uses createScreenshotHelper
   const getScreenshot = createScreenshotHelper(PAGE_NAME);
   
   // ❌ WRONG - manual directory creation
   const screenshotsDir = join(__dirname, '../resources/screenshots/page');
   if (!existsSync(screenshotsDir)) mkdirSync(screenshotsDir, { recursive: true });
   ```

3. **Test both themes** - Always loop over THEMES
   ```typescript
   // ✅ CORRECT
   for (const theme of THEMES) {
       test.describe(`${theme} mode`, () => { ... });
   }
   
   // ❌ WRONG - only one theme
   test.describe('Screenshots', () => { ... });
   ```

4. **Include theme in screenshot name** - For clarity
   ```typescript
   // ✅ CORRECT
   getScreenshot(`01-layout-${theme}`, testInfo.project.name)
   
   // ❌ WRONG - missing theme
   getScreenshot('01-layout', testInfo.project.name)
   ```

5. **Use constants for timeouts** - No magic numbers
   ```typescript
   // ✅ CORRECT
   await page.waitForTimeout(DATA_LOAD_MS);
   await page.waitForTimeout(ANIMATION_SETTLE_MS);
   
   // ❌ WRONG - hardcoded values
   await page.waitForTimeout(500);
   await page.waitForTimeout(1000);
   ```

6. **Use numbered prefixes** - For ordering
   ```
   01-layout-dark-desktop-chrome.png
   02-button-hover-dark-desktop-chrome.png
   03-sheet-open-dark-desktop-chrome.png
   ```

7. **Full page screenshots** - Capture everything
   ```typescript
   await page.screenshot({ path, fullPage: true });
   ```

## Verification

After creating tests, run once:

```bash
cd client && bun run test -- test/components/[page].spec.ts
```

Verify:
- [ ] Tests pass (no errors)
- [ ] Screenshots exist in `test/resources/screenshots/[page]/`
- [ ] Screenshots show actual page content (not blank/error)
- [ ] Both dark and light theme screenshots exist

## Folder Structure

```
client/
├── test/
│   ├── fixtures/
│   │   └── test-fixtures.ts       # Extended test, theme, auth helpers
│   ├── utils/
│   │   ├── index.ts               # Re-exports
│   │   ├── constants.ts           # Timing constants, THEMES
│   │   ├── screenshot.ts          # createScreenshotHelper
│   │   ├── auth.ts                # login, loginAndNavigate
│   │   └── theme.ts               # setTheme
│   ├── components/
│   │   ├── login.spec.ts
│   │   ├── dashboard.spec.ts
│   │   └── [page].spec.ts         ← Create this
│   └── resources/
│       └── screenshots/
│           ├── login/
│           ├── dashboard/
│           └── [page]/            ← Generated here
└── playwright.config.ts
```

## Troubleshooting

### Tests timeout
- Ensure `test.setTimeout(COLD_START_TIMEOUT_MS)` is set at describe level
- Check backend is running
- Use `domcontentloaded` not `networkidle`

### Screenshots blank
- Wait longer: `await page.waitForTimeout(DATA_LOAD_MS)`
- Check route exists
- Verify login succeeded (for auth pages)

### Element not found
- Use `ELEMENT_WAIT_TIMEOUT_MS` for waitFor
- Check selector syntax
- Verify element exists in DOM

### Theme not applied
- Ensure `setTheme(page, theme)` is called before navigation
- Check theme class is applied to html element

## Remember

- **Create tests for new pages** - Mandatory
- **Run tests before returning** - Must pass
- **Test both themes** - Dark and light
- **Use shared utilities** - No manual directory creation
- **Don't iterate on visuals** - UI Agent does that
- **Include test file in output** - Orchestrator needs it
