<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import Input from '$lib/components/ui/input/input.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';
	import PasswordStrengthMeter from '$lib/components/registration/PasswordStrengthMeter.svelte';
	import { toast } from 'svelte-sonner';
	import { CircleAlert, Eye, EyeOff, KeyRound, LoaderCircle } from '@lucide/svelte';
	import { handleError } from '$lib/utils/error-handler';
	import { resetPassword } from '$lib/api/authentication/PasswordController';
	import { validatePassword } from '$lib/utils/password-validator';

	const token: string = $derived(page.url.searchParams.get('token') ?? '');

	let newPassword: string = $state('');
	let confirmPassword: string = $state('');
	let showNewPassword: boolean = $state(false);
	let showConfirmPassword: boolean = $state(false);
	let isSubmitting: boolean = $state(false);
	let errors: Record<string, string> = $state({});

	const passwordValidation = $derived(validatePassword(newPassword));
	const passwordsMatch: boolean = $derived(
		confirmPassword.length > 0 && newPassword === confirmPassword
	);
	const passwordsDontMatch: boolean = $derived(
		confirmPassword.length > 0 && newPassword !== confirmPassword
	);

	function validateForm(): boolean {
		const newErrors: Record<string, string> = {};

		if (!newPassword) {
			newErrors.newPassword = 'New password is required';
		} else if (!passwordValidation.isValid) {
			newErrors.newPassword = 'Password does not meet all requirements';
		}

		if (!confirmPassword) {
			newErrors.confirmPassword = 'Please confirm your password';
		} else if (newPassword !== confirmPassword) {
			newErrors.confirmPassword = 'Passwords do not match';
		}

		errors = newErrors;
		return Object.keys(newErrors).length === 0;
	}

	async function handleSubmit(event: SubmitEvent): Promise<void> {
		event.preventDefault();

		if (!validateForm()) {
			return;
		}

		if (!token) {
			toast.error('Invalid or missing reset token');
			return;
		}

		isSubmitting = true;
		errors = {};

		try {
			await resetPassword({
				token,
				newPassword,
				confirmPassword
			});

			toast.success('Password reset successfully. Please log in.');
			goto(CLIENT_ROUTES.LOGIN_PAGE.path);
		} catch (error: unknown) {
			const { message } = handleError(error, 'Failed to reset password');

			if (message.toLowerCase().includes('token') || message.toLowerCase().includes('expired')) {
				errors.general = 'Your reset link has expired or is invalid. Please request a new one.';
			} else {
				errors.general = message;
			}
		} finally {
			isSubmitting = false;
		}
	}

	function toggleNewPasswordVisibility(): void {
		showNewPassword = !showNewPassword;
	}

	function toggleConfirmPasswordVisibility(): void {
		showConfirmPassword = !showConfirmPassword;
	}
</script>

<svelte:head>
	<title>Reset Password - Eventify</title>
</svelte:head>

<div class="max-w-md mx-auto">
	<div class="mb-8">
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

				<Button
					href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path}
					class="w-full"
				>
					Request New Reset Link
				</Button>
			{:else}
				{#if errors.general}
					<Alert variant="destructive" class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm">
						<CircleAlert class="h-4 w-4" />
						<AlertDescription>{errors.general}</AlertDescription>
					</Alert>

					{#if errors.general.toLowerCase().includes('expired') || errors.general.toLowerCase().includes('invalid')}
						<Button
							href={CLIENT_ROUTES.FORGOT_PASSWORD_PAGE.path}
							variant="outline"
							class="w-full mb-4"
						>
							Request New Reset Link
						</Button>
					{/if}
				{/if}

				<form onsubmit={handleSubmit} class="space-y-6">
					<div class="space-y-2">
						<Label for="newPassword">New Password</Label>
						<div class="relative">
							<Input
								id="newPassword"
								type={showNewPassword ? 'text' : 'password'}
								placeholder="Enter your new password"
								bind:value={newPassword}
								disabled={isSubmitting}
								class="pr-10 bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20"
								aria-invalid={!!errors.newPassword}
								aria-describedby={errors.newPassword ? 'newPassword-error' : undefined}
								required
							/>
							<button
								type="button"
								onclick={toggleNewPasswordVisibility}
								disabled={isSubmitting}
								class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
								aria-label={showNewPassword ? 'Hide password' : 'Show password'}
								tabindex="0"
							>
								{#if showNewPassword}
									<EyeOff class="h-4 w-4" />
								{:else}
									<Eye class="h-4 w-4" />
								{/if}
							</button>
						</div>
						{#if errors.newPassword}
							<p id="newPassword-error" class="text-sm text-destructive">{errors.newPassword}</p>
						{/if}

						<!-- Password Strength Meter -->
						<PasswordStrengthMeter password={newPassword} />
					</div>

					<div class="space-y-2">
						<Label for="confirmPassword">Confirm Password</Label>
						<div class="relative">
							<Input
								id="confirmPassword"
								type={showConfirmPassword ? 'text' : 'password'}
								placeholder="Confirm your new password"
								bind:value={confirmPassword}
								disabled={isSubmitting}
								class="pr-10 bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20"
								aria-invalid={!!errors.confirmPassword}
								aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
								required
							/>
							<button
								type="button"
								onclick={toggleConfirmPasswordVisibility}
								disabled={isSubmitting}
								class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
								aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
								tabindex="0"
							>
								{#if showConfirmPassword}
									<EyeOff class="h-4 w-4" />
								{:else}
									<Eye class="h-4 w-4" />
								{/if}
							</button>
						</div>
						{#if errors.confirmPassword}
							<p id="confirmPassword-error" class="text-sm text-destructive">{errors.confirmPassword}</p>
						{:else if passwordsMatch}
							<p class="text-sm text-green-400 flex items-center gap-1">
								<span class="inline-block w-1.5 h-1.5 rounded-full bg-green-400"></span>
								Passwords match
							</p>
						{:else if passwordsDontMatch}
							<p class="text-sm text-destructive">Passwords must match</p>
						{/if}
					</div>

					<Button
						type="submit"
						class="w-full"
						disabled={isSubmitting}
					>
						{#if isSubmitting}
							<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
							Resetting Password...
						{:else}
							Reset Password
						{/if}
					</Button>

					<div class="text-center">
						<a
							href={CLIENT_ROUTES.LOGIN_PAGE.path}
							class="text-sm text-primary hover:text-accent transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
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
