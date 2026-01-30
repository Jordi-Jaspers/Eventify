# Eventify Styling Guide

This guide defines the visual design system for the Eventify frontend.

**Brand Identity:** Modern security monitoring service - professional, trustworthy, precise.

## Framework

- **UI Framework:** SvelteKit 2.x with Svelte 5
- **CSS Framework:** TailwindCSS v4
- **Component Library:** shadcn-svelte
- **Icons:** lucide-svelte

## Design Philosophy

- **Professional:** Clean, modern aesthetic that conveys trust and security
- **Minimal:** Reduce visual noise - every element has a purpose
- **Precise:** Sharp edges, clear hierarchy, no ambiguity
- **Scannable:** Dashboard-style layouts for quick situational awareness
- **Progressive disclosure:** Show controls on hover, keep interface uncluttered

## Visual Identity

### Aesthetic

Think: Security operations center, modern fintech dashboard, professional monitoring tools.

- **Dark mode first:** Dark backgrounds with high-contrast text (security dashboards are typically dark)
- **Subtle depth:** Use `border-border/30` and `bg-muted/20` for layering, not heavy shadows
- **Accent colors:** Primary blue for actions, semantic colors for status (green/yellow/red)
- **Typography:** Clean, readable, slightly technical feel

### Do's
- ✅ Use subtle borders to create visual separation
- ✅ Leverage hover states for progressive disclosure
- ✅ Keep data-dense layouts scannable
- ✅ Use status colors consistently (success/warning/error)
- ✅ Make interactive elements feel responsive
- ✅ Use glassmorphism for buttons (transparency + backdrop-blur)
- ✅ Keep styling consistent between light and dark modes
- ✅ Use `text-primary` for page titles
- ✅ Reference `/dev-playbook` for component patterns

### Don'ts
- ❌ Heavy drop shadows (looks dated)
- ❌ Gradients on buttons (use glassmorphism instead)
- ❌ Gradients on page titles (use `text-primary` instead)
- ❌ Rounded corners everywhere (keep it sharp, professional)
- ❌ Too much whitespace (density is expected in dashboards)
- ❌ Decorative elements without purpose
- ❌ Different button styles between light/dark modes
- ❌ Custom button/logo markup (use Button, AppLogo components)

---

## Design Tokens

### Colors

#### Dark Mode Palette (Primary)

The dark mode uses a professional, security-dashboard aesthetic with neutral dark backgrounds and blue-teal accents:

```css
.dark {
  /* Background - Deep, neutral dark (security dashboard feel) */
  --background: hsl(220 20% 8%);
  --foreground: hsl(210 20% 92%);
  
  /* Card - Slightly elevated from background */
  --card: hsl(220 18% 10%);
  --card-foreground: hsl(210 20% 92%);
  
  /* Popover - Darkest layer */
  --popover: hsl(220 22% 6%);
  --popover-foreground: hsl(210 20% 92%);
  
  /* Primary - Professional blue (trust, security) */
  --primary: hsl(205 85% 50%);
  --primary-foreground: hsl(220 20% 98%);
  
  /* Secondary - Subtle elevation */
  --secondary: hsl(220 16% 14%);
  --secondary-foreground: hsl(210 20% 88%);
  
  /* Muted - For backgrounds and disabled states */
  --muted: hsl(220 14% 12%);
  --muted-foreground: hsl(215 15% 55%);
  
  /* Accent - Teal for highlights (professional, modern) */
  --accent: hsl(175 70% 45%);
  --accent-foreground: hsl(220 20% 98%);
  
  /* Destructive - Clear danger signal */
  --destructive: hsl(0 70% 50%);
  --destructive-foreground: hsl(0 0% 98%);
  
  /* Borders - Subtle but visible */
  --border: hsl(220 15% 18%);
  --input: hsl(220 15% 18%);
  --ring: hsl(205 85% 50%);
  
  /* Charts - Professional data visualization palette */
  --chart-1: hsl(205 85% 55%);  /* Blue */
  --chart-2: hsl(175 70% 50%);  /* Teal */
  --chart-3: hsl(45 90% 55%);   /* Amber */
  --chart-4: hsl(280 60% 60%);  /* Purple */
  --chart-5: hsl(0 70% 55%);    /* Red */
  
  /* Sidebar - Slightly different from main background */
  --sidebar: hsl(220 22% 7%);
  --sidebar-foreground: hsl(210 20% 90%);
  --sidebar-primary: hsl(205 85% 50%);
  --sidebar-primary-foreground: hsl(220 20% 98%);
  --sidebar-accent: hsl(220 14% 14%);
  --sidebar-accent-foreground: hsl(210 20% 88%);
  --sidebar-border: hsl(220 15% 15%);
  --sidebar-ring: hsl(205 85% 50%);
}
```

