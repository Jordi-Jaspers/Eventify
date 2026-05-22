<script lang="ts">
	import { BarChart3 } from '@lucide/svelte';
	import { Card, CardContent } from '$lib/components/ui/card';
</script>

<svelte:head>
	<title>Statistics - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<div>
			<h1 class="text-3xl font-bold text-primary">Statistics</h1>
			<p class="text-muted-foreground mt-1">Organization analytics and insights</p>
		</div>

		<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
			<CardContent class="flex flex-col items-center justify-center py-20 gap-4">
				<BarChart3 class="size-12 text-muted-foreground/50" />
				<div class="text-center">
					<p class="text-lg font-medium">Coming Soon</p>
					<p class="text-muted-foreground text-sm mt-1">Organization statistics will be available here.</p>
				</div>
			</CardContent>
		</Card>
	</div>
</main>
