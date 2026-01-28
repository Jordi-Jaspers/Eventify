<script lang="ts">
	import { CircleCheckBig, CircleX } from '@lucide/svelte';

	interface Props {
		status: boolean | undefined;
		activeLabel: string;
		inactiveLabel: string;
		activeColor?: string;
		inactiveColor?: string;
	}

	let {
		status,
		activeLabel,
		inactiveLabel,
		activeColor = 'text-green-500',
		inactiveColor = 'text-red-500'
	}: Props = $props();
</script>

<div class="flex items-center gap-2">
	{#if status}
		<CircleCheckBig class="w-4 h-4 {activeColor}" />
		<span class="text-sm font-medium {activeColor}">{activeLabel}</span>
	{:else}
		<CircleX class="w-4 h-4 {inactiveColor}" />
		<span class="text-sm font-medium {inactiveColor}">{inactiveLabel}</span>
	{/if}
</div>
