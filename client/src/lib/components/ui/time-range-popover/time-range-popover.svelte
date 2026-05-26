<script lang="ts">
	import { Popover, PopoverContent, PopoverTrigger } from '$lib/components/ui/popover';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { ToggleGroup, ToggleGroupItem } from '$lib/components/ui/toggle-group';
	import { DateTimePicker } from '$lib/components/ui/date-time-picker';
	import { Calendar, Clock, CheckCircle2 } from '@lucide/svelte';

	const DEFAULT_QUICK_RANGES: { value: string; label: string }[] = [
		{ value: '7', label: '7d' },
		{ value: '30', label: '30d' },
		{ value: '90', label: '90d' },
		{ value: '180', label: '180d' }
	];

	interface Props {
		quickRanges?: { value: string; label: string }[];
		selectedDays: string;
		isCustomRange: boolean;
		customStart: string;
		customEnd: string;
		onQuickRangeSelect: (days: string) => void;
		onCustomRangeApply: (start: string, end: string) => void;
		onCustomRangeToggle: () => void;
	}

	let {
		quickRanges = DEFAULT_QUICK_RANGES,
		selectedDays,
		isCustomRange,
		customStart,
		customEnd,
		onQuickRangeSelect,
		onCustomRangeApply,
		onCustomRangeToggle
	}: Props = $props();

	let popoverOpen: boolean = $state(false);
	let localStart: string = $state('');
	let localEnd: string = $state('');

	$effect(() => {
		if (popoverOpen) {
			localStart = customStart;
			localEnd = customEnd;
		}
	});

	const triggerLabel: string = $derived(
		isCustomRange && customStart && customEnd
			? `${new Date(customStart).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} – ${new Date(customEnd).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`
			: `Last ${selectedDays} days`
	);

	function handleQuickRange(value: string | undefined): void {
		if (!value) return;
		onQuickRangeSelect(value);
		popoverOpen = false;
	}

	function handleApply(): void {
		if (!localStart || !localEnd) return;
		onCustomRangeApply(localStart, localEnd);
		popoverOpen = false;
	}
</script>

<Popover bind:open={popoverOpen}>
	<PopoverTrigger>
		{#snippet child({ props }: { props: Record<string, unknown> })}
			<button
				{...props}
				class="inline-flex items-center gap-2 px-3.5 py-2 text-sm font-medium rounded-lg border border-border/40 bg-background/80 backdrop-blur-sm shadow-sm hover:shadow-md hover:border-primary/30 hover:bg-background transition-all duration-200 text-foreground/80 hover:text-foreground"
			>
				<Calendar class="h-3.5 w-3.5 text-primary/70" />
				<span>{triggerLabel}</span>
				<svg class="h-3 w-3 text-muted-foreground/60" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" /></svg>
			</button>
		{/snippet}
	</PopoverTrigger>
	<PopoverContent
		class="w-[320px] p-0 border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl"
		align="end"
		sideOffset={8}
	>
		<div class="px-5 py-4 border-b border-border/30">
			<div class="flex items-center gap-2">
				<Clock class="h-4 w-4 text-primary" />
				<h3 class="font-semibold text-sm">Time Range</h3>
			</div>
		</div>

		<div class="p-5 space-y-4">
			<!-- Quick Ranges -->
			<div class="space-y-2">
				<Label class="text-xs font-semibold text-foreground/90">Quick Ranges</Label>
				<ToggleGroup
					type="single"
					value={!isCustomRange ? selectedDays : undefined}
					onValueChange={handleQuickRange}
					class="grid grid-cols-4 gap-1 p-1 rounded-lg bg-muted/20 border border-border/30"
				>
					{#each quickRanges as opt (opt.value)}
						<ToggleGroupItem
							value={opt.value}
							class="data-[state=on]:bg-background data-[state=on]:text-primary data-[state=on]:shadow-sm data-[state=on]:border-primary/30 border border-transparent text-xs font-medium transition-all duration-200 hover:bg-background/50 rounded-md"
						>
							{opt.label}
						</ToggleGroupItem>
					{/each}
				</ToggleGroup>
			</div>

			<!-- Custom Range -->
			<div class="space-y-2">
				<button
					onclick={onCustomRangeToggle}
					class="w-full flex items-center justify-between px-3 py-2.5 rounded-lg border transition-all duration-200 {isCustomRange
						? 'bg-background border-primary/50 text-foreground'
						: 'bg-background/30 border-border/30 hover:bg-background/50 hover:border-border/50 text-muted-foreground'}"
				>
					<div class="flex items-center gap-2">
						<Calendar class="h-3.5 w-3.5" />
						<span class="text-xs font-medium">Custom Range</span>
					</div>
					{#if isCustomRange}
						<CheckCircle2 class="h-3.5 w-3.5 text-primary" />
					{/if}
				</button>

				{#if isCustomRange}
					<div class="space-y-3 pt-1 animate-in fade-in slide-in-from-top-2 duration-200">
						<DateTimePicker
							value={localStart}
							onValueChange={(v: string) => { localStart = v; }}
							label="Start Date & Time"
							placeholder="Select start..."
							id="time-range-custom-start"
						/>
						<DateTimePicker
							value={localEnd}
							onValueChange={(v: string) => { localEnd = v; }}
							label="End Date & Time"
							placeholder="Select end..."
							id="time-range-custom-end"
						/>
						<Button
							size="sm"
							class="w-full"
							disabled={!localStart || !localEnd}
							onclick={handleApply}
						>
							Apply Range
						</Button>
					</div>
				{/if}
			</div>
		</div>
	</PopoverContent>
</Popover>
