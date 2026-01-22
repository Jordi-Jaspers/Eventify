---
name: ui-validation
description: Use Playwright to validate UI during frontend development. Supports iterative ui-polish-loop for autonomous UI improvement cycles.
metadata:
  skill-type: frontend
  framework: playwright
  loop-compatible: true
---
# UI Validation

Use Playwright to validate UI during frontend development. Take screenshots iteratively to verify visual output matches requirements.

## CRITICAL: When This Skill Is Loaded

**ALWAYS use the ui-polish-loop for UI validation tasks unless explicitly told otherwise.**

When you load this skill (or when a user asks for UI validation), you MUST:

1. **Immediately check**: Is this a UI validation/improvement task?
2. **If YES**: Use the ui-polish-loop script (see below)
3. **If NO**: User is just asking questions about the skill - answer normally

### Decision Tree

```
User asks for UI validation/screenshot tests?
├─ YES → Use ui-polish-loop.sh script
│        └─ Run iterative improvement cycle
│
└─ NO → User is asking about the skill itself
        └─ Answer questions normally
```

**Example prompts that REQUIRE ui-polish-loop:**
- "Validate the dashboard UI"
- "Check the login page screenshots"
- "Perform UI validation on organization settings"
- "Review the UI and make improvements"
- "Take screenshots and fix any issues"
- "Run UI validation on landing page"

**Example prompts that DON'T require ui-polish-loop:**
- "How do I write a Playwright test?"
- "What's the ui-polish-loop command?"
- "Where are screenshots saved?"

## When to Use

- After creating or modifying a page/component
- To verify layout, styling, and visual states
- To check responsive behavior
- Before reporting completion to user
- **In a ui-polish-loop for autonomous UI improvement** (DEFAULT for validation tasks)

## UI Polish Loop Integration

**This is the PRIMARY way to use this skill for validation tasks.**

This skill supports the ui-polish-loop pattern for iterative UI improvement.

### Command Syntax

```bash
# From project root
./.opencode/scripts/ui-polish-loop.sh <page> <test-file> [max-iterations]
```

### Examples

```bash
# Landing page with 10 iterations (default)
./.opencode/scripts/ui-polish-loop.sh landing client/test/components/landing.spec.ts 10

# Dashboard page with 5 iterations
./.opencode/scripts/ui-polish-loop.sh dashboard client/test/components/dashboard.spec.ts 5

# Login page with default iterations
./.opencode/scripts/ui-polish-loop.sh login client/test/components/login.spec.ts
```

### Parameters

| Parameter | Required | Description |
|-----------|----------|-------------|
| `page` | Yes | Page name (used for screenshot folder organization) |
| `test-file` | Yes | Path to Playwright test file (from project root) |
| `max-iterations` | No | Maximum iterations (default: 5) |

### Loop Workflow

Each iteration of the ui-polish-loop:
1. **Run tests** → Capture fresh screenshots
2. **Analyze** → Read and critique the screenshots
3. **Identify** → Find specific improvements needed
4. **Implement** → Make targeted changes to components (edit Svelte files, CSS, etc.)
5. **Verify** → Re-run tests to confirm fixes
6. **Repeat** → Continue until quality threshold met or max iterations reached

**Key Point:** The loop allows you to make ACTUAL UI changes (not just test fixes) and verify them iteratively.

### Completion Signals

The script uses these signals to control the loop:

| Signal | Meaning |
|--------|---------|
| `UI_VALIDATION_COMPLETE` | All screenshots passed validation, exit loop |
| `UI_VALIDATION_BLOCKED` | Stuck, needs human intervention |
| `FIXES_APPLIED` | Fixes were made, continue to next iteration |
| `FIXES_BLOCKED` | Could not apply fixes |

## Running Tests

The test command automatically starts the backend if not running, waits for health check, then runs Playwright tests.

```bash
cd client

# Run ALL component tests
bun run test

# Run specific test file
bun run test -- test/components/developer.spec.ts

# Run tests matching a pattern
bun run test -- --grep "Developer"

# Run tests matching pattern in specific file
bun run test -- test/components/login.spec.ts --grep "default"
```

The script (`scripts/playwright-test.sh`) handles:
1. Check if backend is running (health endpoint)
2. Start backend if needed (./gradlew bootRun)
3. Wait for backend to be ready
4. Run Playwright tests with all passed arguments
5. Clean up backend process on exit

## Quick Validation (Single Screenshot)

