<script lang="ts">
	import type { Component } from 'svelte';

	interface Props {
		label: string;
		value: string;
		icon: Component<any>;
		class?: string;
	}

	let { label, value, icon: IconComponent, class: className = '' }: Props = $props();
</script>

<div class="p-4 rounded-lg bg-background/50 border border-border/50 {className}">
	<div class="flex items-center gap-2 mb-1">
		<IconComponent class="w-3 h-3 text-primary" />
		<p class="text-xs text-muted-foreground">{label}</p>
	</div>
	<p class="font-medium text-foreground">{value}</p>
</div>
