<script lang="ts">
    import { authStore } from '$lib/stores/auth';
    import { goto } from '$app/navigation';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import { UserPlus } from '@lucide/svelte';
    import { RegistrationForm } from '$lib/components/auth';
    import { CLIENT_ROUTES } from '$lib/config/routes';
    import { handleError } from '$lib/utils/error-handler';
    import { toast } from 'svelte-sonner';

    let loading: boolean = $state(false);

    async function handleSubmit(data: {
        firstName: string;
        lastName: string;
        email: string;
        password: string;
        passwordConfirmation: string;
    }): Promise<void> {
        loading = true;
        try {
            await authStore.register(data);
            await goto(CLIENT_ROUTES.LOGIN_PAGE.path);
        } catch (error: unknown) {
            const { message } = handleError(error, 'Registration failed. Please try again.');
            toast.error(message);
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Register - Eventify</title>
</svelte:head>

<div class="max-w-md mx-auto">
    <div class="mb-8 text-center">
        <AppLogo size="medium" subtitle="Create your account to get started" />
    </div>

    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg ring-1 ring-border/10">
        <CardHeader class="space-y-1">
            <CardTitle class="text-2xl flex items-center gap-2">
                <UserPlus class="w-5 h-5 text-primary" />
                Sign Up
            </CardTitle>
            <CardDescription>
                Enter your information to create an account
            </CardDescription>
        </CardHeader>
        <CardContent>
            <RegistrationForm {loading} onSubmit={handleSubmit} />
        </CardContent>
    </Card>

    <p class="text-center text-xs text-muted-foreground mt-6">
        By signing up, you agree to our Terms of Service and Privacy Policy
    </p>
</div>
