<script lang="ts">
    import {goto} from '$app/navigation';
    import {page} from '$app/state';
    import {authStore, isAuthenticated, isUnverified} from '$lib/stores/auth';
    import {CLIENT_ROUTES} from '$lib/config/routes';
    import Button from '$lib/components/ui/button/button.svelte';
    import Input from '$lib/components/ui/input/input.svelte';
    import Label from '$lib/components/ui/label/label.svelte';
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Alert, AlertDescription} from '$lib/components/ui/alert';
    import ResendVerificationButton from '$lib/components/registration/ResendVerificationButton.svelte';
    import OAuthButtons from '$lib/components/auth/OAuthButtons.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import {toast} from 'svelte-sonner';
    import {CircleAlert, Eye, EyeOff, Info, LoaderCircle, Shield, Terminal} from '@lucide/svelte';
    import {handleError} from '$lib/utils/error-handler';
    import {getDevCredentials} from '$lib/api/dev/DevController';
    import type {DevCredentialsResponse} from '$lib/api/models';
    import {showDevCredentials} from '$lib/config/env';

    $effect(() => {
        if ($isAuthenticated) {
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
        }
    });

    let email: string = $state('');
    let password: string = $state('');
    let showPassword: boolean = $state(false);
    let isSubmitting: boolean = $state(false);

    // Dev credentials fetched from API (dev mode only)
    let devCredentials: DevCredentialsResponse | null = $state(null);
    let devCredentialsLoading: boolean = $state(false);

    const shouldShowDevCredentials: boolean = showDevCredentials();

    $effect(() => {
        if (shouldShowDevCredentials && !devCredentials && !devCredentialsLoading) {
            devCredentialsLoading = true;
            getDevCredentials()
                .then((data: DevCredentialsResponse) => {
                    devCredentials = data;
                })
                .catch((err: unknown) => {
                    console.error('Failed to fetch dev credentials:', err);
                })
                .finally(() => {
                    devCredentialsLoading = false;
                });
        }
    });

    const verificationFailed: boolean = $derived(page.url.searchParams.get('verification') === 'failed');
    const sessionExpired: boolean = $derived(page.url.searchParams.get('expired') === 'true');
    async function handleSubmit(event: SubmitEvent): Promise<void> {
        event.preventDefault();
        if (!email.trim() || !password) {
            toast.error('Please enter your email and password');
            return;
        }

        isSubmitting = true;

        try {
            await authStore.login(email.trim(), password);
        } catch (error: unknown) {
            const {message} = handleError(error, 'Invalid email or password. Please try again.');
            toast.error(message);
        } finally {
            isSubmitting = false;
        }
    }

    function togglePasswordVisibility(): void {
        showPassword = !showPassword;
    }

    function fillDevCredentials(): void {
        if (devCredentials) {
            email = devCredentials.email ?? '';
            password = devCredentials.password ?? '';
        }
    }
</script>

<svelte:head>
    <title>Login - Eventify</title>
</svelte:head>