#### Accent Color Usage

| Color | HSL | Use Case |
|-------|-----|----------|
| Primary Blue | `hsl(205 85% 50%)` | CTAs, links, focus states |
| Accent Teal | `hsl(175 70% 45%)` | Secondary highlights, gradients |
| Destructive Red | `hsl(0 70% 50%)` | Errors, delete actions |

#### Gradient Effects

Use gradients sparingly - only for decorative background elements, NOT for buttons or page titles:

```css
/* Gradient orbs (background decoration only) */
.gradient-orb-1 { background: radial-gradient(circle, hsl(205 85% 50% / 0.15) 0%, transparent 70%); }
.gradient-orb-2 { background: radial-gradient(circle, hsl(175 70% 45% / 0.12) 0%, transparent 70%); }
```

**Note:** 
- Do NOT use gradients on buttons - use glassmorphism instead (see Button section below)
- Do NOT use gradients on page titles - use `text-primary` instead
- Gradient text is acceptable only in hero sections on marketing pages

#### Semantic Colors

```css
/* Status colors */
--color-success: hsl(142 70% 45%);  /* Green */
--color-warning: hsl(45 90% 55%);   /* Amber */
--color-error: hsl(0 70% 50%);      /* Red */
--color-info: hsl(205 85% 50%);     /* Blue */
```

### Spacing Scale

```
xs: 0.25rem (4px)   → gap-1, p-1
sm: 0.5rem (8px)    → gap-2, p-2
md: 1rem (16px)     → gap-4, p-4
lg: 1.5rem (24px)   → gap-6, p-6
xl: 2rem (32px)     → gap-8, p-8
2xl: 3rem (48px)    → gap-12, p-12
```

### Typography

#### Size Scale

```
xs: 0.75rem (12px)  → text-xs
sm: 0.875rem (14px) → text-sm
base: 1rem (16px)   → text-base
lg: 1.125rem (18px) → text-lg
xl: 1.25rem (20px)  → text-xl
2xl: 1.5rem (24px)  → text-2xl
3xl: 1.875rem (30px) → text-3xl
4xl: 2.25rem (36px) → text-4xl
5xl: 3rem (48px)    → text-5xl
```

#### Font Weights

| Weight | Class | Usage |
|--------|-------|-------|
| Light | `font-light` | Logo text only |
| Normal | `font-normal` | Body text (default) |
| Medium | `font-medium` | Labels, form fields |
| Semibold | `font-semibold` | Section headings, subheadings |
| Bold | `font-bold` | Page titles, stat values |

#### Text Colors

| Color | Class | Usage |
|-------|-------|-------|
| Foreground | `text-foreground` | Primary text |
| Muted | `text-muted-foreground` | Secondary text, descriptions |
| Primary | `text-primary` | Links, accents, page titles |
| Destructive | `text-destructive` | Errors, required markers |
| Success | `text-green-500` | Success messages, positive changes |
| Warning | `text-amber-500` | Warnings, cautions |

---

## Typography Patterns (Use Cases)

Reference the dev playbook at `/dev-playbook` for live examples.

### Page Title

Main heading for a page. Uses `text-primary` for visual distinction.

```svelte
<h1 class="text-3xl font-bold text-primary">
  Organization Channels
</h1>
<p class="text-muted-foreground mt-2">
  Manage channels for your organization
</p>
```

