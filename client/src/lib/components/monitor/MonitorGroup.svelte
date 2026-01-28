<script lang="ts">
	import { Radio, Folder, ChevronRight } from '@lucide/svelte';
	import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '$lib/components/ui/tooltip';
	import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '$lib/components/ui/collapsible';
	import TimelineBar from './TimelineBar.svelte';
	import MonitorRow from './MonitorRow.svelte';
	import type { Timeline, ChannelResponse } from '$lib/api/models';
	import { getSeverityColors, getCurrentSeverityFromTimeline } from './types';

	interface Props {
		name: string;
		timeline: Timeline;
		channels: ChannelResponse[];
		rangeStart: Date;
		rangeEnd: Date;
	}

	let { name, timeline, channels, rangeStart, rangeEnd }: Props = $props();

	let isExpanded = $state(false);

	const currentSeverity = $derived(getCurrentSeverityFromTimeline(timeline));
	const severityColors = $derived(currentSeverity ? getSeverityColors(currentSeverity) : null);
</script>

<Collapsible bind:open={isExpanded}>
	<!-- Group Header Row -->
	<CollapsibleTrigger class="w-full">
		<div class="grid grid-cols-[auto_minmax(120px,180px)_1fr_32px] gap-3 items-center px-4 py-2 hover:bg-muted/30 transition-colors border-b border-border/30 cursor-pointer">
			<!-- Icon Column - fixed width, right-aligned -->
			<div class="flex items-center justify-end gap-1 w-8">
				<ChevronRight 
					class="h-3.5 w-3.5 text-muted-foreground transition-transform duration-200 {isExpanded ? 'rotate-90' : ''}" 
				/>
				<Folder class="h-4 w-4 text-primary" />
			</div>

			<!-- Group Name -->
			<div class="truncate font-medium text-foreground text-left">
				{name}
				<span class="text-xs text-muted-foreground ml-1">({channels.length})</span>
			</div>

			<!-- Timeline Bar -->
			<div class="min-w-0">
				<TimelineBar
					{timeline}
					{rangeStart}
					{rangeEnd}
				/>
			</div>

			<!-- Status Indicator -->
			<div class="flex items-center justify-center">
				{#if currentSeverity && severityColors}
					<TooltipProvider>
						<Tooltip>
							<TooltipTrigger>
								<div
									class="w-2.5 h-2.5 rounded-full {severityColors.bg}"
									aria-label="Current severity: {currentSeverity}"
								></div>
							</TooltipTrigger>
							<TooltipContent>
								<p class="text-xs">{currentSeverity}</p>
							</TooltipContent>
						</Tooltip>
					</TooltipProvider>
				{:else}
					<div class="w-2.5 h-2.5 rounded-full bg-gray-400" aria-label="No data"></div>
				{/if}
			</div>
		</div>
	</CollapsibleTrigger>

	<!-- Sub-channels -->
	<CollapsibleContent>
		{#each channels as channel, idx (`sub-channel-${idx}-${channel.channelId}`)}
			{#if channel.timeline}
				<MonitorRow
					type="sub-channel"
					name={channel.channelName ?? 'Unnamed Channel'}
					timeline={channel.timeline}
					currentSeverity={channel.currentSeverity ?? null}
					status={channel.status ?? null}
					{rangeStart}
					{rangeEnd}
				/>
			{/if}
		{/each}
	</CollapsibleContent>
</Collapsible>
