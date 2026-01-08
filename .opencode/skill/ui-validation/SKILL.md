# UI Validation Skill

Use Playwright to validate UI during frontend development. Take screenshots iteratively to verify visual output matches requirements.

## When to Use

- After creating or modifying a page/component
- To verify layout, styling, and visual states
- To check responsive behavior
- Before reporting completion to user

## Quick Validation (Single Screenshot)

For quick checks, use Playwright's browser tools directly:

```bash
# Navigate and take screenshot
cd client && bunx playwright test test/components/<page>.spec.ts
```

Or create a quick one-off test:

```typescript
// test/components/<page>.spec.ts
import { test } from '@playwright/test';

test('validate <page>', async ({ page }) => {
    await page.goto('/<route>');
    await page.waitForLoadState('domcontentloaded');
    await page.waitForTimeout(500);
    
    await page.screenshot({
        path: 'test/resources/screenshots/<page>/validation.png',
        fullPage: true
    });
});
```

Run with:
```bash
bun run test:components
```

## Creating Screenshot Tests for a Page

### 1. Create Test File

Location: `client/test/components/<page>.spec.ts`

```typescript
import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Screenshots folder: test/resources/screenshots/<page>/
const screenshotsDir = join(__dirname, '../resources/screenshots/<page>');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function getScreenshotPath(name: string, projectName: string): string {
    const suffix = projectName.replace(/\s+/g, '-').toLowerCase();
    return join(screenshotsDir, `${name}-${suffix}.png`);
}

test.describe('<Page> Screenshots', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/<route>');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
    });

    test('default state', async ({ page }, testInfo) => {
        const screenshotPath = getScreenshotPath('01-default', testInfo.project.name);
        await page.screenshot({ path: screenshotPath, fullPage: true });
        expect(existsSync(screenshotPath)).toBeTruthy();
    });

    // Add more states as needed...
});
```

### 2. Run Tests

```bash
cd client

# Run all component tests
bun run test:components

# Run specific test file
bunx playwright test test/components/<page>.spec.ts
```

### 3. View Screenshots

Screenshots are saved to: `client/test/resources/screenshots/<page>/`

## Common UI States to Capture

### Forms
```typescript
test('empty form', async ({ page }, testInfo) => { /* ... */ });
test('filled form', async ({ page }, testInfo) => { /* ... */ });
test('validation errors', async ({ page }, testInfo) => {
    await page.getByRole('button', { name: 'Submit' }).click();
    await page.waitForTimeout(300); // Wait for error states
    // Screenshot...
});
test('loading state', async ({ page }, testInfo) => { /* ... */ });
test('success state', async ({ page }, testInfo) => { /* ... */ });
```

### Interactive Elements
```typescript
test('hover state', async ({ page }, testInfo) => {
    await page.getByRole('button', { name: 'Action' }).hover();
    // Screenshot...
});

test('focus state', async ({ page }, testInfo) => {
    await page.getByLabel('Email').focus();
    // Screenshot...
});

test('dropdown open', async ({ page }, testInfo) => {
    await page.getByRole('combobox').click();
    await page.waitForTimeout(300);
    // Screenshot...
});

test('modal open', async ({ page }, testInfo) => {
    await page.getByRole('button', { name: 'Open Modal' }).click();
    await page.waitForSelector('[role="dialog"]');
    // Screenshot...
});
```

### Data States
```typescript
test('empty state', async ({ page }, testInfo) => { /* no data */ });
test('loading state', async ({ page }, testInfo) => { /* skeleton/spinner */ });
test('with data', async ({ page }, testInfo) => { /* populated */ });
test('error state', async ({ page }, testInfo) => { /* API error */ });
```

## Iterative Development Workflow

When building a new page:

1. **Create basic structure** → Take screenshot → Verify layout
2. **Add styling** → Take screenshot → Verify appearance
3. **Add interactivity** → Take screenshots of states → Verify behavior
4. **Add responsive styles** → Test mobile viewport → Verify responsiveness

### Adding Mobile Viewport Test

