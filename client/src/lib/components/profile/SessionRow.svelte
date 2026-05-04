<script lang="ts">
    import { Button } from '$lib/components/ui/button';
    import { Badge } from '$lib/components/ui/badge';
    import { LoaderCircle, Monitor, Trash2 } from '@lucide/svelte';
    import type { SessionResponse } from '$lib/api/models';
    // NOTE: formatRelativeTime is computed once per render. The page does not
    // re-render on an interval, so labels can become stale on long-idle pages.
    // This matches usage across the codebase (dashboard, channels) — change
    // globally via a reactive time source if/when needed.
    import { formatRelativeTime, formatDateTime } from '$lib/utils/date';
    import { SESSION_GRID_ROW, SESSION_COL } from './session-grid';

    interface Props {
        session: SessionResponse;
        revoking: boolean;
        onRevoke: (session: SessionResponse) => void;
    }

    let { session, revoking, onRevoke }: Props = $props();

    const deviceLabel: string = $derived(session.deviceInfo ?? 'Unknown device');
</script>

<div
    class="{SESSION_GRID_ROW} px-4 py-3 rounded-lg border transition-colors
        {session.current
            ? 'border-primary/30 bg-primary/5'
            : 'border-border/30 bg-card/20 hover:bg-card/40'}"
>
    <!-- Device -->
    <div class="{SESSION_COL.device} flex items-center gap-2 min-w-0">
        <Monitor class="w-4 h-4 text-muted-foreground shrink-0" />
        <span class="text-sm font-medium truncate">{deviceLabel}</span>
    </div>

    <!-- IP Address -->
    <div class="{SESSION_COL.ip} flex items-center">
        <span class="text-sm text-muted-foreground font-mono">
            {session.ipAddress ?? '—'}
        </span>
    </div>

    <!-- Last Active -->
    <div class="{SESSION_COL.lastActive} flex items-center">
        <span class="text-sm text-muted-foreground">
            {formatRelativeTime(session.lastActiveAt)}
        </span>
    </div>

    <!-- Expires -->
    <div class="{SESSION_COL.expires} flex items-center">
        <span class="text-sm text-muted-foreground">
            {session.expiresAt ? formatDateTime(session.expiresAt) : '—'}
        </span>
    </div>

    <!-- Status -->
    <div class="{SESSION_COL.status} flex items-center">
        {#if session.current}
            <Badge class="bg-primary/20 text-primary border-primary/30 text-xs">
                Current
            </Badge>
        {/if}
    </div>

    <!-- Action -->
    <div class="{SESSION_COL.action} flex items-center justify-end">
        <Button
            variant="ghost"
            size="sm"
            class="h-8 w-8 p-0 text-muted-foreground hover:text-destructive hover:bg-destructive/10"
            disabled={session.current || revoking}
            onclick={() => onRevoke(session)}
            aria-label={session.current
                ? `Current session on ${deviceLabel} cannot be revoked`
                : `Revoke session on ${deviceLabel}`}
        >
            {#if revoking}
                <LoaderCircle class="w-4 h-4 animate-spin" />
            {:else}
                <Trash2 class="w-4 h-4" />
            {/if}
        </Button>
    </div>
</div>
