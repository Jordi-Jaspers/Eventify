<script lang="ts">
	import type { Timeline, TimelineDuration } from '$lib/api/models';
	import TimelineSegment from './TimelineSegment.svelte';

	interface Props {
		timeline: Timeline;
		rangeStart: Date;
		rangeEnd: Date;
		onSegmentClick?: (duration: TimelineDuration) => void;
	}

	let { timeline, rangeStart, rangeEnd, onSegmentClick }: Props = $props();
</script>

<div class="relative h-2 bg-muted/20 rounded-sm overflow-hidden">
	{#each timeline.durations as duration, index (`${index}-${duration.startTime}`)}
		<TimelineSegment
			{duration}
			{rangeStart}
			{rangeEnd}
			onclick={onSegmentClick ? () => onSegmentClick(duration) : undefined}
		/>
	{/each}
</div>
