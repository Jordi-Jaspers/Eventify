<script lang="ts">
	import { formatTime } from '$lib/utils/date';
	import { formatDurationLength } from '$lib/utils/duration';
	import type { TimelineDuration, Severity } from '$lib/api/models';
	import { getSeverityColors } from './types';
	import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '$lib/components/ui/tooltip';

	interface Props {
		durations: TimelineDuration[];
		selectedDuration: TimelineDuration | null;
		onDurationClick: (duration: TimelineDuration) => void;
	}

	let { durations, selectedDuration, onDurationClick }: Props = $props();

	// Find index of selected duration
	const selectedIndex = $derived.by(() => {
		if (!selectedDuration) return -1;
		return durations.findIndex(d => 
			d.startTime === selectedDuration.startTime && d.endTime === selectedDuration.endTime
		);
	});

	// Get the durations to display: previous (if exists), selected, next (if exists)
	const visibleDurations = $derived.by(() => {
		if (selectedIndex === -1 || durations.length === 0) return [];
		
		const result: { duration: TimelineDuration; position: 'previous' | 'selected' | 'next' }[] = [];
		
		// Previous duration (if exists)
		if (selectedIndex > 0) {
			result.push({ duration: durations[selectedIndex - 1], position: 'previous' });
		}
		
		// Selected duration
		result.push({ duration: durations[selectedIndex], position: 'selected' });
		
		// Next duration (if exists)
		if (selectedIndex < durations.length - 1) {
			result.push({ duration: durations[selectedIndex + 1], position: 'next' });
		}
		
		return result;
	});

	// Calculate the time range for the visible window
	const timeRange = $derived.by(() => {
		if (visibleDurations.length === 0) return { start: 0, end: 0, totalMs: 0 };
		
		const start = new Date(visibleDurations[0].duration.startTime).getTime();
		const end = new Date(visibleDurations[visibleDurations.length - 1].duration.endTime).getTime();
		
		return { start, end, totalMs: end - start };
	});

	// Calculate width and position for each duration
	function getSegmentStyle(duration: TimelineDuration): { left: string; width: string } {
		if (timeRange.totalMs === 0) return { left: '0%', width: '100%' };
		
		const start = new Date(duration.startTime).getTime();
		const end = new Date(duration.endTime).getTime();
		
		const leftPercent = ((start - timeRange.start) / timeRange.totalMs) * 100;
		const widthPercent = ((end - start) / timeRange.totalMs) * 100;
		
		return {
			left: `${Math.max(0, leftPercent)}%`,
			width: `${Math.max(1, widthPercent)}%`
		};
	}

	// Time markers for the visible range
	const timeMarkers = $derived.by(() => {
		if (visibleDurations.length === 0) return [];
		
		const markers: { label: string; left: number }[] = [];
		
		// Add start time of first visible duration
		markers.push({
			label: formatTime(visibleDurations[0].duration.startTime),
			left: 0
		});
		
		// Add end time of last visible duration
		markers.push({
			label: formatTime(visibleDurations[visibleDurations.length - 1].duration.endTime),
			left: 100
		});
		
		return markers;
	});

	function getSegmentClasses(severity: Severity): string {
		switch (severity) {
			case 'CRITICAL':
				return 'bg-red-500 shadow-red-500/30';
			case 'WARNING':
				return 'bg-amber-500 shadow-amber-500/30';
			case 'OK':
				return 'bg-green-500 shadow-green-500/30';
			default:
				return 'bg-gray-400 shadow-gray-400/20';
		}
	}

	// Navigation helpers
	const hasPrevious = $derived(selectedIndex > 0);
	const hasNext = $derived(selectedIndex < durations.length - 1);

	function goToPrevious(): void {
		if (hasPrevious) {
			onDurationClick(durations[selectedIndex - 1]);
		}
	}

	function goToNext(): void {
		if (hasNext) {
			onDurationClick(durations[selectedIndex + 1]);
		}
	}
</script>

