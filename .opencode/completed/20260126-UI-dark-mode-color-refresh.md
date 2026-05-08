# Dark Mode Color Refresh

**Completed:** 2026-01-26
**Epic:** UI/UX
**Branch:** epic/watchlist

## Summary

Refreshed the dark mode color palette with a professional, security-dashboard aesthetic. Replaced the blue-tinted dark backgrounds with deeper neutral tones, and swapped the purple accent color for a modern teal. Fixed the animated grid background to cover the full viewport regardless of sidebar state.

## Changes

### Dark Mode Color Palette

Updated `client/src/app.css` with new professional dark mode colors:

| Token | Old Value | New Value | Purpose |
|-------|-----------|-----------|---------|
| `--background` | `hsl(222 47% 11%)` | `hsl(220 20% 8%)` | Deep neutral dark |
| `--card` | `hsl(222 47% 11%)` | `hsl(220 18% 10%)` | Slightly elevated |
| `--primary` | `hsl(199 89% 48%)` | `hsl(205 85% 50%)` | Professional blue |
| `--accent` | `hsl(280 65% 60%)` | `hsl(175 70% 45%)` | Teal (was purple) |
| `--muted` | `hsl(223 47% 11%)` | `hsl(220 14% 12%)` | Subtle backgrounds |
| `--muted-foreground` | `hsl(215 20% 65%)` | `hsl(215 15% 55%)` | Better contrast |
| `--border` | `hsl(217 33% 17% / 0.5)` | `hsl(220 15% 18%)` | Visible borders |

### Gradient Updates

- **Orbs:** Changed from blue/purple to blue/teal gradients with reduced opacity (0.15/0.12)
- **Text gradients:** Blue-to-teal instead of blue-to-purple
- **Button gradients:** Professional blue-to-teal

### Grid Background Fix

**Root cause:** The `::before` pseudo-element used `position: absolute` which was constrained by the sidebar layout container width.

**Fix applied:**
- Changed grid pattern from `position: absolute` to `position: fixed`
- Added `::after` pseudo-element for fixed background color layer
- Removed `overflow: hidden` from `.animated-grid-bg`
- Added `min-height: 100vh` for proper sizing

The grid now covers the full viewport (edge-to-edge) and tiles infinitely during window resizing.

### Styling Guide

Updated `~/.config/opencode/projects/eventify/STYLING-GUIDE.md` with:
- Complete dark mode color palette documentation
- Accent color usage guidelines
- Gradient effect patterns
- Semantic color definitions

## Files Modified

| File | Changes |
|------|---------|
| `client/src/app.css` | New dark mode palette, fixed grid positioning |
| `client/src/lib/components/layout/AppBackground.svelte` | Added `w-full` class |
| `~/.config/opencode/projects/eventify/STYLING-GUIDE.md` | Updated color documentation |
| 14 screenshot files | Updated with new styling |

## Design Direction

Security operations center / fintech dashboard aesthetic:
- Dark mode first
- Neutral dark backgrounds (less blue tint)
- Professional blue + teal color scheme (no purple)
- Subtle depth via borders, not heavy shadows
- Clean, scannable, data-dense layouts

## Tests

- ✅ TypeScript check: 0 errors, 0 warnings
- ✅ Login screenshot tests: 12 passing
- ✅ Dashboard screenshot tests: passing
- ✅ Grid covers full viewport in both themes

## Notes

- The grid uses `background-size: 40px 40px` for the tile pattern
- `position: fixed` ensures the grid isn't affected by parent container constraints
- Gradient orbs remain `position: absolute` (intentional - they float within the layout)
