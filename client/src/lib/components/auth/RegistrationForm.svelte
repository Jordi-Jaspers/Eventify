<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Button } from '$lib/components/ui/button';
	import PasswordStrengthMeter from '$lib/components/registration/PasswordStrengthMeter.svelte';
	import OAuthButtons from '$lib/components/auth/OAuthButtons.svelte';
	import { validateEmail, validateName, validatePassword } from '$lib/utils/password-validator';
	import { toast } from 'svelte-sonner';
	import { LoaderCircle } from '@lucide/svelte';
	import { PasswordInput } from '$lib/components/ui/password-input';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		loading: boolean;
		onSubmit: (data: {
			firstName: string;
			lastName: string;
			email: string;
			password: string;
			passwordConfirmation: string;
		}) => Promise<void>;
	}

	let { loading, onSubmit }: Props = $props();

	let firstName: string = $state('');
	let lastName: string = $state('');
	let email: string = $state('');
	let password: string = $state('');
	let passwordConfirmation: string = $state('');

	let errors: {
		firstName?: string;
		lastName?: string;
		email?: string;
		password?: string;
		passwordConfirmation?: string;
	} = $state({});

	const passwordValidation = $derived(validatePassword(password));
	const passwordsMatch: boolean = $derived(
		passwordConfirmation.length > 0 && password === passwordConfirmation
	);
	const passwordsDontMatch: boolean = $derived(
		passwordConfirmation.length > 0 && password !== passwordConfirmation
	);

	function validateForm(): boolean {
		const newErrors: typeof errors = {};

		if (!firstName.trim()) {
			newErrors.firstName = 'First name is required';
		} else if (!validateName(firstName)) {
			newErrors.firstName = 'First name must be 1-255 characters';
		}

		if (!lastName.trim()) {
			newErrors.lastName = 'Last name is required';
		} else if (!validateName(lastName)) {
			newErrors.lastName = 'Last name must be 1-255 characters';
		}

		if (!email.trim()) {
			newErrors.email = 'Email is required';
		} else if (!validateEmail(email)) {
			newErrors.email = 'Please enter a valid email address';
		}

		if (!password) {
			newErrors.password = 'Password is required';
		} else if (!passwordValidation.isValid) {
			newErrors.password = 'Password does not meet all requirements';
		}

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
		await onSubmit({ firstName, lastName, email: email.toLowerCase(), password, passwordConfirmation });
	}
</script>

<form onsubmit={handleSubmit} class="space-y-4">
	<!-- First Name & Last Name -->
	<div class="grid grid-cols-2 gap-4">
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
				class="bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20"
			/>
			{#if errors.firstName}
				<p id="firstName-error" class="text-sm text-destructive">{errors.firstName}</p>
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
				class="bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20"
			/>
			{#if errors.lastName}
				<p id="lastName-error" class="text-sm text-destructive">{errors.lastName}</p>
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
			class="bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20"
		/>
		{#if errors.email}
			<p id="email-error" class="text-sm text-destructive">{errors.email}</p>
		{/if}
	</div>

	<!-- Password -->
	<div class="space-y-2">
		<Label for="password">
			Password
			<span class="text-destructive" aria-label="required">*</span>
		</Label>
		<PasswordInput
			id="password"
			placeholder="Enter a strong password"
			bind:value={password}
			required
			ariaInvalid={!!errors.password}
			ariaDescribedby={errors.password ? 'password-error' : undefined}
		/>
		{#if errors.password}
			<p id="password-error" class="text-sm text-destructive">{errors.password}</p>
		{/if}
		<PasswordStrengthMeter {password} />
	</div>

	<!-- Password Confirmation -->
	<div class="space-y-2">
		<Label for="passwordConfirmation">
			Confirm Password
			<span class="text-destructive" aria-label="required">*</span>
		</Label>
		<PasswordInput
			id="passwordConfirmation"
			placeholder="Re-enter your password"
			bind:value={passwordConfirmation}
			required
			ariaInvalid={!!errors.passwordConfirmation}
			ariaDescribedby={errors.passwordConfirmation ? 'passwordConfirmation-error' : undefined}
		/>
		{#if errors.passwordConfirmation}
			<p id="passwordConfirmation-error" class="text-sm text-destructive">{errors.passwordConfirmation}</p>
		{:else if passwordsMatch}
			<p class="text-sm text-green-500 flex items-center gap-1">
				<span class="inline-block w-1.5 h-1.5 rounded-full bg-green-500"></span>
				Passwords match
			</p>
		{:else if passwordsDontMatch}
			<p class="text-sm text-destructive">Passwords must match</p>
		{/if}
	</div>

	<!-- Submit Button -->
	<Button type="submit" class="w-full mt-6" disabled={loading}>
		{#if loading}
			<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
			Creating account...
		{:else}
			Create Account
		{/if}
	</Button>

	<OAuthButtons disabled={loading} />

	<div class="mt-6 text-center">
		<p class="text-sm text-muted-foreground">
			Already have an account?{' '}
			<a
				href={CLIENT_ROUTES.LOGIN_PAGE.path}
				class="text-primary hover:underline transition-colors font-medium"
			>
				Sign in
			</a>
		</p>
	</div>
</form>
