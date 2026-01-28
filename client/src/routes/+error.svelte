<script lang="ts">
	import { page } from '$app/state';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { CircleAlert, House, ServerCrash, ShieldAlert, FileQuestionMark } from '@lucide/svelte';
	import AppBackground from '$lib/components/layout/AppBackground.svelte';

	const status = $derived(page.status);
	const message = $derived(page.error?.message || 'An unexpected error occurred');
	const errorConfig = $derived.by(() => {
		switch (status) {
			case 404:
				return {
					icon: FileQuestionMark,
					title: 'Page Not Found',
					description: 'The page you\'re looking for doesn\'t exist or has been moved.',
					color: 'text-blue-500'
				};
			case 403:
				return {
					icon: ShieldAlert,
					title: 'Access Forbidden',
					description: 'You don\'t have permission to access this page.',
					color: 'text-amber-500'
				};
			case 500:
				return {
					icon: ServerCrash,
					title: 'Server Error',
					description: 'Something went wrong on our end. We\'re working to fix it.',
					color: 'text-red-500'
				};
			default:
				return {
					icon: CircleAlert,
					title: 'Something Went Wrong',
					description: 'An unexpected error occurred. Please try again.',
					color: 'text-destructive'
				};
		}
	});

	const Icon = $derived(errorConfig.icon);
</script>

<svelte:head>
	<title>{status} - {errorConfig.title} - Eventify</title>
</svelte:head>

<AppBackground>
	<div class="flex items-center justify-center min-h-screen px-4 py-8">
		<div class="w-full max-w-md animate-fade-in">
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
				<CardHeader class="space-y-4">
					<div class="flex justify-center">
						<div class="rounded-full bg-secondary/50 backdrop-blur-sm p-4">
							<Icon class="h-12 w-12 {errorConfig.color}" />
						</div>
					</div>
					<div class="text-center space-y-2">
						<div class="text-6xl font-bold text-muted-foreground/40">
							{status}
						</div>
						<CardTitle class="text-2xl font-bold">
							{errorConfig.title}
						</CardTitle>
						<CardDescription class="text-base">
							{errorConfig.description}
						</CardDescription>
					</div>
				</CardHeader>
				<CardContent class="space-y-4">
					{#if message && message !== errorConfig.description}
						<div class="p-4 rounded-lg bg-secondary/50 backdrop-blur-sm">
							<p class="text-sm text-muted-foreground text-center">
								{message}
							</p>
						</div>
					{/if}

					<div class="space-y-2">
						<Button
							href={CLIENT_ROUTES.LANDING_PAGE.path}
							class="w-full"
						>
							<House class="mr-2 h-4 w-4" />
							Go to Home
						</Button>

						{#if status === 403}
							<Button
								href={CLIENT_ROUTES.LOGIN_PAGE.path}
								variant="outline"
								class="w-full bg-background/50 border-border hover:bg-accent/10"
							>
								Sign In
							</Button>
						{:else if status !== 404}
							<Button
								variant="outline"
								class="w-full bg-background/50 border-border hover:bg-accent/10"
								onclick={() => window.location.reload()}
							>
								Try Again
							</Button>
						{/if}
					</div>

					<div class="text-center">
						<p class="text-sm text-muted-foreground">
							Need help?
							<a href={CLIENT_ROUTES.LANDING_PAGE.path} class="text-primary hover:text-accent transition-colors font-medium">
								Contact support
							</a>
						</p>
					</div>
				</CardContent>
			</Card>
		</div>
	</div>
</AppBackground>
