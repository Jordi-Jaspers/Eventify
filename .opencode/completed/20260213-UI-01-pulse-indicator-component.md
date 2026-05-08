# PulseIndicator Component

**Completed:** 2026-02-13
**Epic:** UI Components
**Type:** Refactor / Component Extraction

## Summary

Extracted a reusable PulseIndicator component to standardize animated status dots across the application. Replaced the glitchy `animate-ping` pattern (nested absolute positioning) with a simpler, smoother `animate-pulse` animation.

## Problem

The old pattern for animated status indicators used nested absolute positioning with `animate-ping`:

```html
<span class="relative flex h-2 w-2">
  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
  <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
</span>
```

This caused visual glitches where the ping animation would sometimes appear misaligned or jittery.

## Solution

Created a simple, single-element approach using `animate-pulse`:

```html
<span class="h-2 w-2 rounded-full bg-green-500 animate-pulse"></span>
```

The `animate-pulse` animation smoothly fades opacity (1 → 0.5 → 1), providing a subtle, professional "breathing" effect without the complexity of nested absolute positioning.

## Component API

```svelte
<PulseIndicator 
  variant="green" | "primary" | "red" | "yellow" | "blue"
  size="xs" | "sm" | "md" | "lg"
  label="Live"  <!-- optional, adds text next to dot -->
  class="ml-auto"  <!-- optional custom classes -->
/>
```

### Variants
- `green` - Success/live status (default)
- `primary` - Brand color (teal)
- `red` - Error/critical
- `yellow` - Warning
- `blue` - Info/neutral

### Sizes
- `xs` - 1.5 (6px)
- `sm` - 2 (8px, default)
- `md` - 2.5 (10px)
- `lg` - 3 (12px)

## Files Changed

### New Files
- `client/src/lib/components/ui/pulse-indicator/PulseIndicator.svelte` - Component implementation
- `client/src/lib/components/ui/pulse-indicator/constants.ts` - Configuration constants & types
- `client/src/lib/components/ui/pulse-indicator/index.ts` - Barrel export with type re-exports

### Modified Files
- `client/src/lib/components/monitor/ConfigurePopover.svelte` - Live indicator in popover
- `client/src/lib/components/monitor/TimeAxisHeader.svelte` - Live indicator in monitor header
- `client/src/routes/(authenticated)/dashboard/+page.svelte` - Account status indicator
- `client/src/routes/(public)/+page.svelte` - Landing page "Connected" indicator
- `client/src/routes/(public)/dev-playbook/+page.svelte` - Updated documentation with new component
- `client/src/lib/data/changelog.ts` - Added to What's New

## Implementation Details

The component uses an optimized structure with extracted constants:

### constants.ts
```typescript
export type PulseIndicatorSize = 'xs' | 'sm' | 'md' | 'lg';
export type PulseIndicatorVariant = 'green' | 'primary' | 'red' | 'yellow' | 'blue';

export const SIZE_CLASSES: Record<PulseIndicatorSize, string> = {
  xs: 'h-1.5 w-1.5',
  sm: 'h-2 w-2',
  md: 'h-2.5 w-2.5',
  lg: 'h-3 w-3'
} as const;

export const VARIANT_CLASSES: Record<PulseIndicatorVariant, string> = {
  green: 'bg-green-500',
  primary: 'bg-primary',
  red: 'bg-red-500',
  yellow: 'bg-yellow-500',
  blue: 'bg-blue-500'
} as const;

export const LABEL_COLOR_CLASSES: Record<PulseIndicatorVariant, string> = {
  green: 'text-green-500',
  primary: 'text-primary',
  red: 'text-red-500',
  yellow: 'text-yellow-500',
  blue: 'text-blue-500'
} as const;
```

### PulseIndicator.svelte
```svelte
<script lang="ts">
  import { cn } from '$lib/utils';
  import {
    SIZE_CLASSES,
    VARIANT_CLASSES,
    LABEL_COLOR_CLASSES,
    type PulseIndicatorSize,
    type PulseIndicatorVariant
  } from './constants';

  interface Props {
    variant?: PulseIndicatorVariant;
    size?: PulseIndicatorSize;
    label?: string;
    class?: string;
  }

  const { variant = 'green', size = 'sm', label, class: className }: Props = $props();

  const indicatorClasses: string = $derived(
    cn('rounded-full animate-pulse', SIZE_CLASSES[size], VARIANT_CLASSES[variant])
  );

  const wrapperClasses: string = $derived(
    cn('flex items-center gap-1.5', label && LABEL_COLOR_CLASSES[variant], className)
  );
</script>

{#if label}
  <span class={wrapperClasses}>
    <span class={indicatorClasses}></span>
    <span class="text-xs font-medium">{label}</span>
  </span>
{:else}
  <span class={cn(indicatorClasses, className)}></span>
{/if}
```

## Usage Examples

```svelte
<!-- Simple live indicator -->
<PulseIndicator variant="green" label="Live" />

<!-- Status in a header -->
<PulseIndicator variant="primary" size="md" class="ml-auto" />

<!-- Error state -->
<PulseIndicator variant="red" label="Disconnected" />
```

## Dev Playbook

The component is documented in the Dev Playbook under "Status Indicators" with examples of all variants and sizes.

## Optimization Pass

After initial implementation, the frontend optimizer improved the component:

| Improvement | Description |
|-------------|-------------|
| **PascalCase naming** | Renamed to `PulseIndicator.svelte` (matches project convention) |
| **Constants extraction** | Separated configuration into `constants.ts` with JSDoc |
| **Type exports** | Consumers can import `PulseIndicatorSize` and `PulseIndicatorVariant` |
| **$derived reactive state** | Uses Svelte 5 `$derived()` for reactive class computation |
| **Label color matching** | Labels now match their variant color (green label for green dot, etc.) |

## Notes

- The `animate-pulse` animation is built into Tailwind CSS (keyframes: `pulse { 0%, 100% { opacity: 1 } 50% { opacity: .5 } }`)
- No JavaScript animation logic needed - pure CSS
- PascalCase component file with kebab-case folder (project convention)
- Types exported for consumer type-checking
