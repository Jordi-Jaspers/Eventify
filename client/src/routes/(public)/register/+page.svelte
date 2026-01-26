<script lang="ts">
    import {authStore} from '$lib/stores/auth';
    import {goto} from '$app/navigation';
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Input} from '$lib/components/ui/input';
    import {Label} from '$lib/components/ui/label';
    import {Button} from '$lib/components/ui/button';
    import PasswordStrengthMeter from '$lib/components/registration/PasswordStrengthMeter.svelte';
    import OAuthButtons from '$lib/components/auth/OAuthButtons.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import {validateEmail, validateName, validatePassword} from '$lib/utils/password-validator';
    import {toast} from 'svelte-sonner';
    import {Eye, EyeOff, LoaderCircle, UserPlus} from '@lucide/svelte';
    import {CLIENT_ROUTES} from "$lib/config/routes.ts";
    import {handleError} from "$lib/utils/error-handler.ts";

    let firstName: string = $state('');
    let lastName: string = $state('');
    let email: string = $state('');
    let password: string = $state('');
    let passwordConfirmation: string = $state('');
    let loading: boolean = $state(false);
    let showPassword: boolean = $state(false);
    let showPasswordConfirmation: boolean = $state(false);

    // Field-level errors
    let errors: {
        firstName?: string;
        lastName?: string;
        email?: string;
        password?: string;
        passwordConfirmation?: string;
    } = $state({});

    // Validation state
    const passwordValidation = $derived(validatePassword(password));
    const passwordsMatch = $derived(
        passwordConfirmation.length > 0 && password === passwordConfirmation
    );
    const passwordsDontMatch = $derived(
        passwordConfirmation.length > 0 && password !== passwordConfirmation
    );

    function validateForm(): boolean {
        const newErrors: typeof errors = {};

        // First name validation
        if (!firstName.trim()) {
            newErrors.firstName = 'First name is required';
        } else if (!validateName(firstName)) {
            newErrors.firstName = 'First name must be 1-255 characters';
        }

        // Last name validation
        if (!lastName.trim()) {
            newErrors.lastName = 'Last name is required';
        } else if (!validateName(lastName)) {
            newErrors.lastName = 'Last name must be 1-255 characters';
        }

        // Email validation
        if (!email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!validateEmail(email)) {
            newErrors.email = 'Please enter a valid email address';
        }

        // Password validation
        if (!password) {
            newErrors.password = 'Password is required';
        } else if (!passwordValidation.isValid) {
            newErrors.password = 'Password does not meet all requirements';
        }

        // Password confirmation validation
        if (!passwordConfirmation) {
            newErrors.passwordConfirmation = 'Password confirmation is required';
        } else if (password !== passwordConfirmation) {
            newErrors.passwordConfirmation = 'Passwords must match';
        }

        errors = newErrors;
        return Object.keys(newErrors).length === 0;
    }

    async function handleSubmit(event: Event): Promise<void> {
        event.preventDefault();
        if (!validateForm()) {
            toast.error('Please fix the errors in the form');
            return;
        }

        loading = true;

        try {
            await authStore.register({
                firstName,
                lastName,
                email: email.toLowerCase(),
                password,
                passwordConfirmation
            });

            await goto(CLIENT_ROUTES.LOGIN_PAGE.path);
        } catch (error: unknown) {
            const {message} = handleError(error, 'Registration failed. Please try again.');
            toast.error(message);
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Register - Eventify</title>
</svelte:head>

<!-- Content Container -->
<div class="max-w-md mx-auto">
    <!-- Logo/Branding Section -->
    <div class="mb-8">
        <AppLogo size="medium" subtitle="Create your account to get started"/>
    </div>

    <!-- Glassmorphism Card -->
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl ring-1 ring-border/10">
        <!-- Card Content -->
        <CardHeader class="space-y-1">
            <CardTitle class="text-2xl flex items-center gap-2">
                <UserPlus class="w-5 h-5 text-primary"/>
                Sign Up
            </CardTitle>
            <CardDescription>
                Enter your information to create an account
            </CardDescription>
        </CardHeader>
        <CardContent>
            <form onsubmit={handleSubmit} class="space-y-4">
                <!-- First Name & Last Name -->
                <div class="grid grid-cols-2 gap-3">
                    <div class="space-y-2">
                        <Label for="firstName">
                            First Name
                            <span class="text-destructive" aria-label="required">*</span>
                        </Label>
                        <Input
                                id="firstName"
                                type="text"
                                placeholder="John"
                                bind:value={firstName}
                                required
                                aria-invalid={!!errors.firstName}
                                aria-describedby={errors.firstName ? 'firstName-error' : undefined}
                                class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        />
                        {#if errors.firstName}
                            <p id="firstName-error" class="text-sm text-destructive">
                                {errors.firstName}
                            </p>
                        {/if}
                    </div>
                    <div class="space-y-2">
                        <Label for="lastName">
                            Last Name
                            <span class="text-destructive" aria-label="required">*</span>
                        </Label>
                        <Input
                                id="lastName"
                                type="text"
                                placeholder="Doe"
                                bind:value={lastName}
                                required
                                aria-invalid={!!errors.lastName}
                                aria-describedby={errors.lastName ? 'lastName-error' : undefined}
                                class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        />
                        {#if errors.lastName}
                            <p id="lastName-error" class="text-sm text-destructive">
                                {errors.lastName}
                            </p>
                        {/if}
                    </div>
                </div>

                <!-- Email -->
                <div class="space-y-2">
                    <Label for="email">
                        Email
                        <span class="text-destructive" aria-label="required">*</span>
                    </Label>
                    <Input
                            id="email"
                            type="email"
                            placeholder="john.doe@example.com"
                            bind:value={email}
                            required
                            aria-invalid={!!errors.email}
                            aria-describedby={errors.email ? 'email-error' : undefined}
                            class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                    />
                    {#if errors.email}
                        <p id="email-error" class="text-sm text-destructive">
                            {errors.email}
                        </p>
                    {/if}
                </div>

                <!-- Password -->
                <div class="space-y-2">
                    <Label for="password">
                        Password
                        <span class="text-destructive" aria-label="required">*</span>
                    </Label>
                    <div class="relative">
                        <Input
                                id="password"
                                type={showPassword ? 'text' : 'password'}
                                placeholder="Enter a strong password"
                                bind:value={password}
                                required
                                aria-invalid={!!errors.password}
                                aria-describedby={errors.password ? 'password-error' : undefined}
                                class="pr-10 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        />
                        <button
                                type="button"
                                onclick={() => (showPassword = !showPassword)}
                                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary rounded-sm p-0.5"
                                aria-label={showPassword ? 'Hide password' : 'Show password'}
                        >
                            {#if showPassword}
                                <EyeOff class="h-4 w-4"/>
                            {:else}
                                <Eye class="h-4 w-4"/>
                            {/if}
                        </button>
                    </div>
                    {#if errors.password}
                        <p id="password-error" class="text-sm text-destructive">
                            {errors.password}
                        </p>
                    {/if}

                    <!-- Password Strength Meter -->
                    <PasswordStrengthMeter {password}/>
                </div>

                <!-- Password Confirmation -->
                <div class="space-y-2">
                    <Label for="passwordConfirmation">
                        Confirm Password
                        <span class="text-destructive" aria-label="required">*</span>
                    </Label>
                    <div class="relative">
                        <Input
                                id="passwordConfirmation"
                                type={showPasswordConfirmation ? 'text' : 'password'}
                                placeholder="Re-enter your password"
                                bind:value={passwordConfirmation}
                                required
                                aria-invalid={!!errors.passwordConfirmation}
                                aria-describedby={errors.passwordConfirmation
								? 'passwordConfirmation-error'
								: undefined}
                                class="pr-10 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        />
                        <button
                                type="button"
                                onclick={() => (showPasswordConfirmation = !showPasswordConfirmation)}
                                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary rounded-sm p-0.5"
                                aria-label={showPasswordConfirmation ? 'Hide password' : 'Show password'}
                        >
                            {#if showPasswordConfirmation}
                                <EyeOff class="h-4 w-4"/>
                            {:else}
                                <Eye class="h-4 w-4"/>
                            {/if}
                        </button>
                    </div>
                    {#if errors.passwordConfirmation}
                        <p id="passwordConfirmation-error" class="text-sm text-destructive">
                            {errors.passwordConfirmation}
                        </p>
                    {:else if passwordsMatch}
                        <p class="text-sm text-green-400 flex items-center gap-1">
                            <span class="inline-block w-1.5 h-1.5 rounded-full bg-green-400"></span>
                            Passwords match
                        </p>
                    {:else if passwordsDontMatch}
                        <p class="text-sm text-destructive">Passwords must match</p>
                    {/if}
                </div>

                <!-- Submit Button -->
                <Button
                        type="submit"
                        class="w-full mt-6"
                        disabled={loading}
                >
                    {#if loading}
                        <LoaderCircle class="mr-2 h-4 w-4 animate-spin"/>
                        Creating account...
                    {:else}
                        Create Account
                    {/if}
                </Button>

                <!-- OAuth Buttons Component -->
                <OAuthButtons disabled={loading}/>

                <!-- Sign in link -->
                <div class="mt-6 text-center">
                    <p class="text-sm text-muted-foreground">
                        Already have an account?{' '}
                        <a
                                href={CLIENT_ROUTES.LOGIN_PAGE.path}
                                class="text-primary hover:text-accent transition-colors font-medium"
                        >
                            Sign in
                        </a>
                    </p>
                </div>
            </form>
        </CardContent>
    </Card>

    <!-- Footer -->
    <p class="text-center text-xs text-muted-foreground mt-6">
        By signing up, you agree to our Terms of Service and Privacy Policy
    </p>
</div>
