<script lang="ts">
	import { page } from '$app/state';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';
	import { CircleAlert, KeyRound } from '@lucide/svelte';
	import NewPasswordForm from './NewPasswordForm.svelte';

	const token: string = $derived(page.url.searchParams.get('token') ?? '');
</script>

<svelte:head>
	<title>Reset Password - Eventify</title>
</svelte:head>

<div class="max-w-md mx-auto">
	<div class="mb-8 text-center">
		<AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />
	</div>

	<Card class="border-border/50 bg-card/80 backdrop-blur-xl shadow-2xl">
		<CardHeader class="space-y-2">
			<CardTitle class="text-2xl flex items-center gap-3">
				<KeyRound class="h-5 w-5 text-primary" />
				Create New Password
			</CardTitle>
			<CardDescription>
				Enter your new password below
			</CardDescription>
		</CardHeader>

		<CardContent class="space-y-6">
			{#if !token}
				<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
					<CircleAlert class="h-4 w-4" />
					<AlertDescription>
						Invalid or missing reset token. Please request a new password reset.
					</AlertDescription>
				</Alert>

				<Button href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path} class="w-full">
					Request New Reset Link
				</Button>
			{:else}
				<NewPasswordForm {token} />
			{/if}
		</CardContent>
	</Card>

	<p class="text-center text-xs text-muted-foreground mt-6">
		By using this service, you agree to our Terms of Service and Privacy Policy
	</p>
</div>
