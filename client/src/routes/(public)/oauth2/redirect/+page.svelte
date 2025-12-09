<script lang="ts">
    import {onMount} from 'svelte';
    import {goto} from '$app/navigation';
    import {authStore} from '$lib/stores/auth';
    import {CLIENT_ROUTES} from '$lib/config/routes';
    import {toast} from 'svelte-sonner';
    import {Card, CardContent} from '$lib/components/ui/card';
    import {CircleCheck, LoaderCircle, CircleX} from '@lucide/svelte';
    import {page} from "$app/state";

    let status: 'loading' | 'success' | 'error' = $state('loading');
    let errorMessage: string = $state('');

    const redirect = (path: string, delay = 3000) =>
        setTimeout(() => goto(path), delay);

    const fail = (msg: string, delay = 3000) => {
        status = 'error';
        errorMessage = msg;
        redirect(CLIENT_ROUTES.LOGIN_PAGE.path, delay);
    };

    onMount(async () => {
        const oauthError = page.url.searchParams.get('error');
        if (oauthError) {
            fail(`Authentication failed: ${decodeURIComponent(oauthError)}`, 5000);

            const cleaned = new URL(window.location.href);
            cleaned.searchParams.delete('error');
            history.replaceState({}, '', cleaned.toString());
            return;
        }

        authStore.initializeFromToken();
        await new Promise(r => setTimeout(r, 500));

        const user = $authStore.user;
        if (!user) return fail('Session could not be established. Please try again.');

        if (!user.validated)
            return fail('Email not verified. Please verify your email to continue.');

        status = 'success';
        setTimeout(() => {
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
            toast.success(`Welcome back, ${user.firstName}!`);
        }, 1000);
    });
</script>

<svelte:head>
    <title>Authenticating - Eventify</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center p-4">
    <Card class="w-full max-w-md border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
        <CardContent class="pt-6">
            {#if status === 'loading'}
                <!-- Loading State -->
                <div class="text-center py-8">
                    <LoaderCircle class="w-12 h-12 mx-auto mb-4 text-primary animate-spin"/>
                    <h2 class="text-xl font-semibold mb-2">Completing authentication...</h2>
                    <p class="text-sm text-muted-foreground">
                        Please wait while we set up your session
                    </p>
                </div>
            {:else if status === 'success'}
                <!-- Success State -->
                <div class="text-center py-8">
                    <CircleCheck class="w-12 h-12 mx-auto mb-4 text-green-500"/>
                    <h2 class="text-xl font-semibold mb-2">Authentication successful!</h2>
                    <p class="text-sm text-muted-foreground">Redirecting to dashboard...</p>
                </div>
            {:else if status === 'error'}
                <!-- Error State -->
                <div class="text-center py-8">
                    <CircleX class="w-12 h-12 mx-auto mb-4 text-destructive"/>
                    <h2 class="text-xl font-semibold mb-2">Authentication failed</h2>
                    <p class="text-muted-foreground mb-4">{errorMessage}</p>
                    <p class="text-xs text-muted-foreground">Redirecting to login page...</p>
                </div>
            {/if}
        </CardContent>
    </Card>
</div>
