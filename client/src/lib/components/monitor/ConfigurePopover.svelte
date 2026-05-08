<script lang="ts">
	import { Settings, Clock, Filter, Calendar, Radio, CheckCircle2, RotateCcw } from '@lucide/svelte';
	import {
		Popover,
		PopoverContent,
		PopoverTrigger
	} from '$lib/components/ui/popover';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Switch } from '$lib/components/ui/switch';
	import { Separator } from '$lib/components/ui/separator';
	import { ToggleGroup, ToggleGroupItem } from '$lib/components/ui/toggle-group';
	import { DateTimePicker } from '$lib/components/ui/date-time-picker';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';
	import type { TimeRange } from '$lib/api/models';

	interface Props {
		timeRange: TimeRange;
		onlyCritical: boolean;
		sortBySeverity: boolean;
		groupedView: boolean;
		isLive: boolean;
		customStartTime: string;
		customEndTime: string;
		showResetButton?: boolean;
		showModifiedIndicator?: boolean;
		onResetToDefaults?: () => void;
		onTimeRangeChange: (range: TimeRange) => void;
		onToggleOnlyCritical: () => void;
		onToggleSortBySeverity: () => void;
		onToggleGroupedView: () => void;
		onCustomStartTimeChange: (startTime: string) => void;
		onCustomEndTimeChange: (endTime: string) => void;
	}

	let {
		timeRange,
		onlyCritical,
		sortBySeverity,
		groupedView,
		isLive,
		customStartTime,
		customEndTime,
		showResetButton = false,
		showModifiedIndicator = false,
		onResetToDefaults,
		onTimeRangeChange,
		onToggleOnlyCritical,
		onToggleSortBySeverity,
		onToggleGroupedView,
		onCustomStartTimeChange,
		onCustomEndTimeChange
	}: Props = $props();

	let open: boolean = $state(false);

	const quickRanges: { value: TimeRange; label: string }[] = [
		{ value: '2h', label: '2h' },
		{ value: '4h', label: '4h' },
		{ value: '12h', label: '12h' },
		{ value: '24h', label: '24h' },
		{ value: '7d', label: '7d' },
		{ value: '30d', label: '30d' }
	];

	function handleTimeRangeSelect(value: string | undefined): void {
		if (value && value !== 'custom') {
			onTimeRangeChange(value as TimeRange);
		}
	}

	function handleCustomRangeSelect(): void {
		onTimeRangeChange('custom');
	}

	// Derived state for better UX
	const isCustomRangeValid: boolean = $derived(
		timeRange !== 'custom' || (!!customStartTime && !!customEndTime)
	);
</script>

<Popover bind:open>
	<PopoverTrigger>
		{#snippet child({ props }: { props: Record<string, any> })}
			<Button
				{...props}
				variant="outline"
				class="relative gap-2 border-border/50 bg-background/30 backdrop-blur-sm hover:bg-background/50 hover:border-primary/50 transition-all duration-200"
			>
				<Settings class="h-4 w-4" />
				<span class="hidden sm:inline">Configure</span>
				<span class="sm:hidden">Config</span>
				{#if showModifiedIndicator}
					<span class="flex h-2 w-2 absolute -top-1 -right-1">
						<span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
					</span>
				{/if}
			</Button>
		{/snippet}
	</PopoverTrigger>
	<PopoverContent
		class="w-[380px] p-0 border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl"
		align="end"
		sideOffset={8}
	>
		<!-- Header -->
		<div class="px-6 py-4 border-b border-border/30">
			<div class="flex items-center justify-between">
				<div class="flex items-center gap-2.5">
					<div
						class="p-2 rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30"
					>
						<Settings class="h-4 w-4 text-primary" />
					</div>
					<div>
						<h3 class="font-semibold text-sm">Monitor Configuration</h3>
						<p class="text-xs text-muted-foreground">Customize your view</p>
					</div>
				</div>
				{#if isLive}
					<div
						class="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary/10 border border-primary/30"
					>
						<PulseIndicator variant="primary" size="sm" label="Live" />
					</div>
				{/if}
			</div>
		</div>

		<div class="p-6 space-y-6">
			<!-- Time Range Section -->
			<div class="space-y-3">
				<div class="flex items-center gap-2">
					<Clock class="h-3.5 w-3.5 text-muted-foreground" />
					<Label class="text-xs font-semibold text-foreground/90">Time Range</Label>
				</div>

				<!-- Quick Ranges - Segmented Control Style -->
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
					class="w-full flex items-center justify-between px-3 py-2.5 rounded-lg border transition-all duration-200 {timeRange ===
					'custom'
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
							<div
								class="flex items-start gap-2 px-3 py-2 rounded-lg bg-amber-500/10 border border-amber-500/30"
							>
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
					<div
						class="flex items-start gap-2 px-3 py-2 rounded-lg bg-primary/5 border border-primary/20 animate-in fade-in duration-300"
					>
						<Radio class="h-3.5 w-3.5 text-primary mt-0.5 flex-shrink-0" />
						<p class="text-xs text-primary/90 leading-relaxed">
							Live monitoring active • Auto-refresh every 60 seconds
						</p>
					</div>
				{/if}
			</div>

			<Separator class="bg-border/30" />

			<!-- Filter Options Section -->
			<div class="space-y-4">
				<div class="flex items-center gap-2">
					<Filter class="h-3.5 w-3.5 text-muted-foreground" />
					<Label class="text-xs font-semibold text-foreground/90">Display Filters</Label>
				</div>

				<!-- Filter Toggles -->
				<div class="space-y-3">
					<!-- Only Critical -->
					<div
						class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group"
					>
						<div class="flex-1">
							<Label
								for="toggle-critical"
								class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors"
							>
								Critical channels only
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">
								Show high-priority channels
							</p>
						</div>
						<Switch
							id="toggle-critical"
							checked={onlyCritical}
							onCheckedChange={onToggleOnlyCritical}
							class="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-primary data-[state=checked]:to-accent"
						/>
					</div>

					<!-- Sort by Severity -->
					<div
						class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group"
					>
						<div class="flex-1">
							<Label
								for="toggle-severity"
								class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors"
							>
								Sort by severity
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">
								Order by threat level
							</p>
						</div>
						<Switch
							id="toggle-severity"
							checked={sortBySeverity}
							onCheckedChange={onToggleSortBySeverity}
							class="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-primary data-[state=checked]:to-accent"
						/>
					</div>

					<!-- Grouped View -->
					<div
						class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group"
					>
						<div class="flex-1">
							<Label
								for="toggle-grouped"
								class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors"
							>
								Grouped view
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">
								Organize by categories
							</p>
						</div>
						<Switch
							id="toggle-grouped"
							checked={groupedView}
							onCheckedChange={onToggleGroupedView}
							class="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-primary data-[state=checked]:to-accent"
						/>
					</div>
				</div>
			</div>
		</div>

		<!-- Reset to Defaults Button -->
		{#if showResetButton && onResetToDefaults}
			<div class="px-6 pb-4">
				<Separator class="bg-border/30 mb-4" />
				<Button
					variant="ghost"
					size="sm"
					onclick={onResetToDefaults}
					class="w-full justify-start gap-2"
				>
					<RotateCcw class="h-4 w-4" />
					Reset to defaults
				</Button>
			</div>
		{/if}

		<!-- Footer hint -->
		<div class="px-6 py-3 border-t border-border/30 bg-muted/5">
			<p class="text-[10px] text-muted-foreground text-center">
				Changes apply instantly to your monitor view
			</p>
		</div>
	</PopoverContent>
</Popover>
