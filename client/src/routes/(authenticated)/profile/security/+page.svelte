<script lang="ts">
    import { onMount } from 'svelte';
    import { page } from '$app/stores';
    import { goto } from '$app/navigation';
    import { toast } from 'svelte-sonner';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import { Button } from '$lib/components/ui/button';
    import { Shield, Link2, LoaderCircle } from '@lucide/svelte';
    import { SettingsNav } from '$lib/components/settings';
    import { ConnectedAccountRow, SessionsTable } from '$lib/components/profile';
    import { createConnectedAccountsService } from '$lib/api/user/service/ConnectedAccountsService.svelte';
    import { createSessionService } from '$lib/api/user/service/SessionService.svelte';
    import { CLIENT_ROUTES } from '$lib/config/routes';
    import * as AlertDialog from '$lib/components/ui/alert-dialog';
    import { getProviderMeta } from '$lib/utils/provider-meta';

    const LINK_ERROR_MESSAGES: Record<string, string> = {
        email_in_use: 'The email address from this provider is already associated with another account.',
        provider_linked_elsewhere: 'This provider account is already linked to a different user.',
        already_linked: 'This provider is already connected to your account.'
    };

    const accountsService = createConnectedAccountsService();
    const sessionService = createSessionService();

    onMount(() => {
        const error: string | null = $page.url.searchParams.get('error');
        if (error) {
            const message: string = LINK_ERROR_MESSAGES[error] ?? 'Failed to link provider. Please try again.';
            toast.error(message);
            goto($page.url.pathname, { replaceState: true });
        }
        accountsService.load();
        sessionService.load();
    });
</script>

<svelte:head>
    <title>Security - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_SECURITY_PAGE.path} />

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto space-y-6 animate-fade-in">

        <!-- Sign-in methods -->
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50 pointer-events-none"></div>

            <CardHeader class="relative z-10">
                <CardTitle class="text-2xl flex items-center gap-2">
                    <Link2 class="w-6 h-6 text-primary" />
                    Sign-in methods
                </CardTitle>
                <CardDescription class="mt-1">
                    Link or unlink third-party providers used to sign in to your account.
                </CardDescription>
            </CardHeader>

            <CardContent class="relative z-10">
                {#if accountsService.loading}
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
                        {#each accountsService.providers as provider (provider.provider)}
                            <ConnectedAccountRow {provider} service={accountsService} />
                        {/each}
                    </div>
                {/if}
            </CardContent>
        </Card>

        <!-- Active sessions -->
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50 pointer-events-none"></div>

            <CardHeader class="relative z-10">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <CardTitle class="text-2xl flex items-center gap-2">
                            <Shield class="w-6 h-6 text-primary" />
                            Active sessions
                        </CardTitle>
                        <CardDescription class="mt-1">
                            Manage devices currently signed in to your account.
                        </CardDescription>
                    </div>

                    {#if sessionService.sessions.length > 1}
                        <Button
                            variant="outline"
                            size="sm"
                            class="shrink-0 border-destructive/50 text-destructive hover:bg-destructive/10 hover:text-destructive"
                            disabled={sessionService.revokingAll}
                            onclick={sessionService.openRevokeAllDialog}
                        >
                            {#if sessionService.revokingAll}
                                <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                            {/if}
                            Revoke all other sessions
                        </Button>
                    {/if}
                </div>
            </CardHeader>

            <CardContent class="relative z-10">
                <SessionsTable service={sessionService} />
            </CardContent>
        </Card>

    </div>
</main>

<!-- Unlink Confirm Dialog -->
<AlertDialog.Root
    open={accountsService.showUnlinkDialog}
    onOpenChange={(open: boolean) => accountsService.setShowUnlinkDialog(open)}
>
    <AlertDialog.Content class="bg-background border-border/50">
        <AlertDialog.Header>
            <AlertDialog.Title>Disconnect provider?</AlertDialog.Title>
            <AlertDialog.Description>
                {#if accountsService.providerToUnlink}
                    Are you sure you want to disconnect {getProviderMeta(
                        accountsService.providerToUnlink.provider
                    ).label}? You will no longer be able to sign in with this provider.
                {/if}
            </AlertDialog.Description>
        </AlertDialog.Header>
        <AlertDialog.Footer>
            <AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
            <AlertDialog.Action
                onclick={accountsService.confirmUnlink}
                class="bg-destructive hover:bg-destructive/90"
            >
                Disconnect
            </AlertDialog.Action>
        </AlertDialog.Footer>
    </AlertDialog.Content>
</AlertDialog.Root>