### Page Header with Icon

Alternative style with an icon badge.

```svelte
<div class="flex items-center gap-3">
  <div class="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
    <Settings class="h-5 w-5 text-primary" />
  </div>
  <div>
    <h1 class="text-2xl font-semibold">Dashboard</h1>
    <p class="text-sm text-muted-foreground">Monitor your events in real-time.</p>
  </div>
</div>
```

### Card Header

Title and description inside a Card component.

```svelte
<CardHeader>
  <CardTitle class="text-lg font-semibold">Event Statistics</CardTitle>
  <CardDescription class="text-sm text-muted-foreground">
    Overview of events in the last 24 hours.
  </CardDescription>
</CardHeader>
```

### Section Title

Subheading within a page.

```svelte
<h2 class="text-xl font-semibold">Recent Activity</h2>
<p class="text-sm text-muted-foreground">Your latest events and updates.</p>
```

### Stat/Metric Card

Display numeric values with labels.

```svelte
<div class="space-y-1">
  <p class="text-sm font-medium text-muted-foreground">Total Events</p>
  <p class="text-3xl font-bold">12,456</p>
  <p class="text-xs text-green-500">+12.5% from last week</p>
</div>
```

### Form Labels

Labels, hints, and validation messages.

```svelte
<div class="space-y-1">
  <label class="text-sm font-medium">Email Address</label>
  <p class="text-xs text-muted-foreground">We'll never share your email.</p>
</div>

<!-- Error state -->
<div class="space-y-1">
  <label class="text-sm font-medium">
    Password <span class="text-destructive">*</span>
  </label>
  <p class="text-xs text-destructive">Password is required.</p>
</div>
```

### Empty State

No data messaging.

```svelte
<div class="text-center py-8 space-y-2">
  <Inbox class="h-12 w-12 text-muted-foreground/50 mx-auto" />
  <p class="text-lg font-medium">No events found</p>
  <p class="text-sm text-muted-foreground">
    Start by creating your first channel to receive events.
  </p>
</div>
```

### Alerts & Toasts

Status messages with semantic colors.

```svelte
<!-- Success -->
<div class="p-3 rounded-md bg-green-500/10 border border-green-500/30">
  <p class="text-sm font-medium text-green-500">Success!</p>
  <p class="text-sm text-muted-foreground">Your changes have been saved.</p>
</div>

<!-- Error -->
<div class="p-3 rounded-md bg-destructive/10 border border-destructive/30">
  <p class="text-sm font-medium text-destructive">Error</p>
  <p class="text-sm text-muted-foreground">Failed to save changes.</p>
</div>

<!-- Warning -->
<div class="p-3 rounded-md bg-amber-500/10 border border-amber-500/30">
  <p class="text-sm font-medium text-amber-500">Warning</p>
  <p class="text-sm text-muted-foreground">This action cannot be undone.</p>
</div>
```

### Table Headers & Cells

Data table styling.

```svelte
<thead class="bg-muted/30">
  <tr>
    <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-3 py-2">
      Name
    </th>
  </tr>
</thead>
<tbody>
  <tr class="border-t border-border/50">
    <td class="text-sm font-medium px-3 py-2">Production</td>
    <td class="text-sm text-green-500 px-3 py-2">Active</td>
    <td class="text-sm text-muted-foreground text-right px-3 py-2">1,234</td>
  </tr>
</tbody>
```

### Badges & Tags

Status pills with semantic colors.

```svelte
<span class="text-xs font-medium px-2 py-0.5 rounded-full bg-primary/20 text-primary">Primary</span>
<span class="text-xs font-medium px-2 py-0.5 rounded-full bg-green-500/20 text-green-500">Success</span>
<span class="text-xs font-medium px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-500">Warning</span>
<span class="text-xs font-medium px-2 py-0.5 rounded-full bg-destructive/20 text-destructive">Error</span>
<span class="text-xs font-medium px-2 py-0.5 rounded-full bg-muted text-muted-foreground">Muted</span>
```

### Special Text Styles

