<script lang="ts">
	import type { Timeline, TimelineDuration } from '$lib/api/models';
	import TimelineSegment from './TimelineSegment.svelte';
	import { mergeSubPixelDurations } from './types';

	interface Props {
		timeline: Timeline;
		rangeStart: Date;
		rangeEnd: Date;
		isAggregated?: boolean;
		onSegmentClick?: (duration: TimelineDuration) => void;
	}

	let { timeline, rangeStart, rangeEnd, isAggregated = false, onSegmentClick }: Props = $props();

	let containerWidth: number = $state(0);

	const displayDurations: TimelineDuration[] = $derived(
		mergeSubPixelDurations(timeline.durations ?? [], rangeStart, rangeEnd, containerWidth)
	);
</script>

<div class="relative h-2 bg-muted/20 rounded-sm overflow-hidden" bind:clientWidth={containerWidth}>
	{#each displayDurations as duration, index (`${index}-${duration.startTime}`)}
		<TimelineSegment
			{duration}
			{rangeStart}
			{rangeEnd}
			{isAggregated}
			onclick={onSegmentClick ? () => onSegmentClick(duration) : undefined}
		/>
	{/each}
</div>