<div class="space-y-1.5 select-none">
	<!-- Navigation and Position Indicator -->
	<div class="flex items-center justify-between text-xs text-muted-foreground">
		<div class="flex items-center gap-2">
			<button
				type="button"
				class="px-2 py-0.5 rounded hover:bg-muted/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
				disabled={!hasPrevious}
				onclick={goToPrevious}
			>
				← Previous
			</button>
		</div>
		
		<span class="font-mono text-[10px]">
			Duration {selectedIndex + 1} of {durations.length}
		</span>
		
		<div class="flex items-center gap-2">
			<button
				type="button"
				class="px-2 py-0.5 rounded hover:bg-muted/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
				disabled={!hasNext}
				onclick={goToNext}
			>
				Next →
			</button>
		</div>
	</div>

	<!-- Time Labels -->
	<div class="relative h-4 text-[10px] text-muted-foreground/70 font-mono">
		{#each timeMarkers as marker}
			<div 
				class="absolute transform transition-opacity"
				class:-translate-x-full={marker.left === 100}
				style="left: {marker.left}%"
			>
				{marker.label}
			</div>
		{/each}
	</div>

	<!-- Timeline Track -->
	<div class="relative h-8 bg-muted/10 rounded-md border border-border/20 overflow-hidden">
		<!-- Segments -->
		{#each visibleDurations as item (item.duration.startTime)}
			{@const style = getSegmentStyle(item.duration)}
			{@const isSelected = item.position === 'selected'}
			{@const colors = getSeverityColors(item.duration.severity)}
			
			<TooltipProvider>
				<Tooltip>
					<TooltipTrigger 
						class="absolute h-full top-0 cursor-pointer transition-all duration-200 ease-out z-10 group"
						style="left: {style.left}; width: {style.width}"
						onclick={() => onDurationClick(item.duration)}
					>
						<div class="h-full w-full py-1 px-0.5">
							<div 
								class="h-full rounded-sm shadow-sm transition-all duration-200 flex items-center justify-center
									{getSegmentClasses(item.duration.severity)}
									{isSelected 
										? 'ring-2 ring-primary ring-offset-1 ring-offset-card shadow-lg' 
										: 'opacity-50 hover:opacity-80 group-hover:shadow-md'
									}"
							>
								<!-- Show duration length inside if wide enough -->
								{#if isSelected}
									<span class="text-[10px] font-medium text-white drop-shadow-sm truncate px-1">
										{formatDurationLength(item.duration.startTime, item.duration.endTime)}
									</span>
								{/if}
							</div>
						</div>
					</TooltipTrigger>
					<TooltipContent side="bottom" class="text-xs">
						<div class="space-y-1">
							<p class="font-semibold {colors.text}">{item.duration.severity}</p>
							<p class="text-muted-foreground font-mono">
								{formatTime(item.duration.startTime)} – {formatTime(item.duration.endTime)}
							</p>
							<p class="text-muted-foreground/80">
								Duration: {formatDurationLength(item.duration.startTime, item.duration.endTime)}
							</p>
							{#if item.position === 'previous'}
								<p class="text-primary text-[10px]">Click to view previous duration</p>
							{:else if item.position === 'next'}
								<p class="text-primary text-[10px]">Click to view next duration</p>
							{/if}
						</div>
					</TooltipContent>
				</Tooltip>
			</TooltipProvider>
		{/each}

		<!-- Edge indicators when there are more durations -->
		{#if hasPrevious && visibleDurations[0]?.position !== 'previous'}
			<div class="absolute left-0 top-0 bottom-0 w-4 bg-gradient-to-r from-muted/50 to-transparent pointer-events-none z-20 flex items-center justify-start pl-0.5">
				<span class="text-muted-foreground/50 text-xs">‹</span>
			</div>
		{/if}
		{#if hasNext && visibleDurations[visibleDurations.length - 1]?.position !== 'next'}
			<div class="absolute right-0 top-0 bottom-0 w-4 bg-gradient-to-l from-muted/50 to-transparent pointer-events-none z-20 flex items-center justify-end pr-0.5">
				<span class="text-muted-foreground/50 text-xs">›</span>
			</div>
		{/if}
	</div>

	<!-- Legend for context -->
	{#if visibleDurations.length > 1}
		<div class="flex items-center justify-center gap-4 text-[10px] text-muted-foreground/60">
			{#if visibleDurations.some(v => v.position === 'previous')}
				<span>← Previous duration</span>
			{/if}
			<span class="font-medium text-foreground/60">Selected</span>
			{#if visibleDurations.some(v => v.position === 'next')}
				<span>Next duration →</span>
			{/if}
		</div>
	{/if}
</div>