```svelte
<!-- Logo style -->
<span class="text-2xl font-light tracking-wide">eventify</span>

<!-- Section label -->
<span class="text-xs uppercase tracking-widest text-muted-foreground">Section Label</span>

<!-- Code/technical -->
<code class="font-mono text-sm bg-muted/30 px-2 py-1 rounded">code.example()</code>

<!-- Long-form content -->
<p class="text-sm leading-relaxed">Long paragraphs with relaxed line height...</p>

<!-- Inline link -->
<span class="text-primary hover:underline cursor-pointer">Link text</span>
```

---

## Component Patterns

### Dev Playbook

**Always reference the dev playbook at `/dev-playbook`** (visible in dev mode only) for:
- Live examples of all components
- Typography patterns and use cases
- Color schemes and semantic colors
- Button variants and sizes
- Card styles

The playbook is the source of truth for component usage.

### Component Library Reference

| Component | Import | Purpose |
|-----------|--------|---------|
| `Button` | `$lib/components/ui/button/button.svelte` | All clickable actions |
| `AppLogo` | `$lib/components/layout/AppLogo.svelte` | Branding/logo |
| `Card` | `$lib/components/ui/card` | Content containers |
| `Badge` | `$lib/components/ui/badge` | Status indicators |
| `Input` | `$lib/components/ui/input` | Form inputs |
| `Label` | `$lib/components/ui/label` | Form labels |
| `Alert` | `$lib/components/ui/alert` | Notifications |

**ALWAYS use these components.** Never write custom markup for common UI elements.

### Logo (AppLogo)

Minimalistic logo with Radar icon - modern, secure, monitoring-focused. No container/box around the icon.

```svelte
import AppLogo from '$lib/components/layout/AppLogo.svelte';

<!-- Full logo with subtitle -->
<AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />

<!-- Size variants -->
<AppLogo size="small" />   <!-- Sidebar -->
<AppLogo size="medium" />  <!-- Auth pages -->
<AppLogo size="large" />   <!-- Landing page -->

<!-- Display variants -->
<AppLogo variant="full" />  <!-- Icon + text (default) -->
<AppLogo variant="icon" />  <!-- Icon only -->
<AppLogo variant="text" />  <!-- Text only -->

<!-- With link -->
<AppLogo href="/" />
```

**Typography:** Light weight (`font-light`), wide tracking (`tracking-wide`), lowercase "eventify"

### Auth Page Layout (CRITICAL - Must Be Consistent)

All authentication pages (login, register, forgot-password, reset-password, verify) MUST follow this exact layout pattern:

```svelte
<!-- Content Container -->
<div class="max-w-md mx-auto">
    <!-- Logo/Branding Section - MUST be centered -->
    <div class="mb-8 text-center">
        <AppLogo size="medium" subtitle="Your subtitle here"/>
    </div>

    <!-- Card -->
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
        <!-- Card content -->
    </Card>

    <!-- Footer -->
    <p class="text-center text-xs text-muted-foreground mt-6">
        Footer text here
    </p>
</div>
```

**Critical requirements:**
- Logo wrapper MUST have `text-center` class
- Container MUST have `max-w-md mx-auto`
- Card styling should be consistent across all auth pages
- Footer should be centered

**Why this matters:** Inconsistent logo alignment (e.g., centered on login but left-aligned on register) creates a jarring user experience and looks unprofessional.

### Buttons (Glassmorphism)

All primary and destructive buttons use glassmorphism - semi-transparent with backdrop blur. This creates a modern, unified look across light and dark modes.

```svelte
<!-- Primary CTA (glassmorphism is the default) -->
<Button>Sign In</Button>

<!-- Destructive action (also glassmorphism) -->
<Button variant="destructive">Delete</Button>

<!-- Other variants (unchanged) -->
<Button variant="outline">Cancel</Button>
<Button variant="secondary">Save Draft</Button>
<Button variant="ghost">Edit</Button>
<Button variant="link">Learn more</Button>
```

#### Button Glassmorphism Styling

