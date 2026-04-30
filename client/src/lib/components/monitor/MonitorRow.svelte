<script lang="ts">
	import { Radio, Folder, Pause, LayoutList } from '@lucide/svelte';
	import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '$lib/components/ui/tooltip';
	import TimelineBar from './TimelineBar.svelte';
	import type { Timeline, Severity, TimelineDuration } from '$lib/api/models';
	import { getSeverityColors } from './types';

	type RowType = 'dashboard' | 'channel' | 'group' | 'sub-channel';
	type ChannelStatus = 'ACTIVE' | 'PAUSED' | 'PENDING_DELETION';

	interface Props {
		type: RowType;
		name: string;
		timeline: Timeline;
		currentSeverity: Severity | null;
		status: ChannelStatus | null;
		rangeStart: Date;
		rangeEnd: Date;
		isAggregated?: boolean;
		onSegmentClick?: (duration: TimelineDuration) => void;
	}

	let { type, name, timeline, currentSeverity, status, rangeStart, rangeEnd, isAggregated = false, onSegmentClick }: Props = $props();

	const isPaused: boolean = $derived(status === 'PAUSED');

	const severityColors = $derived(
		currentSeverity ? getSeverityColors(currentSeverity) : null
	);

	const rowClasses: string = $derived(
		type === 'dashboard'
			? 'py-2.5 bg-muted/20'
			: 'py-2 hover:bg-muted/30 transition-colors'
	);

	const nameClasses: string = $derived(
		type === 'dashboard' ? 'font-semibold text-foreground' : 'font-medium text-foreground'
	);
</script>

<div class="grid grid-cols-[32px_minmax(120px,180px)_1fr_32px] gap-3 items-center px-4 {rowClasses} border-b border-border/30">
	<!-- Icon Column - fixed width, right-aligned for consistent positioning -->
	<div class="flex items-center justify-end gap-1 w-8">
		{#if type === 'dashboard'}
			<LayoutList class="h-4 w-4 text-primary" />
		{:else if type === 'group'}
			<Folder class="h-4 w-4 text-primary" />
		{:else if type === 'sub-channel'}
			<!-- Sub-channels: indent + channel icon -->
			<span class="text-muted-foreground text-xs mr-0.5">└</span>
			<Radio class="h-3.5 w-3.5 text-muted-foreground" />
		{:else}
			<!-- Regular channel -->
			<Radio class="h-4 w-4 text-primary" />
		{/if}
	</div>

	<!-- Channel/Group Name -->
	<div class="truncate {nameClasses}">
		{name}
	</div>

	<!-- Timeline Bar - always show, grey when paused -->
	<div class="min-w-0">
		{#if isPaused}
			<!-- Paused timeline: full dark grey bar -->
			<div class="h-2 bg-gray-400 rounded-sm w-full" aria-label="Paused - no data being collected"></div>
		{:else}
			<TimelineBar
				{timeline}
				{rangeStart}
				{rangeEnd}
				{isAggregated}
				{onSegmentClick}
			/>
		{/if}
	</div>

	<!-- Status Indicator - Pause icon if paused, otherwise severity dot -->
	<div class="flex items-center justify-center">
		{#if isPaused}
			<TooltipProvider>
				<Tooltip>
					<TooltipTrigger>
						<Pause class="h-4 w-4 text-muted-foreground" />
					</TooltipTrigger>
					<TooltipContent>
						<p class="text-xs">Paused</p>
					</TooltipContent>
				</Tooltip>
			</TooltipProvider>
		{:else if currentSeverity && severityColors}
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
