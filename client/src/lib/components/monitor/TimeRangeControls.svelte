<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { Calendar, Clock } from '@lucide/svelte';
	import type { TimeRange } from '$lib/api/models';

	interface Props {
		selectedRange: TimeRange;
		onRangeChange: (range: TimeRange) => void;
		disabled?: boolean;
	}

	let { selectedRange, onRangeChange, disabled = false }: Props = $props();

	const presetRanges: { value: TimeRange; label: string }[] = [
		{ value: '2h', label: '2h' },
		{ value: '4h', label: '4h' },
		{ value: '12h', label: '12h' },
		{ value: '24h', label: '24h' },
		{ value: '7d', label: '7d' },
		{ value: '30d', label: '30d' }
	];
</script>

<div class="flex flex-wrap items-center gap-2">
	<Clock class="h-4 w-4 text-muted-foreground" />
	<span class="text-sm text-muted-foreground mr-2">Time Range:</span>

	{#each presetRanges as range (range.value)}
		<Button
			variant={selectedRange === range.value ? 'default' : 'outline'}
			size="sm"
			onclick={() => onRangeChange(range.value)}
			disabled={disabled}
			class={selectedRange === range.value
				? 'bg-gradient-to-r from-primary to-accent'
				: ''}
		>
			{range.label}
		</Button>
	{/each}

	<!-- Custom date range - placeholder for future implementation -->
	<Button
		variant={selectedRange === 'custom' ? 'default' : 'outline'}
		size="sm"
		disabled
		title="Custom date range coming soon"
		class="gap-2"
	>
		<Calendar class="h-3 w-3" />
		Custom
	</Button>
</div>