| Variant | Light Mode | Dark Mode |
|---------|------------|-----------|
| `default` | `bg-primary/80` | `bg-primary/40` |
| `destructive` | `bg-destructive/80` | `bg-destructive/40` |

Both include:
- `backdrop-blur-md` for frosted glass effect
- `border` with matching color at partial opacity
- Hover increases opacity slightly

**Why glassmorphism?**
- Unified appearance across light/dark modes
- Modern, professional look
- Subtle depth without heavy shadows
- Clear visual hierarchy

### Minimal Card

```svelte
<Card class="border-border/50">
  <CardHeader>
    <CardTitle class="text-lg">Title</CardTitle>
  </CardHeader>
  <CardContent class="space-y-4">
    <!-- content -->
  </CardContent>
</Card>
```

### Button Usage Examples

```svelte
<!-- Primary action (glassmorphism default) -->
<Button>
  <Plus class="h-4 w-4 mr-2" />
  Add Item
</Button>

<!-- Secondary/outline action -->
<Button variant="outline">
  <Settings class="h-4 w-4 mr-2" />
  Settings
</Button>

<!-- Destructive action (glassmorphism) -->
<Button variant="destructive">
  <Trash class="h-4 w-4 mr-2" />
  Delete
</Button>

<!-- Ghost for subtle actions -->
<Button variant="ghost" size="icon">
  <X class="h-4 w-4" />
</Button>
```

### List Item Row (Preferred Pattern)

**Use this for DataTable rows, list items, and any scannable data.** This minimal pattern is preferred over heavier card-style rows with borders and backgrounds.

```svelte
<!-- Basic list item row -->
<div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
  <!-- Icon - subtle -->
  <Radio class="h-4 w-4 text-primary/70 shrink-0" />

  <!-- Content -->
  <div class="flex-1 min-w-0">
    <p class="text-sm font-medium truncate text-foreground">{item.name}</p>
    <p class="text-xs text-muted-foreground truncate">{item.description}</p>
  </div>

  <!-- Status badge -->
  <Badge variant="success">Active</Badge>

  <!-- Actions - appear on hover -->
  <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
    <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-primary">
      <Edit class="h-4 w-4" />
    </Button>
    <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-destructive">
      <X class="h-4 w-4" />
    </Button>
  </div>
</div>
```

#### With Drag Handle (Reorderable)

```svelte
<div
  draggable="true"
  class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move"
>
  <!-- Drag handle - appears on hover -->
  <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
    <GripVertical class="h-4 w-4 text-muted-foreground/50" />
  </div>

  <!-- Icon -->
  <Radio class="h-4 w-4 text-primary/70 shrink-0" />

  <!-- Content -->
  <div class="flex-1 min-w-0">
    <p class="text-sm font-medium truncate">{item.name}</p>
    <p class="text-xs text-muted-foreground truncate">{item.description}</p>
  </div>

  <!-- Delete - appears on hover -->
  <Button
    variant="ghost"
    size="icon"
    class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all"
  >
    <X class="h-4 w-4" />
  </Button>
</div>
```

#### Collapsible Group Header

For grouped/nested items:

```svelte
<div class="rounded-md border border-border/30">
  <!-- Group header -->
  <div class="group flex items-center gap-3 px-3 py-2.5 bg-muted/20 hover:bg-muted/40 transition-all cursor-move">
    <!-- Drag handle -->
    <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
      <GripVertical class="h-4 w-4 text-muted-foreground/50" />
    </div>

    <!-- Expand/collapse -->
    <button class="shrink-0 text-muted-foreground hover:text-foreground">
      <ChevronDown class="h-4 w-4" />
    </button>

    <!-- Icon + name -->
    <Folder class="h-4 w-4 text-primary/70 shrink-0" />
    <div class="flex-1 min-w-0">
      <p class="font-medium text-sm truncate">{group.name}</p>
      <p class="text-xs text-muted-foreground">{group.items.length} items</p>
    </div>

    <!-- Delete -->
    <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive">
      <X class="h-4 w-4" />
    </Button>
  </div>

  <!-- Children (indented) -->
  <div class="pl-10 pr-3 pb-3 pt-2 space-y-1">
    <!-- Nested items here -->
  </div>
</div>
```

