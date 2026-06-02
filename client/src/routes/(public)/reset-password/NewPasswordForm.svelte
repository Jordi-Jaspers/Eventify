<script lang="ts">
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import PasswordStrengthMeter from '$lib/components/registration/PasswordStrengthMeter.svelte';
	import { toast } from 'svelte-sonner';
	import { CircleAlert, LoaderCircle } from '@lucide/svelte';
	import { PasswordInput } from '$lib/components/ui/password-input';
	import { handleError } from '$lib/utils/error-handler';
	import { resetPassword } from '$lib/api/authentication/PasswordController';
	import { validatePassword } from '$lib/utils/password-validator';

	interface Props {
		token: string;
	}

	let { token }: Props = $props();

	let newPassword: string = $state('');
	let confirmPassword: string = $state('');
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

		if (!validateForm()) return;

		isSubmitting = true;
		errors = {};

		try {
			await resetPassword({ token, newPassword, confirmPassword });
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
</script>

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
		<PasswordInput
			id="newPassword"
			placeholder="Enter your new password"
			bind:value={newPassword}
			disabled={isSubmitting}
			ariaInvalid={!!errors.newPassword}
			ariaDescribedby={errors.newPassword ? 'newPassword-error' : undefined}
			required
		/>
		{#if errors.newPassword}
			<p id="newPassword-error" class="text-sm text-destructive">{errors.newPassword}</p>
		{/if}

		<PasswordStrengthMeter password={newPassword} />
	</div>

	<div class="space-y-2">
		<Label for="confirmPassword">Confirm Password</Label>
		<PasswordInput
			id="confirmPassword"
			placeholder="Confirm your new password"
			bind:value={confirmPassword}
			disabled={isSubmitting}
			ariaInvalid={!!errors.confirmPassword}
			ariaDescribedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
			required
		/>
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

	<Button type="submit" class="w-full" disabled={isSubmitting}>
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
