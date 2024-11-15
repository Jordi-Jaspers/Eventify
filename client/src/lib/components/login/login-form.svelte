<script lang="ts">
	import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { ShieldAlert, ShieldEllipsis } from 'lucide-svelte';
	import { CLIENT_URLS } from '$lib/paths';
	import { Privacy, Submit } from '$lib/components/button/index.js';
	import GithubButton from '$lib/components/login/github.svelte';
	import OTPButton from '$lib/components/login/otp.svelte';

	let formData = $state<LoginRequest>({
		email: '',
		password: ''
	});

	let errorMessage: string | undefined;
	let authorizeResponse: AuthorizeResponse | undefined;
	let isLoading = $state(false);
	let showPassword: boolean = $state(false);

	async function handleSubmit() {
		isLoading = true;
		console.log(formData);
		isLoading = false;
	}

</script>

<form id="login" onsubmit={handleSubmit}>
	<CardHeader>
		<CardTitle class="text-2xl">Sign In</CardTitle>
		<CardDescription>Enter your email and password to access your account and start your monitoring journey
		</CardDescription>
	</CardHeader>
	<CardContent class="grid w-full items-center gap-4">
		<div>
			<Label>Email</Label>
			<Input placeholder="johndoe@example.com" autocomplete="email" required bind:value={formData.email} />
		</div>
		<div class="grid gap-2">
			<div class="flex flex-row items-center justify-between">
				<Label>Password</Label>
				<a href={CLIENT_URLS.FORGOT_PASSWORD_URL} class="text-sm text-blue-500 hover:underline">Forgot password?</a>
			</div>
			<div class="relative">
				<Input type={showPassword ? "text" : "password"}
							 bind:value={formData.password}
							 placeholder="Password"
							 autocomplete="current-password"
							 class="pr-10"
							 required
				/>
				<Privacy bind:enabled={showPassword} />
			</div>
		</div>

		{#if errorMessage && authorizeResponse === undefined}
			<div class="mx-1 flex flex-row content-center justify-center space-x-2 text-sm text-red-500">
				<ShieldAlert class="m-2 w-[10%]" />
				<span class="w-[90%]">
						{errorMessage}
					</span>
			</div>
		{/if}

		{#if authorizeResponse && !authorizeResponse.validated && errorMessage === undefined}
			<div class="mx-1 flex flex-row content-center justify-center space-x-2 text-sm text-orange-500">
				<ShieldEllipsis class="m-2 w-[10%]" />
				<span class="w-[90%]">
						Your account has not been validated. Please check your email for a validation link.
						<button type="button" class="underline"> Resend email </button>
					</span>
			</div>
		{/if}

		<Submit isLoading={isLoading} isDisabled={isLoading} title="Log in" form="login" />
		<div class="relative">
			<div class="absolute inset-0 flex items-center">
					<span class="w-full border-t">
					</span>
			</div>
			<div class="relative flex justify-center text-xs uppercase">
				<span class="bg-background px-2 text-muted-foreground"> Or continue with </span>
			</div>
		</div>
	</CardContent>
	<CardFooter class="flex flex-col space-y-2">
		<GithubButton isLoading={isLoading} />
		<OTPButton isLoading={isLoading} />
	</CardFooter>
</form>
