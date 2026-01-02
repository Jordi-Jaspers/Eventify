<script lang="ts">
    import {goto} from '$app/navigation';
    import {page} from '$app/state';
    import {dev} from '$app/environment';
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

    $effect(() => {
        if ($isAuthenticated) {
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
        }
    });

    let email: string = $state('');
    let password: string = $state('');
    let showPassword: boolean = $state(false);
    let isSubmitting: boolean = $state(false);

    const verificationFailed: boolean = $derived(page.url.searchParams.get('verification') === 'failed');
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
        email = 'jordijaspers@gmail.com';
        password = 'admin123!';
    }
</script>

<svelte:head>
    <title>Login - Eventify</title>
</svelte:head>

<!-- Content Container -->
<div class="max-w-md mx-auto">
    <!-- Logo/Branding Section -->
    <div class="mb-8">
        <AppLogo size="medium" subtitle="Real-time monitoring and event tracking"/>
    </div>

    <!-- Glassmorphism Card -->
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
        <!-- Card Content -->
        <CardHeader class="space-y-1">
            <CardTitle class="text-2xl flex items-center gap-2">
                <Shield class="w-5 h-5 text-primary"/>
                Sign In
            </CardTitle>
            <CardDescription>
                Enter your credentials to access the dashboard
            </CardDescription>
        </CardHeader>
        <CardContent>
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
                            class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                    />
                </div>

                <!-- Password Field -->
                <div class="space-y-2">
                    <div class="flex items-center justify-between">
                        <Label for="password">Password</Label>
                        <a
                                href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path}
                                class="text-sm text-primary hover:text-accent transition-colors"
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
                                class="pr-10 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        />
                        <button
                                type="button"
                                onclick={togglePasswordVisibility}
                                disabled={isSubmitting}
                                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50"
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
                        class="w-full bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg hover:shadow-primary/50"
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
                                class="text-primary hover:text-accent transition-colors font-medium"
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
    {#if dev}
        <div class="mt-4 p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 backdrop-blur-sm">
            <div class="flex items-center gap-2 text-amber-500 text-sm font-medium mb-2">
                <Terminal class="w-4 h-4"/>
                Dev Credentials
            </div>
            <div class="text-xs text-muted-foreground space-y-1">
                <p><span class="font-medium">Email:</span> jordijaspers@gmail.com</p>
                <p><span class="font-medium">Password:</span> admin123!</p>
            </div>
            <Button
                    variant="outline"
                    size="sm"
                    class="mt-2 w-full text-xs"
                    onclick={fillDevCredentials}
            >
                Fill Credentials
            </Button>
        </div>
    {/if}

    <!-- Footer -->
    <p class="text-center text-xs text-muted-foreground mt-6">
        By signing in, you agree to our Terms of Service and Privacy Policy
    </p>
</div>
