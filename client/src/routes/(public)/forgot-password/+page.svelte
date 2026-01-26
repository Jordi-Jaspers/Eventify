<script lang="ts">
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import Input from '$lib/components/ui/input/input.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';
	import { toast } from 'svelte-sonner';
	import { CheckCircle, KeyRound, LoaderCircle, Mail } from '@lucide/svelte';
	import { handleError } from '$lib/utils/error-handler';
	import { requestPasswordReset } from '$lib/api/authentication/PasswordController';

	let email: string = $state('');
	let isSubmitting: boolean = $state(false);
	let showSuccessMessage: boolean = $state(false);

	async function handleSubmit(event: SubmitEvent): Promise<void> {
		event.preventDefault();

		if (!email.trim()) {
			toast.error('Please enter your email address');
			return;
		}

		isSubmitting = true;

		try {
			await requestPasswordReset(email.trim());
			showSuccessMessage = true;
		} catch (error: unknown) {
			const { message } = handleError(error, 'Unable to process password reset request');
			toast.error(message);
		} finally {
			isSubmitting = false;
		}
	}
</script>

<svelte:head>
	<title>Forgot Password - Eventify</title>
</svelte:head>

<div class="max-w-md mx-auto">
	<div class="mb-8">
		<AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />
	</div>

	<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
		<CardHeader class="space-y-2">
			<CardTitle class="text-2xl flex items-center gap-2">
				<KeyRound class="w-5 h-5 text-primary" />
				Reset Password
			</CardTitle>
			<CardDescription>
				Enter your email address and we'll send you a reset link
			</CardDescription>
		</CardHeader>

		<CardContent>
			{#if showSuccessMessage}
				<div class="space-y-6">
					<Alert class="bg-primary/5 border-primary/30 backdrop-blur-sm">
						<CheckCircle class="h-5 w-5 text-primary" />
						<AlertDescription class="text-foreground">
							If an account exists with this email, you will receive a password reset link shortly.
						</AlertDescription>
					</Alert>

					<Button
						href={CLIENT_ROUTES.LOGIN_PAGE.path}
						class="w-full"
					>
						Back to Login
					</Button>
				</div>
			{:else}
				<form onsubmit={handleSubmit} class="space-y-4">
					<div class="space-y-2">
						<Label for="email">Email</Label>
						<div class="relative">
							<Mail class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
							<Input
								id="email"
								type="email"
								placeholder="you@example.com"
								bind:value={email}
								disabled={isSubmitting}
								class="pl-10 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
								required
							/>
						</div>
					</div>

					<Button
						type="submit"
						class="w-full"
						disabled={isSubmitting}
					>
						{#if isSubmitting}
							<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
							Sending...
						{:else}
							Send Reset Link
						{/if}
					</Button>

					<div class="text-center">
						<a
							href={CLIENT_ROUTES.LOGIN_PAGE.path}
							class="text-sm text-primary hover:text-accent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm"
							tabindex={isSubmitting ? -1 : 0}
						>
							Back to Login
						</a>
					</div>
				</form>
			{/if}
		</CardContent>
	</Card>

	<p class="text-center text-xs text-muted-foreground mt-6">
		By using this service, you agree to our Terms of Service and Privacy Policy
	</p>
</div>