#### Standard Card Row

For overviews that need more visual weight while staying clean. Uses subtle border and background, slightly larger icon (no containers!), and visible actions.

```svelte
<!-- Standard card row - more substantial feel -->
<div class="group flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/30 hover:bg-card/50 hover:border-border transition-all">
  <!-- Icon - slightly larger, no container -->
  <Radio class="h-5 w-5 text-primary shrink-0" />

  <!-- Content -->
  <div class="flex-1 min-w-0">
    <p class="font-medium truncate">{item.name}</p>
    <p class="text-sm text-muted-foreground truncate">{item.description}</p>
  </div>

  <!-- Status -->
  <Badge variant="success">Active</Badge>

  <!-- Meta info -->
  <span class="text-sm text-muted-foreground whitespace-nowrap">Jan 15, 2026</span>

  <!-- Actions - visible but subtle -->
  <div class="flex items-center gap-1">
    <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
      <Edit class="h-4 w-4" />
    </Button>
    <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-destructive">
      <Trash2 class="h-4 w-4" />
    </Button>
  </div>
</div>
```

**Key differences from minimal:**
- Border: `border border-border/50`
- Background: `bg-card/30` (visible at rest)
- Icon: `h-5 w-5 text-primary` (slightly larger, no container)
- Padding: `px-4 py-3` (slightly more spacious)
- Actions: Always visible (not hover-reveal)

**When to use Standard vs Minimal:**
- **Standard:** Main list pages, dashboards, detail views where items need presence
- **Minimal:** Configuration panels, nested lists, drag-and-drop builders, dense data

#### Choosing the Right Style

| Aspect | Minimal | Standard | Avoid |
|--------|---------|----------|-------|
| Container | No border, `hover:bg-muted/50` | `border-border/50 bg-card/30` | `bg-card/50 backdrop-blur-sm` |
| Icon | `h-4 w-4 text-primary/70` | `h-5 w-5 text-primary` | Icon containers, gradients |
| Actions | Hover-reveal | Always visible | Heavy button styles |
| Padding | `px-3 py-2.5` | `px-4 py-3` | Excessive padding |
| Use case | Dense lists, builders | Main overviews | - |

**Never use:**
- Icon containers with backgrounds (`bg-primary/10`, gradients)
- Heavy backdrop blur on rows
- Oversized icons (h-10 w-10 or larger)

### Hover-Reveal Actions

Show secondary actions only on hover to reduce visual noise:

```svelte
<div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-colors">
  <!-- Drag handle - appears on hover -->
  <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
    <GripVertical class="h-4 w-4 text-muted-foreground/50" />
  </div>

  <!-- Content -->
  <div class="flex-1 min-w-0">
    <p class="text-sm truncate">{item.name}</p>
  </div>

  <!-- Delete - appears on hover -->
  <Button
    variant="ghost"
    size="icon"
    class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all"
  >
    <X class="h-4 w-4" />
  </Button>
</div>
```

### Draggable List Item

```svelte
<div
  draggable="true"
  ondragstart={handleDragStart}
  ondragend={handleDragEnd}
  ondragover={handleDragOver}
  ondrop={handleDrop}
  class:opacity-50={isDragging}
  class:border-primary={isDropTarget}
  class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move"
>
  <!-- Grip handle -->
  <GripVertical class="h-4 w-4 text-muted-foreground/50 opacity-0 group-hover:opacity-100" />
  
  <!-- Content -->
  <div class="flex-1">{item.name}</div>
</div>
```

### Collapsible Section

```svelte
<div class="rounded-md border border-border/30">
  <!-- Header -->
  <div class="flex items-center gap-3 px-3 py-2.5 bg-muted/20 hover:bg-muted/40 transition-colors">
    <button onclick={toggle} class="text-muted-foreground hover:text-foreground">
      {#if isExpanded}
        <ChevronDown class="h-4 w-4" />
      {:else}
        <ChevronRight class="h-4 w-4" />
      {/if}
    </button>
    <span class="font-medium text-sm">{title}</span>
  </div>
  
  <!-- Content -->
  {#if isExpanded}
    <div class="pl-10 pr-3 pb-3 pt-2 space-y-1">
      <!-- children -->
    </div>
  {/if}
</div>
```

