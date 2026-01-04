<script lang="ts">
	import { Database } from '@lucide/svelte';
	import type { Component } from 'svelte';

	interface Props {
		icon?: Component;
		title?: string;
		message?: string;
	}

	let {
		icon = Database,
		title = 'No results found',
		message = 'Try adjusting your search or filters'
	}: Props = $props();

	const IconComponent: Component = $derived(icon);
</script>

<div class="flex flex-col items-center justify-center py-12">
	<div class="relative">
		<!-- Glow background -->
		<div class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"></div>

		<!-- Icon -->
		<div
			class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 backdrop-blur-sm"
		>
			<IconComponent class="w-12 h-12 text-primary" />
		</div>
	</div>

	<h3 class="mt-6 text-lg font-semibold">{title}</h3>
	<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
		{message}
	</p>
</div>
