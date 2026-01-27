<script lang="ts">
	import { calculateTimeTicks, type TimeTick } from './types';
	import { Clock } from '@lucide/svelte';

	interface Props {
		rangeStart: Date;
		rangeEnd: Date;
		isLive: boolean;
	}

	let { rangeStart, rangeEnd, isLive }: Props = $props();

	const ticks: TimeTick[] = $derived(calculateTimeTicks(rangeStart, rangeEnd));
</script>

<div
	class="sticky top-0 z-10 bg-card/95 backdrop-blur-sm border-b border-border/30 py-4 px-6 mb-6"
>
	<div class="flex items-center justify-between mb-2">
		<div class="flex items-center gap-2 text-sm text-muted-foreground">
			<Clock class="h-4 w-4" />
			<span>Timeline</span>
		</div>
		{#if isLive}
			<div class="flex items-center gap-2">
				<div class="h-2 w-2 rounded-full bg-green-500 animate-pulse"></div>
				<span class="text-xs text-green-500 font-medium">Live</span>
			</div>
		{/if}
	</div>

	<!-- Time axis -->
	<div class="relative h-8 mt-2">
		{#each ticks as tick, index (`tick-${index}-${tick.timestamp.getTime()}`)}
			<div
				class="absolute flex flex-col items-center"
				style="left: {tick.position}%;"
			>
				<!-- Tick mark -->
				<div class="h-2 w-px bg-border"></div>
				<!-- Label -->
				<span class="text-xs text-muted-foreground mt-1">{tick.label}</span>
			</div>
		{/each}
	</div>
</div>
