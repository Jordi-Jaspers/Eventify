# Fix: Configure Button Hover Contrast in Watchlist

**Epic:** USER_CHANGE_REQUEST
**Date:** 2026-05-19
**Commit:** fix: improve configure button hover contrast in watchlist monitor

## Problem

The Configure button in the watchlist monitor page had poor contrast on hover due to low-opacity background (`bg-background/30`) and no explicit text color, making it hard to read against dark backgrounds.

## Solution

In `client/src/lib/components/monitor/ConfigurePopover.svelte`:
- Added `text-foreground` to ensure text is always visible
- Changed hover background from `hover:bg-background/50` → `hover:bg-background/80` for better contrast

## Files Changed

| File | Change |
|------|--------|
| `client/src/lib/components/monitor/ConfigurePopover.svelte` | Fixed button contrast classes |
