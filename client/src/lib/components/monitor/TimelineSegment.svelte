<script lang="ts">
	import type { TimelineDuration } from '$lib/api/models';
	import type { SegmentStyle } from './types';
	import { calculateSegmentStyle, getSeverityColors } from './types';

	interface Props {
		duration: TimelineDuration;
		rangeStart: Date;
		rangeEnd: Date;
		onclick?: () => void;
	}

	let { duration, rangeStart, rangeEnd, onclick }: Props = $props();

	const style: SegmentStyle = $derived(calculateSegmentStyle(duration, rangeStart, rangeEnd));
	const colors = $derived(getSeverityColors(duration.severity));
	const isClickable: boolean = $derived(!!onclick);
</script>

{#if isClickable}
	<button
		class="absolute h-full transition-all {colors.bg} cursor-pointer hover:opacity-80 hover:scale-y-110 border-0 p-0"
		style="left: {style.left}%; width: {style.width}%;"
		onclick={onclick}
		aria-label="{duration.severity} segment from {duration.startTime} to {duration.endTime}"
	></button>
{:else}
	<div
		class="absolute h-full transition-all {colors.bg}"
		style="left: {style.left}%; width: {style.width}%;"
	></div>
{/if}
