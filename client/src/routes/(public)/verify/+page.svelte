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
    import AppLogo from '$lib/components/layout/AppLogo.svelte';

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
	<!-- Logo/Branding Section -->
	<div class="mb-8 text-center">
		<AppLogo size="medium" subtitle="Confirming your email address"/>
	</div>

	<!-- Glassmorphism Card -->
	<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg dark:shadow-primary/5 relative overflow-hidden animate-scale-in">
		<!-- Card Content -->
		<CardHeader class="space-y-2 relative z-10">
			<CardTitle class="text-2xl font-bold text-center text-primary">
				Email Verification
			</CardTitle>
		</CardHeader>

		<CardContent class="flex flex-col items-center space-y-6 relative z-10 pb-8">
			{#if status === 'verifying'}
				<div class="relative animate-scale-in">
					<div class="relative w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center border border-primary/20">
						<LoaderCircle class="h-12 w-12 animate-spin text-primary" />
					</div>
				</div>
				<div class="text-center space-y-3 animate-fade-in-up">
					<h3 class="text-2xl font-bold text-primary">
						Verifying...
					</h3>
					<p class="text-muted-foreground text-base">
						Please wait while we verify your email...
					</p>
				</div>

			{:else if status === 'success'}
				<!-- Success state -->
				<div class="relative animate-scale-in">
					<div class="relative w-24 h-24 rounded-full bg-green-500/10 flex items-center justify-center border border-green-500/20">
						<CircleCheck class="h-12 w-12 text-green-500" />
					</div>
				</div>
				<div class="text-center space-y-3 animate-fade-in-up">
					<h3 class="text-2xl font-bold text-green-500">
						Success!
					</h3>
					<p class="text-foreground text-base">
						Your email has been successfully verified!
					</p>
					<p class="text-sm text-muted-foreground">
						Redirecting to dashboard...
					</p>
				</div>

			{:else}
				<!-- Error state -->
				<div class="relative animate-scale-in">
					<div class="relative w-24 h-24 rounded-full bg-destructive/10 flex items-center justify-center border border-destructive/20">
						<CircleX class="h-12 w-12 text-destructive" />
					</div>
				</div>
				<div class="text-center space-y-3 animate-fade-in-up">
					<h3 class="text-2xl font-bold text-destructive">
						Verification Failed
					</h3>
					<p class="text-center text-foreground/90 text-base">{errorMessage || 'Unable to verify your email'}</p>
					<p class="text-sm text-muted-foreground">
						Redirecting to login page...
					</p>
				</div>
			{/if}
		</CardContent>
	</Card>
</div>
