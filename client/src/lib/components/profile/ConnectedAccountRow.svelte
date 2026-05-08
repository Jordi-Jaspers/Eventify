<script lang="ts">
    import { Button } from '$lib/components/ui/button';
    import { LoaderCircle, CircleCheck } from '@lucide/svelte';
    import type { ProviderResponse } from '$lib/api/models';
    import type { ConnectedAccountsService } from '$lib/api/user/service/ConnectedAccountsService.svelte';
    import { getProviderMeta, toOAuthProvider } from '$lib/utils/provider-meta';

    interface Props {
        provider: ProviderResponse;
        service: ConnectedAccountsService;
    }

    let { provider, service }: Props = $props();

    const isLocal: boolean = $derived(provider.provider === 'LOCAL');
    const meta = $derived(getProviderMeta(provider.provider));
    const Icon = $derived(meta.icon);

    const isUnlinking: boolean = $derived(
        service.unlinkingId !== null && service.unlinkingId === provider.id
    );
</script>

<div class="flex items-center justify-between gap-4 py-4 border-b border-border/30 last:border-0">
    <!-- Icon + Name + Email -->
    <div class="flex items-center gap-3 min-w-0">
        <div class="flex items-center justify-center w-9 h-9 rounded-full bg-muted/50 shrink-0">
            {#if !isLocal && meta.brandIcon}
                {@html meta.brandIcon}
            {:else}
                <Icon class="w-4 h-4 text-foreground" />
            {/if}
        </div>
        <div class="min-w-0">
            <p class="text-sm font-medium flex items-center gap-1.5">
                {isLocal ? 'Password' : meta.label}
                {#if !isLocal && provider.connected}
                    <CircleCheck class="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                {/if}
            </p>
            {#if !isLocal && provider.connected && provider.providerEmail}
                <p class="text-xs text-muted-foreground truncate">{provider.providerEmail}</p>
            {/if}
        </div>
    </div>

    <!-- Action -->
    <div class="flex items-center gap-3 shrink-0">
        {#if isLocal}
            <Button variant="ghost" size="sm" class="text-xs text-muted-foreground" onclick={() => service.openChangePasswordDialog()}>
                Change
            </Button>
            <Button variant="ghost" size="sm" class="text-xs text-muted-foreground" onclick={() => service.requestPasswordResetForLocal(provider.providerEmail ?? '')}>
                Reset via email
            </Button>
        {:else if provider.connected}
            <Button
                variant="outline"
                size="sm"
                class="border-destructive/50 text-destructive hover:bg-destructive/10 hover:text-destructive"
                disabled={isUnlinking}
                onclick={() => service.openUnlinkDialog(provider)}
                aria-label={`Disconnect ${meta.label}`}
            >
                {#if isUnlinking}
                    <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                {/if}
                Disconnect
            </Button>
        {:else}
            <Button
                variant="default"
                size="sm"
                onclick={() => service.linkProvider(toOAuthProvider(provider.provider))}
                aria-label={`Connect ${meta.label}`}
            >
                Connect
            </Button>
        {/if}
    </div>
</div>
