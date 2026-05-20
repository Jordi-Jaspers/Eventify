<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		title: string;
		description: string;
		actions?: Snippet;
	}

	let { title, description, actions }: Props = $props();
</script>

<div class="flex items-center justify-between mb-8">
	<div>
		<h1 class="text-3xl font-bold text-primary">{title}</h1>
		<p class="text-muted-foreground mt-2">{description}</p>
	</div>
	{#if actions}
		{@render actions()}
	{/if}
</div>