### Dashed Add Button

```svelte
<button
  class="w-full text-left px-3 py-2 rounded-md border border-dashed border-border/50 hover:border-primary/50 hover:bg-muted/30 text-sm text-muted-foreground hover:text-primary transition-all flex items-center gap-2"
  onclick={onAdd}
>
  <Plus class="h-3.5 w-3.5" />
  Add Item
</button>
```

### Responsive Grid

```svelte
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  {#each items as item}
    <Card>...</Card>
  {/each}
</div>
```

### Data Table with Proper Alignment

```svelte
<!-- Header and rows MUST use identical grid structure -->
<div class="rounded-lg border border-border/50 overflow-hidden">
  <!-- Header -->
  <div class="grid grid-cols-[1fr_150px_120px_100px] gap-4 px-4 py-3 bg-muted/50 text-sm font-medium">
    <div>Name</div>
    <div>Role</div>
    <div>Status</div>
    <div class="text-right">Actions</div>
  </div>
  
  <!-- Rows - identical grid-cols, gap, and px values -->
  {#each items as item}
    <div class="grid grid-cols-[1fr_150px_120px_100px] gap-4 px-4 py-3 border-t border-border/30">
      <div class="truncate">{item.name}</div>
      <div><Badge>{item.role}</Badge></div>
      <div><Badge variant="outline">{item.status}</Badge></div>
      <div class="text-right">...</div>
    </div>
  {/each}
</div>
```

### Loading State

```svelte
{#if loading}
  <div class="space-y-3">
    {#each Array(3) as _}
      <div class="h-12 bg-muted/50 rounded-md animate-pulse" />
    {/each}
  </div>
{/if}
```

### Empty State

```svelte
{#if items.length === 0}
  <div class="flex flex-col items-center justify-center py-12 text-center">
    <Inbox class="h-12 w-12 text-muted-foreground/50 mb-4" />
    <h3 class="text-lg font-medium">No items found</h3>
    <p class="text-sm text-muted-foreground mt-1">Get started by creating your first item.</p>
    <Button class="mt-4" variant="outline">
      <Plus class="h-4 w-4 mr-2" />
      Create Item
    </Button>
  </div>
{/if}
```

### Error State

```svelte
{#if error}
  <div class="rounded-lg border border-destructive/50 bg-destructive/10 p-4">
    <div class="flex items-center gap-3">
      <AlertCircle class="h-5 w-5 text-destructive" />
      <div>
        <h4 class="font-medium text-destructive">Something went wrong</h4>
        <p class="text-sm text-muted-foreground mt-1">{error.message}</p>
      </div>
    </div>
  </div>
{/if}
```

---

## Shadcn Component Editing Strategy

Shadcn components are at `src/lib/components/ui/`. Edits affect **every page** using that component.

### Edit Base Component When

| Scenario | Example | Edit Location |
|----------|---------|---------------|
| Default styling is wrong | Badge text too small everywhere | `src/lib/components/ui/badge.svelte` |
| Missing variant needed app-wide | Need a `destructive` button variant | `src/lib/components/ui/button.svelte` |
| Accessibility fix | Focus states missing on all cards | `src/lib/components/ui/card.svelte` |
| Design token update | Primary color needs adjustment | Component + `app.css` |

### Edit Page-Level Usage When

| Scenario | Example | Edit Location |
|----------|---------|---------------|
| One-off styling | This card needs special border | `+page.svelte` - add classes to Card usage |
| Layout-specific spacing | Table columns for this data shape | `+page.svelte` - grid classes |
| Contextual overrides | Error state card needs red border | `+page.svelte` - conditional classes |

---

## Keyboard & Focus States

