<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { KeyRound } from '@lucide/svelte';
</script>

<svelte:head>
	<title>Forgot Password - Eventify</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-background px-4 py-8">
	<div class="w-full max-w-md">
		<Card class="bg-card/50 backdrop-blur-xl border-border/50 shadow-lg">
			<CardHeader class="space-y-3">
				<div class="flex justify-center">
					<div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
						<KeyRound class="h-6 w-6 text-primary" />
					</div>
				</div>
				<CardTitle class="text-2xl font-bold text-center">Reset Password</CardTitle>
				<CardDescription class="text-center">
					Password reset coming soon
				</CardDescription>
			</CardHeader>
			<CardContent class="space-y-4">
				<p class="text-center text-muted-foreground text-sm">
					This feature is under development. Please contact support if you need to reset your password.
				</p>

				<Button href="/login" class="w-full bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all">
					Back to Login
				</Button>
			</CardContent>
		</Card>
	</div>
</div>
