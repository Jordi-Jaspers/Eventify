<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { applyAction, enhance } from '$app/forms';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { Submit } from '$lib/components/button';
	import { toast } from 'svelte-sonner';
	import { Logo } from '$lib/components/general';

	let formData = $state({
		email: ''
	});

	let isLoading = $state(false);
	const resend: SubmitFunction = () => {
		isLoading = true;
		return async ({ result }) => {
			isLoading = false;
			toast.success('You will receive a password reset link shortly, if the email exists.');
			await applyAction(result);
		};
	};
</script>

<div class="flex h-full flex-col">
	<Logo />
	<div class="flex h-full w-full items-center justify-center px-4">
		<Card class="snake">
			<CardHeader>
				<CardTitle class="text-2xl">Forgot Password</CardTitle>
				<CardDescription>Enter your email to receive a password reset link</CardDescription>
			</CardHeader>
			<CardContent class="grid w-full items-center gap-4">
				<form id="resend" method="POST" use:enhance={resend}>
					<div class="grid w-full max-w-sm grid-cols-5 items-center space-x-2">
						<Input
							class="col-span-4"
							id="email"
							name="email"
							type="email"
							bind:value={formData.email}
							placeholder="Enter your email..."
							autocomplete="username"
							required
						/>
						<Submit {isLoading} isDisabled={isLoading} title="Resend" form="resend" />
					</div>
				</form>
			</CardContent>
		</Card>
	</div>
</div>