```svelte
<!-- Focusable elements need visible focus states -->
<button class="focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">
  Click me
</button>

<!-- Skip link for screen readers -->
<a href="#main-content" class="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-background focus:rounded-md">
  Skip to main content
</a>

<!-- Icon buttons need labels -->
<button aria-label="Close dialog">
  <X class="h-4 w-4" />
</button>
```

---

## Reduced Motion

```svelte
<div class="transition-transform motion-reduce:transition-none">
  <!-- Respects user preference -->
</div>
```

---

## Visual Polish Checklist

Before marking UI complete, verify:

### Viewport Integrity (CHECK FIRST)
- [ ] No horizontal scrolling on any viewport size
- [ ] Mobile screenshots are exactly 375px wide (not wider)
- [ ] If screenshot is wider than expected, there's horizontal overflow - FIX IT
- [ ] Common causes: fixed-width elements, absolute positioning, missing `overflow-x-hidden`

### Cross-Page Consistency
- [ ] Logo is centered on ALL auth pages (login, register, forgot-password, reset-password, verify)
- [ ] Similar pages follow identical layout patterns
- [ ] Card widths and spacing consistent across page types
- [ ] Compare screenshots of similar pages side-by-side

### Brand & Identity
- [ ] Professional, modern security service feel
- [ ] Dark mode works well (primary use case)
- [ ] Status colors used consistently (green=success, yellow=warning, red=error)
- [ ] No playful or casual elements (this is a professional tool)

### Design System
- [ ] Subtle borders for separation (`border-border/30`, `border-border/50`)
- [ ] Light backgrounds for layering (`bg-muted/20`, `bg-muted/50`)
- [ ] Hover states feel responsive (`hover:bg-muted/50`, `transition-colors`)
- [ ] Icons sized appropriately (h-4 w-4 for inline, h-5 w-5 for headers)
- [ ] Consistent spacing (`space-y-4`, `gap-4`, `px-3 py-2.5` for list items)
- [ ] Progressive disclosure (actions appear on hover)

### Layout
- [ ] Dashboard-style density (not too much whitespace)
- [ ] No text/badge overflow into adjacent columns
- [ ] Table columns have adequate width
- [ ] Table headers perfectly aligned with column content
- [ ] Responsive behavior works at all breakpoints

### States
- [ ] Loading states styled with skeleton/pulse
- [ ] Empty states styled with icon, message, and CTA
- [ ] Error states styled with proper visual treatment
- [ ] Drag states have visual feedback (opacity, border)

### Accessibility
- [ ] Focus states visible on interactive elements
- [ ] Color contrast meets WCAG standards
- [ ] ARIA labels on icon-only buttons
- [ ] Reduced motion respected
- [ ] Keyboard navigation works

### Polish
- [ ] No visual glitches
- [ ] Sharp, professional corners (`rounded-md` max, avoid `rounded-xl`)
- [ ] Hover/active states feel responsive
- [ ] Transitions are smooth but quick (`transition-all`, `transition-colors`)

---

## Common Fixes

### Badge Overflow

```svelte
<!-- Before: overflow -->
<div class="grid grid-cols-4">
  <Badge>ORGANIZATION</Badge>
</div>

<!-- After: adequate width -->
<div class="grid grid-cols-[1fr_140px_100px_80px]">
  <Badge class="w-fit">ORGANIZATION</Badge>
</div>
```

### Header Misalignment

```svelte
<!-- Before: different padding -->
<div class="grid grid-cols-4 px-4">Header</div>
<div class="grid grid-cols-4 px-6">Content</div>

<!-- After: identical structure -->
<div class="grid grid-cols-4 gap-4 px-4">Header</div>
<div class="grid grid-cols-4 gap-4 px-4">Content</div>
```

### Missing Focus State

```svelte
<!-- Before -->
<button class="bg-primary">Click</button>

<!-- After -->
<button class="bg-primary focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2">
  Click
</button>
```

### Truncation Hiding Content

```svelte
<!-- Before: hard truncate -->
<span class="truncate w-20">{longText}</span>

<!-- After: adequate width or tooltip -->
<span class="truncate min-w-[120px]" title={longText}>{longText}</span>
```
