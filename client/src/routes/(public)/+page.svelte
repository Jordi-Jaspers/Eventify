<script lang="ts">
	import { isAuthenticated } from '$lib/stores/auth';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Activity, Users, Zap } from '@lucide/svelte';
</script>

<svelte:head>
	<title>Eventify - Real-time Event Monitoring</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto">
		<!-- Navigation Bar -->
		<div class="flex justify-between items-center mb-12">
			<h1 class="text-4xl font-bold text-foreground">Welcome to Eventify</h1>
			<div class="flex gap-2">
				{#if $isAuthenticated}
					<Button href={CLIENT_ROUTES.DASHBOARD_PAGE.path} class="bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all">Dashboard</Button>
				{:else}
					<Button href={CLIENT_ROUTES.LOGIN_PAGE.path} variant="outline">Login</Button>
					<Button href={CLIENT_ROUTES.REGISTER_PAGE.path} class="bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all">Sign up</Button>
				{/if}
			</div>
		</div>

		<p class="text-xl text-muted-foreground mb-12">
			A high-performance, scalable monitoring solution for tracking and visualizing the health and status of distributed systems in real-time.
		</p>

		<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3 mb-12">
			<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl text-card-foreground shadow-lg p-6 space-y-3">
				<div class="flex items-center gap-3">
					<Activity class="h-6 w-6 text-primary" />
					<h2 class="text-2xl font-semibold">Real-time Monitoring</h2>
				</div>
				<p class="text-muted-foreground">
					Track events and system health with WebSocket-powered live updates.
				</p>
			</div>

			<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl text-card-foreground shadow-lg p-6 space-y-3">
				<div class="flex items-center gap-3">
					<Users class="h-6 w-6 text-primary" />
					<h2 class="text-2xl font-semibold">Team Collaboration</h2>
				</div>
				<p class="text-muted-foreground">
					Team-based access control and customizable dashboards for your organization.
				</p>
			</div>

			<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl text-card-foreground shadow-lg p-6 space-y-3">
				<div class="flex items-center gap-3">
					<Zap class="h-6 w-6 text-primary" />
					<h2 class="text-2xl font-semibold">Intelligent Events</h2>
				</div>
				<p class="text-muted-foreground">
					Smart event creation, processing, and visualization for better insights.
				</p>
			</div>
		</div>

		<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl shadow-lg p-6">
			<h3 class="text-xl font-semibold mb-4">Getting Started</h3>
			<ul class="space-y-2 text-muted-foreground">
				<li>✓ SvelteKit with TypeScript (strict mode)</li>
				<li>✓ TailwindCSS & shadcn-svelte components</li>
				<li>✓ Type-safe API client with OpenAPI integration</li>
				<li>✓ Svelte stores for state management</li>
				<li>✓ Bun for fast development</li>
			</ul>
		</div>
	</div>
</div>
