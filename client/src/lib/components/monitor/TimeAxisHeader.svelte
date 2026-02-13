<script lang="ts">
	import type { TimeTick } from './types';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';

	interface Props {
		rangeStart: Date;
		rangeEnd: Date;
		timeTicks: TimeTick[];
		isLive: boolean;
		lastUpdated: Date | null;
	}

	let { rangeStart, rangeEnd, timeTicks, isLive, lastUpdated }: Props = $props();
</script>

<!-- Grid layout matching MonitorRow: 32px icon, minmax name, 1fr timeline, 32px status -->
<div class="grid grid-cols-[32px_minmax(120px,180px)_1fr_32px] gap-3 items-end px-4 py-1 bg-muted/20 border-b border-border/50">
	<!-- Live/Updated status (spans icon + name columns) -->
	<div class="col-span-2 flex flex-col pb-0.5 pl-4">
		{#if isLive}
			<span class="text-[11px] font-medium text-green-500 flex items-center gap-1">
				<PulseIndicator variant="green" size="xs" />
				Live
			</span>
		{/if}
		{#if lastUpdated}
			<span class="text-[11px] text-muted-foreground">
				Last updated {lastUpdated.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
			</span>
		{/if}
	</div>

	<!-- Time Axis -->
	<div class="min-w-0 relative h-5">
		<div class="absolute bottom-0 left-0 right-0 h-px bg-border"></div>

		<!-- Start time -->
		<div class="absolute left-0 bottom-0 flex flex-col items-start">
			<span class="text-[10px] text-muted-foreground mb-0.5">
				{rangeStart.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
			</span>
			<div class="h-1 w-px bg-border"></div>
		</div>

		<!-- Intermediate ticks -->
		{#each timeTicks as tick, index (`tick-${index}-${tick.timestamp.getTime()}`)}
			{#if tick.position > 12 && tick.position < 88}
				<div
					class="absolute bottom-0 flex flex-col items-center transform -translate-x-1/2"
					style="left: {tick.position}%;"
				>
					<span class="text-[10px] text-muted-foreground mb-0.5">{tick.label}</span>
					<div class="h-1 w-px bg-border"></div>
				</div>
			{/if}
		{/each}

		<!-- End time / Now -->
		<div class="absolute right-0 bottom-0 flex flex-col items-end">
			<span class="text-[10px] text-muted-foreground mb-0.5">
				{#if isLive}
					Now
				{:else}
					{rangeEnd.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
				{/if}
			</span>
			<div class="h-1 w-px bg-border"></div>
		</div>
	</div>

	<!-- Status placeholder -->
	<div></div>
</div>
