<script lang="ts">
    import { Monitor } from '@lucide/svelte';
    import { ConfirmDialog } from '$lib/components/ui/confirm-dialog';
    import SessionRow from './SessionRow.svelte';
    import SessionRowSkeleton from './SessionRowSkeleton.svelte';
    import { SESSION_GRID_HEADER, SESSION_COL } from './session-grid';
    import type { SessionService } from '$lib/api/user/service/SessionService.svelte';

    interface Props {
        service: SessionService;
    }

    let { service }: Props = $props();

    const SKELETON_COUNT: number = 3;
</script>

<!-- Revoke Single Session Dialog -->
<ConfirmDialog
    open={service.showRevokeDialog}
    title="Revoke session?"
    confirmLabel="Revoke"
    destructive
    onOpenChange={service.setShowRevokeDialog}
    onConfirm={service.confirmRevokeSession}
>
    {#snippet description()}
        Are you sure you want to revoke the session on
        <span class="font-medium text-foreground">
            {service.sessionToRevoke?.deviceInfo ?? 'Unknown device'}
        </span>?
        That device will be signed out immediately.
    {/snippet}
</ConfirmDialog>

<!-- Revoke All Others Dialog -->
<ConfirmDialog
    open={service.showRevokeAllDialog}
    title="Revoke all other sessions?"
    confirmLabel="Revoke all"
    destructive
    onOpenChange={service.setShowRevokeAllDialog}
    onConfirm={service.confirmRevokeAllOthers}
>
    {#snippet description()}
        This will sign you out on all other devices. Your current session will remain active.
    {/snippet}
</ConfirmDialog>

{#if service.loading}
    <!-- Loading skeleton -->
    <div class="space-y-3">
        {#each Array(SKELETON_COUNT) as _, i (i)}
            <SessionRowSkeleton />
        {/each}
    </div>
{:else if service.sessions.length === 0}
    <!-- Empty state -->
    <div class="flex flex-col items-center justify-center py-12 text-center">
        <Monitor class="w-10 h-10 text-muted-foreground mb-3" />
        <p class="text-sm text-muted-foreground">No active sessions found.</p>
    </div>
{:else}
    <!-- Table header (desktop) -->
    <div class="{SESSION_GRID_HEADER} px-4 py-2 text-xs font-medium text-muted-foreground border-b border-border/50 uppercase tracking-wide">
        <div class={SESSION_COL.device}>Device</div>
        <div class={SESSION_COL.ip}>IP Address</div>
        <div class={SESSION_COL.lastActive}>Last Active</div>
        <div class={SESSION_COL.expires}>Expires</div>
        <div class={SESSION_COL.status}>Status</div>
        <div class="{SESSION_COL.action} text-right">Action</div>
    </div>

    <!-- Session rows -->
    <div class="space-y-1 mt-1">
        {#each service.sessions as session (session.id)}
            <SessionRow
                {session}
                revoking={service.revokingId === session.id}
                onRevoke={service.openRevokeDialog}
            />
        {/each}
    </div>

    <!-- Only-session note -->
    {#if service.sessions.length === 1}
        <p class="text-xs text-muted-foreground text-center mt-4">
            This is your only active session.
        </p>
    {/if}
{/if}