For quick checks, create a simple test:

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
bun run test -- test/components/<page>.spec.ts
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
bun run test

# Run specific test file
bun run test -- test/components/<page>.spec.ts

# Run with pattern matching
bun run test -- --grep "<Page>"
```

### 3. View Screenshots

Screenshots are saved to: `client/test/resources/screenshots/<page>/`

## Screenshot Analysis Checklist

When critiquing screenshots in a ui-polish-loop, evaluate:

### Layout & Structure
- [ ] Proper spacing and margins
- [ ] Consistent padding
- [ ] Aligned elements
- [ ] Logical visual hierarchy
- [ ] Responsive behavior (if testing multiple viewports)

### Overflow & Overlap Issues (CRITICAL)
- [ ] **No text bleeding into adjacent columns** (e.g., long badges like "ORGANIZATION" overlapping date columns)
- [ ] **Table/grid columns have adequate width** for their content
- [ ] **Badges fit within their designated column** without overflow
- [ ] **No truncated text cutting off critical information** (dates, names, IDs)
- [ ] **Grid colSpan values match actual content requirements**
- [ ] Elements don't overlap each other unexpectedly

### Table Header/Content Alignment (CRITICAL)
- [ ] **Table headers are perfectly aligned with their column content below**
- [ ] **Headers and rows use the same grid structure** (identical grid-cols, identical gap values)
- [ ] **Headers and rows have identical horizontal padding** (e.g., both use px-4)
- [ ] **Account for row card borders** - if rows have borders/rounded corners, ensure they don't cause visual offset
- [ ] **First column content starts at same position as first column header**
- [ ] **Vertical alignment is consistent** between header and content rows

### Typography
- [ ] Readable font sizes
- [ ] Proper line height
- [ ] Consistent font weights
- [ ] Sufficient contrast

### Colors & Styling
- [ ] Color scheme consistency
- [ ] Proper use of accent colors
- [ ] Shadows and depth
- [ ] Border styles

### Interactive Elements
- [ ] Button states visible
- [ ] Focus indicators
- [ ] Hover states (if applicable)
- [ ] Loading states

### Content States
- [ ] Empty state handling
- [ ] Error state display
- [ ] Loading indicators
- [ ] Success feedback

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
├── playwright.config.ts
└── scripts/
    └── playwright-test.sh
```

## Authenticated Pages

For pages requiring authentication, the test must login first. The `bun run test` command automatically starts the backend.

### Login Pattern for Authenticated Pages

```typescript
test.describe('[Authenticated Page] Screenshots', () => {
    // Increase timeout since login adds time
    test.setTimeout(5000);

    test.beforeEach(async ({ page }) => {
        // Go to login page
        await page.goto('/login');
        await page.waitForLoadState('domcontentloaded');
        
        // Wait for dev credentials button and click it
        const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
        await fillButton.waitFor({ state: 'visible', timeout: 10000 });
        await fillButton.click();
        
        // Submit login form
        await page.getByRole('button', { name: 'Sign In' }).click();
        
        // Wait for redirect to dashboard (login success)
        await page.waitForURL('/dashboard', { timeout: 15000 });
        
        // Navigate to target page
        await page.goto('/developer');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(800);
    });
    
    test('authenticated page', async ({ page }, testInfo) => {
        const screenshotPath = getScreenshotPath('01-default', testInfo.project.name);
        await page.screenshot({ path: screenshotPath, fullPage: true });
        expect(existsSync(screenshotPath)).toBeTruthy();
    });
});
```

**Key points:**
- Use `test.setTimeout(5000)` to allow time for login
- Click "Fill Credentials" button (loads from backend dev endpoint)
- Wait for `/dashboard` redirect to confirm login success
- Then navigate to authenticated route

## CRITICAL: No Mock Data

**NEVER use mock HTML or fake data in screenshot tests.**

```typescript
// WRONG - Don't do this!
const mockHtml = `<html><body>Fake content</body></html>`;
await page.setContent(mockHtml);

// WRONG - Don't do this!
const fakeData = [{ name: 'Test', value: 123 }];
await page.evaluate((data) => { ... }, fakeData);

// CORRECT - Navigate to real pages
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

### UI-polish-loop not completing
- Check that tests are passing first
- Verify screenshots are being generated in `client/test/resources/screenshots/<page>/`
- Look for test failures blocking the loop
- Try reducing scope of changes per iteration
