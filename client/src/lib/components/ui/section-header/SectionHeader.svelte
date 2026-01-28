<script lang="ts">
	import type { Component } from 'svelte';

	interface Props {
		title: string;
		icon: Component<any>;
		class?: string;
	}

	let { title, icon: IconComponent, class: className = '' }: Props = $props();
</script>

<h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2 {className}">
	<IconComponent class="w-4 h-4" />
	{title}
</h3>
