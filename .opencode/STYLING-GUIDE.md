# Eventify Styling Guide

## Framework

- **UI Framework:** SvelteKit (Svelte 5)
- **CSS:** Tailwind CSS v4 (hybrid: CSS `@import "tailwindcss"` + `@theme inline` + JS config)
- **Components:** shadcn-svelte (locally owned in `src/lib/components/ui/`)
- **Icons:** Lucide (@lucide/svelte)
- **Variant System:** tailwind-variants (tv)
- **Class Merging:** `cn()` = clsx + tailwind-merge

## Design Philosophy

Enterprise/security dashboard aesthetic with:
- **Glassmorphism** — backdrop-blur, semi-transparent backgrounds, glowing borders
- **Animated grid background** — scrolling CSS grid pattern
- **Gradient motif** — blue → teal gradients on decorative elements only
- **Dark-first design** — dark mode is the primary/polished design target
- **Progressive disclosure** — show actions on hover, keep interface uncluttered
- **Dashboard density** — not too much whitespace, scannable layouts

### Do's & Don'ts

| ✅ Do | ❌ Don't |
|-------|---------|
| Subtle borders for separation (`border-border/30`) | Heavy drop shadows |
| Glassmorphism on buttons (transparency + blur) | Gradients on buttons or page titles |
| `text-primary` for page titles | Gradient text (except hero/marketing) |
| Progressive disclosure (hover-reveal actions) | Rounded corners everywhere |
| Status colors consistently (green/amber/red) | Decorative elements without purpose |
| Reference `/dev-playbook` for live examples | Custom button/logo markup (use components) |

## Design Tokens

### Colors (CSS Custom Properties)

| Token | Light (oklch) | Dark (hsl) |
|-------|--------------|------------|
| `--primary` | `oklch(0.55 0.22 220)` blue | `hsl(205 85% 50%)` bright blue |
| `--accent` | `oklch(0.62 0.21 295)` purple | `hsl(175 70% 45%)` teal |
| `--background` | near-white `oklch(0.99 0 0)` | deep dark `hsl(220 20% 8%)` |
| `--destructive` | red | `hsl(0 70% 50%)` |
| `--radius` | `0.625rem` | same |

5 chart color tokens for data visualization. Dedicated `--sidebar-*` token set.

### Dark Mode
- Class-based: `darkMode: ['class']`
- CSS: `@custom-variant dark (&:is(.dark *))`
- Toggle: mode-watcher library

### Spacing
- Base unit: 4px (Tailwind default)
- Container: centered, 2rem padding, 1400px max at 2xl
- Cards: `gap-6` internal, `rounded-xl`
- Buttons: h-8/h-9/h-10 (sm/default/lg)

## Component Patterns

### Button Variants (tailwind-variants)
- default, destructive, outline, secondary, ghost, link
- Sizes: default, sm, lg, icon

### Custom Visual Classes
| Class | Effect |
|-------|--------|
| `.glass-card` | backdrop-blur(20px), semi-transparent bg, blue glow on hover |
| `.animated-grid-bg` | Scrolling 40px grid pattern |
| `.gradient-text-animated` | Blue→teal animated gradient text |
| `.gradient-button` | Blue→teal gradient with glow/lift on hover |
| `.stagger-1` to `.stagger-5` | Animation delay increments (0.1s each) |

### Custom Components (non-shadcn)
- `StatCard` — metric display card
- `PulseIndicator` — status dot with pulse animation
- `HttpMethodBadge` — colored HTTP method label
- `StatusIndicator` — health/status display
- `PillToggle` — toggle switch
- `InitialsAvatar` — fallback avatar
- `PageHeader` — consistent page header

## Visual Polish Checklist

- [ ] Consistent spacing (gap-4/gap-6)
- [ ] Loading states (skeleton/spinner)
- [ ] Empty states (icon + message + CTA)
- [ ] Focus states (ring-2 ring-primary)
- [ ] Color contrast (WCAG AA minimum)
- [ ] Dark mode tested
- [ ] Glassmorphism used sparingly (hero/cards only)
- [ ] Icons: Lucide, size-4 default, size-5 for emphasis
- [ ] No horizontal overflow on any viewport
- [ ] Auth pages: logo centered, `max-w-md mx-auto`, consistent card styling
- [ ] Table headers aligned with content (identical grid + padding)
- [ ] Hover/active states feel responsive (`transition-colors`)
- [ ] ARIA labels on icon-only buttons

## Key Patterns

### Page Title
```svelte
<h1 class="text-3xl font-bold text-primary">Title</h1>
<p class="text-muted-foreground mt-2">Description</p>
```

### Auth Page Layout (all auth pages MUST match)
```svelte
<div class="max-w-md mx-auto">
  <div class="mb-8 text-center">
    <AppLogo size="medium" subtitle="..." />
  </div>
  <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">...</Card>
  <p class="text-center text-xs text-muted-foreground mt-6">Footer</p>
</div>
```

### List Item Row (preferred)
```svelte
<div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
  <Icon class="h-4 w-4 text-primary/70 shrink-0" />
  <div class="flex-1 min-w-0">
    <p class="text-sm font-medium truncate">{name}</p>
    <p class="text-xs text-muted-foreground truncate">{desc}</p>
  </div>
  <div class="opacity-0 group-hover:opacity-100 transition-opacity"><!-- actions --></div>
</div>
```

### Standard Card Row (more visual weight)
- Add: `border border-border/50 bg-card/30`, `px-4 py-3`, icon `h-5 w-5`, actions always visible

### Shadcn Editing Rule
- Edit base component → global change needed (missing variant, a11y fix)
- Edit page-level → one-off override (conditional classes, layout-specific)
