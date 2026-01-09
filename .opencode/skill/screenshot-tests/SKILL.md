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

## Complete Test Template

```typescript
import { test, expect } from '@playwright/test';
import { existsSync, mkdirSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const screenshotsDir = join(__dirname, '../resources/screenshots/[page]');
if (!existsSync(screenshotsDir)) {
    mkdirSync(screenshotsDir, { recursive: true });
}

function screenshotPath(name: string, project: string): string {
    return join(screenshotsDir, `${name}-${project.replace(/\s+/g, '-').toLowerCase()}.png`);
}

test.describe('[Page] Screenshots', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/[route]');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
    });

    test('default state', async ({ page }, testInfo) => {
        const path = screenshotPath('01-default', testInfo.project.name);
        await page.screenshot({ path, fullPage: true });
        expect(existsSync(path)).toBeTruthy();
    });

    // Add more states as needed
});
```

## Authenticated Pages

For pages behind login:

```typescript
test.describe('[Page] Screenshots', () => {
    test.setTimeout(30000); // Allow time for login

    test.beforeEach(async ({ page }) => {
        // Login flow
        await page.goto('/login');
        await page.waitForLoadState('domcontentloaded');
        
        // Use dev credentials button
        const fillButton = page.getByRole('button', { name: 'Fill Credentials' });
        await fillButton.waitFor({ state: 'visible', timeout: 10000 });
        await fillButton.click();
        
        // Submit
        await page.getByRole('button', { name: 'Sign In' }).click();
        
        // Wait for redirect
        await page.waitForURL('/dashboard', { timeout: 15000 });
        
        // Navigate to target page
        await page.goto('/[target-route]');
        await page.waitForLoadState('domcontentloaded');
        await page.waitForTimeout(500);
    });

    test('default state', async ({ page }, testInfo) => {
        const path = screenshotPath('01-default', testInfo.project.name);
        await page.screenshot({ path, fullPage: true });
        expect(existsSync(path)).toBeTruthy();
    });
});
```

## Common States to Capture

### Forms
```typescript
test('empty form', async ({ page }, testInfo) => {
    const path = screenshotPath('01-empty-form', testInfo.project.name);
    await page.screenshot({ path, fullPage: true });
});

test('validation errors', async ({ page }, testInfo) => {
    await page.getByRole('button', { name: 'Submit' }).click();
    await page.waitForTimeout(300);
    const path = screenshotPath('02-validation-errors', testInfo.project.name);
    await page.screenshot({ path, fullPage: true });
});
```

### Modals/Dialogs
```typescript
test('modal open', async ({ page }, testInfo) => {
    await page.getByRole('button', { name: 'Open Dialog' }).click();
    await page.waitForSelector('[role="dialog"]');
    await page.waitForTimeout(300);
    const path = screenshotPath('03-modal-open', testInfo.project.name);
    await page.screenshot({ path, fullPage: true });
});
```

### Data States
```typescript
test('loading state', async ({ page }, testInfo) => {
    // Intercept to delay response
    await page.route('**/api/**', route => 
        new Promise(resolve => setTimeout(() => resolve(route.continue()), 2000))
    );
    await page.reload();
    const path = screenshotPath('04-loading', testInfo.project.name);
    await page.screenshot({ path, fullPage: true });
});

test('empty state', async ({ page }, testInfo) => {
    // Navigate to page with no data
    const path = screenshotPath('05-empty', testInfo.project.name);
    await page.screenshot({ path, fullPage: true });
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

2. **Wait for content** - Let page settle
   ```typescript
   await page.waitForLoadState('domcontentloaded');
   await page.waitForTimeout(500);
   ```

3. **Use numbered prefixes** - For ordering
   ```
   01-default-desktop-chrome.png
   02-validation-errors-desktop-chrome.png
   03-modal-open-desktop-chrome.png
   ```

4. **Full page screenshots** - Capture everything
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

## Folder Structure

```
client/
├── test/
│   ├── components/
│   │   ├── login.spec.ts
│   │   ├── dashboard.spec.ts
│   │   └── [page].spec.ts      ← Create this
│   └── resources/
│       └── screenshots/
│           ├── login/
│           ├── dashboard/
│           └── [page]/         ← Generated here
└── playwright.config.ts
```

## Troubleshooting

### Tests timeout
- Increase timeout: `test.setTimeout(30000)`
- Check backend is running
- Use `domcontentloaded` not `networkidle`

### Screenshots blank
- Wait longer: `await page.waitForTimeout(1000)`
- Check route exists
- Verify login succeeded (for auth pages)

### Element not found
- Wait for element: `await page.waitForSelector('.element')`
- Check selector syntax
- Verify element exists in DOM

## Remember

- **Create tests for new pages** - Mandatory
- **Run tests before returning** - Must pass
- **Don't iterate on visuals** - UI Agent does that
- **Include test file in output** - Orchestrator needs it
