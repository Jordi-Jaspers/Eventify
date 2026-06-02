<script lang="ts">
	import type { TimelineDuration } from '$lib/api/models';
	import type { SegmentStyle } from './types';
	import { getSeverityColors } from './types';

	interface Props {
		duration: TimelineDuration;
		style: SegmentStyle;
		isAggregated?: boolean;
		onclick?: () => void;
	}

	let { duration, style, isAggregated = false, onclick }: Props = $props();

	const colors = $derived(getSeverityColors(duration.severity));

	const stripeOverlay: string =
		"repeating-linear-gradient(45deg, transparent, transparent 3px, rgba(0,0,0,0.15) 3px, rgba(0,0,0,0.15) 6px)";
</script>

{#if onclick}
	<button
		class="absolute h-full transition-all {colors.bg} border-0 p-0 {isAggregated ? 'cursor-zoom-in' : 'cursor-pointer'} hover:opacity-80 hover:scale-y-110"
		style="left: {style.left}%; width: {style.width}%;{isAggregated ? ` background-image: ${stripeOverlay};` : ''}"
		onclick={onclick}
		aria-label="{duration.severity} segment from {duration.startTime} to {duration.endTime}"
	></button>
{:else}
	<div
		class="absolute h-full transition-all {colors.bg}"
		style="left: {style.left}%; width: {style.width}%;"
	></div>
{/if}
