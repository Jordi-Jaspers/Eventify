<script lang="ts">
	import { Clock, Calendar, Radio, CheckCircle2 } from '@lucide/svelte';
	import { Label } from '$lib/components/ui/label';
	import { ToggleGroup, ToggleGroupItem } from '$lib/components/ui/toggle-group';
	import { DateTimePicker } from '$lib/components/ui/date-time-picker';
	import type { TimeRange } from '$lib/api/models';

	interface Props {
		timeRange: TimeRange;
		isLive: boolean;
		customStartTime: string;
		customEndTime: string;
		onTimeRangeChange: (range: TimeRange) => void;
		onCustomStartTimeChange: (startTime: string) => void;
		onCustomEndTimeChange: (endTime: string) => void;
	}

	let {
		timeRange,
		isLive,
		customStartTime,
		customEndTime,
		onTimeRangeChange,
		onCustomStartTimeChange,
		onCustomEndTimeChange
	}: Props = $props();

	const quickRanges: { value: TimeRange; label: string }[] = [
		{ value: '2h', label: '2h' },
		{ value: '4h', label: '4h' },
		{ value: '12h', label: '12h' },
		{ value: '24h', label: '24h' },
		{ value: '7d', label: '7d' },
		{ value: '30d', label: '30d' }
	];

	const isCustomRangeValid: boolean = $derived(
		timeRange !== 'custom' || (!!customStartTime && !!customEndTime)
	);

	function handleTimeRangeSelect(value: string | undefined): void {
		if (value && value !== 'custom') {
			onTimeRangeChange(value as TimeRange);
		}
	}

	function handleCustomRangeSelect(): void {
		onTimeRangeChange('custom');
	}
</script>

<div class="space-y-3">
	<div class="flex items-center gap-2">
		<Clock class="h-3.5 w-3.5 text-muted-foreground" />
		<Label class="text-xs font-semibold text-foreground/90">Time Range</Label>
	</div>

	<!-- Quick Ranges -->
	<ToggleGroup
		type="single"
		value={timeRange !== 'custom' ? timeRange : undefined}
		onValueChange={handleTimeRangeSelect}
		class="grid grid-cols-3 gap-1.5 p-1 rounded-lg bg-muted/20 border border-border/30"
	>
		{#each quickRanges as option (option.value)}
			<ToggleGroupItem
				value={option.value}
				class="data-[state=on]:bg-background data-[state=on]:text-primary data-[state=on]:shadow-sm data-[state=on]:border-primary/30 border border-transparent text-xs font-medium transition-all duration-200 hover:bg-background/50"
			>
				{option.label}
			</ToggleGroupItem>
		{/each}
	</ToggleGroup>

	<!-- Custom Range Toggle -->
	<button
		onclick={handleCustomRangeSelect}
		class="w-full flex items-center justify-between px-3 py-2.5 rounded-lg border transition-all duration-200 {timeRange === 'custom'
			? 'bg-background border-primary/50 text-foreground'
			: 'bg-background/30 border-border/30 hover:bg-background/50 hover:border-border/50 text-muted-foreground'}"
	>
		<div class="flex items-center gap-2">
			<Calendar class="h-3.5 w-3.5" />
			<span class="text-xs font-medium">Custom Range</span>
		</div>
		{#if timeRange === 'custom'}
			<CheckCircle2 class="h-3.5 w-3.5 text-primary" />
		{/if}
	</button>

	<!-- Custom DateTime Pickers -->
	{#if timeRange === 'custom'}
		<div class="space-y-3 pt-1 animate-in fade-in slide-in-from-top-2 duration-200">
			<DateTimePicker
				value={customStartTime}
				onValueChange={onCustomStartTimeChange}
				label="Start Date & Time"
				placeholder="Select start..."
				id="custom-start-time"
			/>
			<DateTimePicker
				value={customEndTime}
				onValueChange={onCustomEndTimeChange}
				label="End Date & Time"
				placeholder="Select end..."
				id="custom-end-time"
			/>
			{#if !isCustomRangeValid}
				<div class="flex items-start gap-2 px-3 py-2 rounded-lg bg-amber-500/10 border border-amber-500/30">
					<div class="mt-0.5">
						<div class="h-1 w-1 rounded-full bg-amber-500"></div>
					</div>
					<p class="text-xs text-amber-600 dark:text-amber-500 leading-relaxed">
						Select both start and end times to view historical data
					</p>
				</div>
			{/if}
		</div>
	{/if}

	<!-- Live Mode Info -->
	{#if isLive}
		<div class="flex items-start gap-2 px-3 py-2 rounded-lg bg-primary/5 border border-primary/20 animate-in fade-in duration-300">
			<Radio class="h-3.5 w-3.5 text-primary mt-0.5 flex-shrink-0" />
			<p class="text-xs text-primary/90 leading-relaxed">
				Live monitoring active • Auto-refresh every 60 seconds
			</p>
		</div>
	{/if}
</div>
