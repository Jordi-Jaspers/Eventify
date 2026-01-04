<script lang="ts">
	import { getContext } from 'svelte';
	import type { ChartConfig } from './types';

	const config: ChartConfig = getContext('chart-config');

	interface Props {
		data?: Record<string, unknown>;
	}

	let { data }: Props = $props();
</script>

<div class="rounded-lg border bg-background p-2 shadow-sm">
	{#if data}
		<div class="grid gap-2">
			{#each Object.entries(data) as [key, value]}
				{#if config[key]}
					<div class="flex items-center gap-2">
						<div
							class="h-2.5 w-2.5 rounded-full"
							style="background-color: {config[key].color}"
						></div>
						<span class="text-sm text-muted-foreground">{config[key].label}:</span>
						<span class="text-sm font-medium">{value}</span>
					</div>
				{/if}
			{/each}
		</div>
	{/if}
</div>
