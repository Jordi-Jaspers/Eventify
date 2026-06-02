<script lang="ts">
	import { formatTime, formatDate } from '$lib/utils/date';
	import { formatDurationLength } from '$lib/utils/duration';
	import type { TimelineDuration } from '$lib/api/models';
	import { getSeverityColors } from './monitor-utils';

	interface Props {
		duration: TimelineDuration;
		index: number;
		hasPrevious: boolean;
		isCollapsed: boolean;
		isOngoing: boolean;
	}

	let { duration, index, hasPrevious, isCollapsed, isOngoing }: Props = $props();

	const colors = $derived(getSeverityColors(duration.severity));
</script>

<div class="space-y-1">
	<div class="flex items-center gap-2">
		<span class="font-semibold {colors.text}">{duration.severity}</span>
		{#if isCollapsed}
			<span class="text-[10px] bg-muted px-1 rounded">Collapsed (>24h)</span>
		{/if}
		{#if isOngoing}
			<span class="text-[10px] bg-primary/20 text-primary px-1 rounded animate-pulse">Ongoing</span>
		{/if}
	</div>
	<p class="text-muted-foreground font-mono">
		{formatTime(duration.startTime)} – {duration.endTime ? formatTime(duration.endTime) : 'Now'}
	</p>
	<p class="text-muted-foreground/80">
		Duration: {formatDurationLength(duration.startTime, duration.endTime ?? new Date())}
	</p>
	{#if index === 0 && hasPrevious}
		<p class="text-[10px] text-muted-foreground mt-1 border-t border-border/50 pt-1">
			Started: {formatDate(duration.startTime)}
		</p>
	{/if}
</div>
