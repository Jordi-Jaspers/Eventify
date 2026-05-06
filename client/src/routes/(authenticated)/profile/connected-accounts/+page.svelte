<script lang="ts">
    import { onMount } from 'svelte';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import { Link2 } from '@lucide/svelte';
    import { SettingsNav } from '$lib/components/settings';
    import { ConnectedAccountRow } from '$lib/components/profile';
    import { createConnectedAccountsService } from '$lib/api/user/service/ConnectedAccountsService.svelte';
    import { CLIENT_ROUTES } from '$lib/config/routes';
    import * as AlertDialog from '$lib/components/ui/alert-dialog';
    import { getProviderMeta } from '$lib/utils/provider-meta';

    const service = createConnectedAccountsService();

    onMount(() => service.load());
</script>

<svelte:head>
    <title>Connected Accounts - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_CONNECTED_ACCOUNTS_PAGE.path} />

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <!-- Gradient overlay -->
            <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50 pointer-events-none"></div>

            <CardHeader class="relative z-10">
                <CardTitle class="text-2xl flex items-center gap-2">
                    <Link2 class="w-6 h-6 text-primary" />
                    Connected Accounts
                </CardTitle>
                <CardDescription class="mt-1">
                    Link or unlink third-party providers used to sign in to your account.
                </CardDescription>
            </CardHeader>

            <CardContent class="relative z-10">
                {#if service.loading}
                    <div class="space-y-4 py-2">
                        {#each [1, 2, 3] as _}
                            <div class="flex items-center justify-between gap-4 py-4 border-b border-border/30 last:border-0">
                                <div class="flex items-center gap-3">
                                    <div class="w-9 h-9 rounded-full bg-muted/50 animate-pulse"></div>
                                    <div class="space-y-1">
                                        <div class="h-4 w-24 bg-muted/50 rounded animate-pulse"></div>
                                        <div class="h-3 w-32 bg-muted/30 rounded animate-pulse"></div>
                                    </div>
                                </div>
                                <div class="h-8 w-20 bg-muted/50 rounded animate-pulse"></div>
                            </div>
                        {/each}
                    </div>
                {:else}
                    <div>
                        {#each service.providers as provider (provider.provider)}
                            <ConnectedAccountRow {provider} {service} />
                        {/each}
                    </div>
                {/if}
            </CardContent>
        </Card>
    </div>
</main>

<!-- Unlink Confirm Dialog -->
<AlertDialog.Root
    open={service.showUnlinkDialog}
    onOpenChange={(open) => service.setShowUnlinkDialog(open)}
>
    <AlertDialog.Content class="bg-background border-border/50">
        <AlertDialog.Header>
            <AlertDialog.Title>Disconnect provider?</AlertDialog.Title>
            <AlertDialog.Description>
                {#if service.providerToUnlink}
                    Are you sure you want to disconnect {getProviderMeta(
                        service.providerToUnlink.provider
                    ).label}? You will no longer be able to sign in with this provider.
                {/if}
            </AlertDialog.Description>
        </AlertDialog.Header>
        <AlertDialog.Footer>
            <AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
            <AlertDialog.Action
                onclick={service.confirmUnlink}
                class="bg-destructive hover:bg-destructive/90"
            >
                Disconnect
            </AlertDialog.Action>
        </AlertDialog.Footer>
    </AlertDialog.Content>
</AlertDialog.Root>
