<script lang="ts">
	import { Card, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Building2, Shield } from '@lucide/svelte';

	interface Props {
		name: string;
		isGlobalAdmin: boolean;
	}

	let { name, isGlobalAdmin }: Props = $props();
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
	<!-- Gradient overlay -->
	<div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>
	
	<CardHeader class="relative z-10 pb-8">
		<div class="flex items-center gap-3 mb-2">
			<div class="p-3 rounded-xl bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30">
				<Building2 class="w-6 h-6 text-primary" />
			</div>
			{#if isGlobalAdmin}
				<Badge variant="outline" class="text-xs">
					<Shield class="mr-1 h-3 w-3" />
					Global Admin{#if name !== 'Unknown Organization'}istrator{/if} View
				</Badge>
			{/if}
		</div>
		<CardTitle class="text-4xl font-bold text-primary">
			{name}
		</CardTitle>
		<CardDescription class="text-base mt-2">Organization Details & Information</CardDescription>
	</CardHeader>
</Card>
