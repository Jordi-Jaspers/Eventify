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
    import {CircleAlert, Info, LoaderCircle, Shield} from '@lucide/svelte';
    import { PasswordInput } from '$lib/components/ui/password-input';
    import {handleError} from '$lib/utils/error-handler';
    import {getDevCredentials} from '$lib/api/dev/DevController';
    import type {DevCredentialsResponse} from '$lib/api/models';
    import {showDevCredentials} from '$lib/config/env';
    import {Checkbox} from '$lib/components/ui/checkbox';
    import DevCredentialsPanel from '$lib/components/auth/DevCredentialsPanel.svelte';

    $effect(() => {
        if ($isAuthenticated) {
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
        }
    });

    let email: string = $state('');
    let password: string = $state('');
    let isSubmitting: boolean = $state(false);
    let rememberMe: boolean = $state(false);

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
            await authStore.login(email.trim(), password, rememberMe);
        } catch (error: unknown) {
            const {message} = handleError(error, 'Invalid email or password. Please try again.');
            toast.error(message);
        } finally {
            isSubmitting = false;
        }
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
                    <div class="space-y-2">
                        <PasswordInput
                                id="password"
                                placeholder="Enter your password"
                                bind:value={password}
                                disabled={isSubmitting}
                        />
                    </div>
                </div>

                <!-- Remember Me -->
                <div class="flex items-center space-x-3">
                    <Checkbox
                        id="remember-me"
                        checked={rememberMe}
                        onCheckedChange={(v: boolean) => (rememberMe = v)}
                        disabled={isSubmitting}
                    />
                    <Label for="remember-me" class="text-sm font-normal cursor-pointer">
                        Remember me for 30 days
                    </Label>
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
        <DevCredentialsPanel
            {devCredentials}
            {devCredentialsLoading}
            onFill={fillDevCredentials}
        />
    {/if}

    <!-- Footer -->
    <p class="text-center text-xs text-muted-foreground mt-6">
        By signing in, you agree to our Terms of Service and Privacy Policy
    </p>
</div>