<!-- Content Container -->
<div class="max-w-md mx-auto">
    <!-- Logo/Branding Section -->
    <div class="mb-8 text-center">
        <AppLogo size="medium" subtitle="Real-time monitoring and event tracking"/>
    </div>

    <!-- Glassmorphism Card -->
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg dark:shadow-primary/5">
        <!-- Card Content -->
        <CardHeader class="space-y-1">
            <CardTitle class="text-2xl font-bold flex items-center gap-2 text-primary">
                <Shield class="w-5 h-5"/>
                Sign In
            </CardTitle>
            <CardDescription>
                Enter your credentials to access the dashboard
            </CardDescription>
        </CardHeader>
        <CardContent>
            {#if sessionExpired}
                <Alert class="mb-4 bg-primary/5 border-primary/30 backdrop-blur-sm">
                    <Info class="h-4 w-4 text-primary"/>
                    <AlertDescription class="text-foreground">
                        Your session has expired. Please log in again.
                    </AlertDescription>
                </Alert>
            {/if}

            {#if verificationFailed}
                <Alert variant="destructive" class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm">
                    <CircleAlert class="h-4 w-4"/>
                    <AlertDescription>
                        Verification failed. The link may have expired. Please request a new verification email.
                    </AlertDescription>
                </Alert>
            {/if}

            <!-- Unverified Email Alert -->
            {#if $isUnverified}
                <Alert class="mb-4 bg-primary/5 border-primary/30 backdrop-blur-sm">
                    <AlertDescription class="flex flex-col items-center gap-3 text-center">
                        <div class="flex items-center gap-2">
                            <Info class="h-4 w-4 text-primary"/>
                            <p class="text-foreground">Your email address has not been verified yet.</p>
                        </div>

                        <ResendVerificationButton variant="default" size="sm"/>
                    </AlertDescription>
                </Alert>
            {/if}

            <form onsubmit={handleSubmit} class="space-y-4">
                <!-- Email Field -->
                <div class="space-y-2">
                    <Label for="email">Email</Label>
                    <Input
                            id="email"
                            type="email"
                            placeholder="you@example.com"
                            bind:value={email}
                            disabled={isSubmitting}
                            class="bg-background/50"
                    />
                </div>

                <!-- Password Field -->
                <div class="space-y-2">
                    <div class="flex items-center justify-between">
                        <Label for="password">Password</Label>
                        <a
                                href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path}
                                class="text-sm text-primary hover:underline transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm"
                                tabindex={isSubmitting ? -1 : 0}
                        >
                            Forgot password?
                        </a>
                    </div>
                    <div class="relative">
                        <Input
                                id="password"
                                type={showPassword ? 'text' : 'password'}
                                placeholder="Enter your password"
                                bind:value={password}
                                disabled={isSubmitting}
                                class="pr-10 bg-background/50"
                        />
                        <button
                                type="button"
                                onclick={togglePasswordVisibility}
                                disabled={isSubmitting}
                                class="absolute right-0 top-0 h-full px-3 py-2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm"
                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                                tabindex="0"
                        >
                            {#if showPassword}
                                <EyeOff class="h-4 w-4"/>
                            {:else}
                                <Eye class="h-4 w-4"/>
                            {/if}
                        </button>
                    </div>
                </div>

                <!-- Submit Button -->
                <Button
                        type="submit"
                        class="w-full"
                        disabled={isSubmitting}
                >
                    {#if isSubmitting}
                        <LoaderCircle class="mr-2 h-4 w-4 animate-spin"/>
                        Logging in...
                    {:else}
                        Sign In
                    {/if}
                </Button>

                <!-- OAuth Buttons Component -->
                <OAuthButtons disabled={isSubmitting}/>

                <!-- Sign up link -->
                <div class="mt-6 text-center">
                    <p class="text-sm text-muted-foreground">
                        Don't have an account?{' '}
                        <a
                                href={CLIENT_ROUTES.REGISTER_PAGE.path}
                                class="text-primary hover:underline transition-all font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm"
                                tabindex={isSubmitting ? -1 : 0}
                        >
                            Sign up
                        </a>
                    </p>
                </div>
            </form>
        </CardContent>
    </Card>

    <!-- Dev Credentials Block -->
    {#if shouldShowDevCredentials}
        <div class="mt-4 p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 backdrop-blur-sm">
            <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2 text-amber-500 text-sm font-medium">
                    <Terminal class="w-4 h-4"/>
                    Dev Credentials
                </div>
                <a 
                    href="/dev-playbook" 
                    class="text-xs text-primary hover:underline"
                >
                    Component Playbook →
                </a>
            </div>
            {#if devCredentialsLoading}
                <div class="text-xs text-muted-foreground flex items-center gap-2">
                    <LoaderCircle class="w-3 h-3 animate-spin"/>
                    Loading credentials...
                </div>
            {:else if devCredentials}
                <div class="text-xs text-muted-foreground space-y-1">
                    <p><span class="font-medium">Email:</span> {devCredentials.email}</p>
                    <p><span class="font-medium">Password:</span> {devCredentials.password}</p>
                </div>
                <Button
                        variant="outline"
                        size="sm"
                        class="mt-2 w-full text-xs"
                        onclick={fillDevCredentials}
                >
                    Fill Credentials
                </Button>
            {:else}
                <div class="text-xs text-muted-foreground">
                    Failed to load dev credentials
                </div>
            {/if}
        </div>
    {/if}

    <!-- Footer -->
    <p class="text-center text-xs text-muted-foreground mt-6">
        By signing in, you agree to our Terms of Service and Privacy Policy
    </p>
</div>
