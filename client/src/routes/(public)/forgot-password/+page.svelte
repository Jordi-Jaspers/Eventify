<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
</script>

<svelte:head>
	<title>Forgot Password - Eventify</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-background px-4 py-8">
	<div class="w-full max-w-md">
		<Card class="shadow-md">
			<CardHeader class="space-y-1">
				<CardTitle class="text-2xl font-bold text-center">Reset Password</CardTitle>
				<CardDescription class="text-center">
					Password reset coming soon
				</CardDescription>
			</CardHeader>
			<CardContent class="space-y-4">
				<p class="text-center text-muted-foreground">
					This feature is under development. Please contact support if you need to reset your password.
				</p>

				<Button href="/login" class="w-full">
					Back to Login
				</Button>
			</CardContent>
		</Card>
	</div>
</div>