```typescript
// In playwright.config.ts, there's Desktop Chrome configured
// For mobile, add to your test:

test('mobile view', async ({ browser }, testInfo) => {
    const context = await browser.newContext({
        viewport: { width: 375, height: 667 },
        userAgent: 'Mobile'
    });
    const page = await context.newPage();
    await page.goto('/<route>');
    await page.waitForLoadState('domcontentloaded');
    
    const screenshotPath = getScreenshotPath('mobile', testInfo.project.name);
    await page.screenshot({ path: screenshotPath, fullPage: true });
    
    await context.close();
});
```

## Playwright Config Reference

Location: `client/playwright.config.ts`

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './test',
    outputDir: './test/resources/test-results',
    timeout: 60000,
    fullyParallel: false,
    retries: 2,
    workers: 2,
    reporter: [['html', { outputFolder: './test/resources/report', open: 'never' }], ['list']],
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure'
    },
    projects: [
        {
            name: 'Desktop Chrome',
            use: { ...devices['Desktop Chrome'] }
        }
    ],
    webServer: {
        command: 'bun run dev',
        url: 'http://localhost:5173',
        timeout: 120 * 1000
    }
});
```

## Folder Structure

```
client/
├── test/
│   ├── components/
│   │   ├── login.spec.ts
│   │   ├── dashboard.spec.ts
│   │   └── <page>.spec.ts
│   └── resources/              # (gitignored)
│       ├── screenshots/
│       │   ├── login/
│       │   │   ├── 01-default-desktop-chrome.png
│       │   │   └── ...
│       │   └── <page>/
│       ├── report/
│       └── test-results/
└── playwright.config.ts
```

## Authenticated Pages

For pages requiring authentication, start the backend and login first:

### 1. Start Backend

```bash
cd /opt/hawaii/workspace/eventify/server && ./gradlew bootRun
```

Wait for server to be ready (check health endpoint or wait ~30 seconds).

### 2. Login in beforeEach

```typescript
test.describe('[Authenticated Page] Screenshots', () => {
    test.beforeEach(async ({ page }) => {
        // Go to login page
        await page.goto('/login');
        await page.waitForLoadState('domcontentloaded');
        
        // Dev credentials are prefilled in the UI, just click login
        await page.getByRole('button', { name: 'Sign In' }).click();
        
        // Wait for redirect to dashboard
        await page.waitForURL('/dashboard');
    });
    
    test('authenticated page', async ({ page }, testInfo) => {
        await page.goto('/developer');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
        
        const screenshotPath = getScreenshotPath('01-default', testInfo.project.name);
        await page.screenshot({ path: screenshotPath, fullPage: true });
        expect(existsSync(screenshotPath)).toBeTruthy();
    });
});
```

## CRITICAL: No Mock Data

**NEVER use mock HTML or fake data in screenshot tests.**

```typescript
// ❌ WRONG - Don't do this!
const mockHtml = `<html><body>Fake content</body></html>`;
await page.setContent(mockHtml);

// ❌ WRONG - Don't do this!
const fakeData = [{ name: 'Test', value: 123 }];
await page.evaluate((data) => { ... }, fakeData);

// ✅ CORRECT - Navigate to real pages
await page.goto('/developer');
await page.waitForLoadState('domcontentloaded');
await page.screenshot({ path: screenshotPath, fullPage: true });
```

## Tips

1. **Wait for animations**: Use `await page.waitForTimeout(500)` after navigation
2. **Wait for elements**: Use `await page.waitForSelector('.element')` before screenshots
3. **Full page vs viewport**: Use `fullPage: true` for scrollable content
4. **Naming convention**: Use numbered prefixes for ordering (01-, 02-, etc.)
5. **Clean screenshots**: Clear test data or use consistent mock data

## Troubleshooting

### Tests timeout
- Increase timeout in config: `timeout: 60000`
- Check if dev server is running
- Use `await page.waitForLoadState('domcontentloaded')` instead of `networkidle`

### Element not found
- Use `await page.waitForSelector()` before interacting
- Check selector is correct with Playwright UI: `bun run test --ui`

### Screenshots look wrong
- Wait for animations/transitions to complete
- Check viewport size in config
- Ensure fonts/images are loaded
