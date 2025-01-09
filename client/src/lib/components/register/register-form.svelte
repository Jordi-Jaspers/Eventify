<script lang="ts">
	import { CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { PasswordMeter } from '$lib/components/general';
	import { Privacy, Submit } from '$lib/components/button';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { toast } from 'svelte-sonner';
	import { applyAction, enhance } from '$app/forms';

	let formData = $state<RegisterRequest>({
		firstName: '',
		lastName: '',
		email: '',
		password: '',
		passwordConfirmation: ''
	});

	let isLoading = $state(false);
	const register: SubmitFunction = () => {
		isLoading = true;
		return async ({ result }) => {
			isLoading = false;
			if (result.type === 'success') {
				toast.success('Registration successful! Please check your email to validate your account.');
				await applyAction(result);
			}

			if (result.type === 'failure' && result.data) {
				const apiResponse: ApiResponse = result.data.response;
				toast.error(apiResponse.message);
			}
		};
	};

	let showPassword: boolean = $state(false);
	let isPasswordValid: boolean = $state(false);
	let isDisabled = $derived(isLoading || !isPasswordValid);
</script>

<form id="register" action="?/register" method="POST" use:enhance={register}>
	<CardHeader>
		<CardTitle class="text-2xl">Account Creation</CardTitle>
		<CardDescription>Create a new account to get started</CardDescription>
	</CardHeader>
	<CardContent class="grid gap-4">
		<div class="grid grid-cols-2 gap-2">
			<div>
				<Label>First Name</Label>
				<Input
					id="firstName"
					name="firstName"
					type="text"
					bind:value={formData.firstName}
					placeholder="John"
					autocomplete="given-name"
					required
				/>
			</div>
			<div>
				<Label>Last Name</Label>
				<Input
					id="lastName"
					name="lastName"
					type="text"
					bind:value={formData.lastName}
					placeholder="Doe"
					autocomplete="family-name"
					required
				/>
			</div>
		</div>

		<div class="grid gap-2">
			<Label>Email</Label>
			<Input
				id="email"
				name="email"
				type="email"
				bind:value={formData.email}
				placeholder="johndoe@example.com"
				autocomplete="username"
				required
			/>
		</div>
		<div class="grid gap-2">
			<Label>Password</Label>
			<div class="relative">
				<Input
					id="password"
					name="password"
					type={showPassword ? 'text' : 'password'}
					bind:value={formData.password}
					placeholder="Password"
					autocomplete="current-password"
					required
				/>
				<Privacy bind:enabled={showPassword} />
			</div>
		</div>
		<div class="grid gap-2">
			<Label>Confirm Password</Label>
			<div class="relative">
				<Input
					id="passwordConfirmation"
					name="passwordConfirmation"
					type="password"
					autocomplete="new-password"
					bind:value={formData.passwordConfirmation}
					placeholder="Confirm password"
					required
				/>
			</div>
		</div>
		<PasswordMeter password={formData.password} confirmation={formData.passwordConfirmation} bind:isValid={isPasswordValid} />
	</CardContent>
	<CardFooter>
		<Submit {isLoading} {isDisabled} title="Create Account" form="register" />
	</CardFooter>
</form>
