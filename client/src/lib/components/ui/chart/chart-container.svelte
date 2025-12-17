<script lang="ts">
	import { setContext } from 'svelte';
	import type { ChartConfig } from './types';
	import type { Snippet } from 'svelte';

	interface Props {
		config: ChartConfig;
		class?: string;
		children?: Snippet;
	}

	let { config, class: className, children }: Props = $props();

	setContext('chart-config', config);
</script>

<div class={className}>
	{@render children?.()}
</div>
