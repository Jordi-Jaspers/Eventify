<script lang="ts">
	import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { ShieldAlert, ShieldCheck } from 'lucide-svelte';
	import { PasswordMeter } from '$lib/components/general';
	import { Privacy, Submit } from '$lib/components/button';

	let formData = $state<RegisterRequest>({
		firstName: '',
		lastName: '',
		email: '',
		password: '',
		passwordConfirmation: ''
	});

	let errorMessage: string | undefined;
	let registerResponse: RegisterResponse | undefined;
	async function handleSubmit() {
		isLoading = true;
		console.log(formData);
		isLoading = false;
	}

	let showPassword: boolean = $state(false);
	let isPasswordValid:boolean = $state(false);
	let isLoading:boolean = $state(false);
	let isDisabled = $derived(isLoading || !isPasswordValid);
</script>

<form id="register" onsubmit={handleSubmit}>
	<CardHeader>
		<CardTitle class="text-2xl">Account Creation</CardTitle>
		<CardDescription>Create a new account to get started</CardDescription>
	</CardHeader>
	<CardContent class="grid gap-4">
		<div class="grid grid-cols-2 gap-2">
			<div>
				<Label>First Name</Label>
				<Input id="firstName" type="text" placeholder="John" required bind:value={formData.firstName} />
			</div>
			<div>
				<Label>Last Name</Label>
				<Input id="lastName" type="text" placeholder="Doe" required bind:value={formData.lastName} />
			</div>
		</div>

		<div class="grid gap-2">
			<Label>Email</Label>
			<Input id="email" type="email" placeholder="johndoe@example.com" autocomplete="username" required
						 bind:value={formData.email} />
		</div>
		<div class="grid gap-2">
			<Label>Password</Label>
			<div class="relative">
				<Input type={showPassword ? "text" : "password"}
							 bind:value={formData.password}
							 placeholder="Password"
							 autocomplete="current-password"
							 class="pr-10"
							 required
				/>
				<Privacy bind:enabled={showPassword}/>
			</div>
		</div>
		<div class="grid gap-2">
			<Label>Confirm Password</Label>
			<div class="relative">
				<Input
					id="passwordConfirmation"
					type="password"
					autocomplete="new-password"
					placeholder="Confirm password"
					required
					bind:value={formData.passwordConfirmation}
				/>
			</div>
		</div>
		<PasswordMeter
			password={formData.password}
			confirmation={formData.passwordConfirmation}
			bind:isValid={isPasswordValid}
		/>

		{#if errorMessage && registerResponse === undefined}
			<div class="mx-1 flex flex-row content-center justify-center space-x-2 text-sm text-red-500">
				<ShieldAlert class="m-2 w-[10%]" />
				<span class="w-[90%]">
						{errorMessage}
					</span>
			</div>
		{/if}

		{#if registerResponse && !registerResponse.validated && errorMessage === undefined}
			<div class="space-x- mx-1 flex flex-row content-center justify-center text-sm text-green-500">
				<ShieldCheck class="m-2 w-[10%]" />
				<span class="w-[90%]">
						You have successfully registered. Please check your mailbox to verify your email address.
						<button type="button" class="underline"> Resend email </button>
					</span>
			</div>
		{/if}
	</CardContent>
	<CardFooter>
		<Submit isLoading={isLoading} isDisabled={isDisabled} title="Create Account" form="register" />
	</CardFooter>
</form>
