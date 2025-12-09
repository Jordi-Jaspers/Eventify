<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/auth';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { LoaderCircle, CircleCheck, CircleX } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
    import {CLIENT_ROUTES} from "$lib/config/routes.ts";
    import {handleError} from "$lib/utils/error-handler.ts";

	let status: 'verifying' | 'success' | 'error' = $state('verifying');
	let errorMessage: string = $state('');

	onMount(async (): Promise<void> => {
		const token: string | null = page.url.searchParams.get('token');
        console.log(token);
		if (!token) {
			status = 'error';
			toast.error('No verification token provided');
			setTimeout((): void => {
				goto(CLIENT_ROUTES.LOGIN_PAGE.path);
			}, 2000);
            return;
		}

		try {
			status = 'success';

            await authStore.verifyEmail(token);
			setTimeout((): void => {
				goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
			}, 2000);
		} catch (error: unknown) {
			status = 'error';
            const {message}: {message: string} = handleError(error, 'We cannot find this verification token. Please request a new one.');

            toast.error(message);
            await goto(CLIENT_ROUTES.LOGIN_PAGE.path);
		}
	});
</script>

<svelte:head>
	<title>Verify Email - Eventify</title>
</svelte:head>

<!-- Content Container (background and centering handled by layout) -->
<div class="max-w-md mx-auto">
	<!-- Glassmorphism Card -->
	<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/20 transition-all duration-300 relative overflow-hidden animate-scale-in">
		<!-- Gradient Glow Effect -->
		<div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>

		<!-- Card Content -->
		<CardHeader class="space-y-1 relative z-10">
			<CardTitle class="text-3xl font-bold text-center gradient-text-animated">
				Email Verification
			</CardTitle>
			<CardDescription class="text-center text-base">
				{#if status === 'verifying'}
					Verifying your email address...
				{:else if status === 'success'}
					Your email has been verified!
				{:else}
					Verification failed
				{/if}
			</CardDescription>
		</CardHeader>

		<CardContent class="flex flex-col items-center space-y-4 relative z-10">
			{#if status === 'verifying'}
				<div class="relative">
					<!-- Animated gradient spinner -->
					<div class="absolute inset-0 rounded-full bg-gradient-to-r from-primary to-accent blur-xl opacity-50 animate-glow-pulse"></div>
					<LoaderCircle class="relative h-16 w-16 animate-spin text-primary" />
				</div>
				<p class="text-muted-foreground text-center animate-fade-in">
					Please wait while we verify your email...
				</p>

			{:else if status === 'success'}
				<!-- Success state with gradient glow -->
				<div class="relative animate-scale-in">
					<div class="absolute inset-0 rounded-full bg-gradient-to-br from-green-500 to-green-600 blur-2xl opacity-50 animate-glow-pulse"></div>
					<div class="relative w-24 h-24 rounded-full bg-gradient-to-br from-green-500 to-green-600 flex items-center justify-center shadow-2xl shadow-green-500/30">
						<CircleCheck class="h-12 w-12 text-white" />
					</div>
				</div>
				<div class="text-center space-y-2 animate-fade-in-up">
					<h3 class="text-xl font-semibold bg-gradient-to-r from-green-400 to-green-600 bg-clip-text text-transparent">
						Success!
					</h3>
					<p class="text-foreground">
						Your email has been successfully verified!
					</p>
					<p class="text-sm text-muted-foreground">
						Redirecting to dashboard...
					</p>
				</div>

			{:else}
				<!-- Error state with gradient glow -->
				<div class="relative animate-scale-in">
					<div class="absolute inset-0 rounded-full bg-gradient-to-br from-destructive to-red-600 blur-2xl opacity-50 animate-glow-pulse"></div>
					<div class="relative w-24 h-24 rounded-full bg-gradient-to-br from-destructive to-red-600 flex items-center justify-center shadow-2xl shadow-destructive/30">
						<CircleX class="h-12 w-12 text-white" />
					</div>
				</div>
				<div class="text-center space-y-2 animate-fade-in-up">
					<h3 class="text-xl font-semibold bg-gradient-to-r from-red-400 to-red-600 bg-clip-text text-transparent">
						Verification Failed
					</h3>
					<p class="text-center text-muted-foreground">{errorMessage || 'Unable to verify your email'}</p>
					<p class="text-sm text-muted-foreground">
						Redirecting to login page...
					</p>
				</div>
			{/if}
		</CardContent>
	</Card>
</div>
