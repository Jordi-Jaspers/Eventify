<script lang="ts">
    import { onMount } from 'svelte';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import { Button } from '$lib/components/ui/button';
    import { Shield, LoaderCircle } from '@lucide/svelte';
    import { SettingsNav } from '$lib/components/settings';
    import { SessionsTable } from '$lib/components/profile';
    import { createSessionService } from '$lib/api/user/service/SessionService.svelte';
    import { CLIENT_ROUTES } from '$lib/config/routes';

    const service = createSessionService();

    onMount(() => service.load());
</script>

<svelte:head>
    <title>Active Sessions - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_SESSIONS_PAGE.path} />

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <!-- Gradient overlay -->
            <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50 pointer-events-none"></div>

            <CardHeader class="relative z-10">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <CardTitle class="text-2xl flex items-center gap-2">
                            <Shield class="w-6 h-6 text-primary" />
                            Active Sessions
                        </CardTitle>
                        <CardDescription class="mt-1">
                            Manage devices currently signed in to your account.
                        </CardDescription>
                    </div>

                    <!-- Revoke all others button -->
                    {#if service.sessions.length > 1}
                        <Button
                            variant="outline"
                            size="sm"
                            class="shrink-0 border-destructive/50 text-destructive hover:bg-destructive/10 hover:text-destructive"
                            disabled={service.revokingAll}
                            onclick={service.openRevokeAllDialog}
                        >
                            {#if service.revokingAll}
                                <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                            {/if}
                            Revoke all other sessions
                        </Button>
                    {/if}
                </div>
            </CardHeader>

            <CardContent class="relative z-10">
                <SessionsTable {service} />
            </CardContent>
        </Card>
    </div>
</main>
