<script lang="ts">
	import { Settings, Filter, RotateCcw } from '@lucide/svelte';
	import {
		Popover,
		PopoverContent,
		PopoverTrigger
	} from '$lib/components/ui/popover';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Switch } from '$lib/components/ui/switch';
	import { Separator } from '$lib/components/ui/separator';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';
	import MonitorTimeRangeSection from './MonitorTimeRangeSection.svelte';
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
</script>

<Popover bind:open>
	<PopoverTrigger>
		{#snippet child({ props }: { props: Record<string, any> })}
			<Button
				{...props}
				variant="default"
				size="sm"
				class="relative gap-2"
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
					<div class="p-2 rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30">
						<Settings class="h-4 w-4 text-primary" />
					</div>
					<div>
						<h3 class="font-semibold text-sm">Monitor Configuration</h3>
						<p class="text-xs text-muted-foreground">Customize your view</p>
					</div>
				</div>
				{#if isLive}
					<div class="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary/10 border border-primary/30">
						<PulseIndicator variant="primary" size="sm" label="Live" />
					</div>
				{/if}
			</div>
		</div>

		<div class="p-6 space-y-6">
			<!-- Time Range Section -->
			<MonitorTimeRangeSection
				{timeRange}
				{isLive}
				{customStartTime}
				{customEndTime}
				{onTimeRangeChange}
				{onCustomStartTimeChange}
				{onCustomEndTimeChange}
			/>

			<Separator class="bg-border/30" />

			<!-- Filter Options Section -->
			<div class="space-y-4">
				<div class="flex items-center gap-2">
					<Filter class="h-3.5 w-3.5 text-muted-foreground" />
					<Label class="text-xs font-semibold text-foreground/90">Display Filters</Label>
				</div>

				<div class="space-y-3">
					<!-- Only Critical -->
					<div class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group">
						<div class="flex-1">
							<Label for="toggle-critical" class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors">
								Critical channels only
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">Show high-priority channels</p>
						</div>
						<Switch
							id="toggle-critical"
							checked={onlyCritical}
							onCheckedChange={onToggleOnlyCritical}
							class="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-primary data-[state=checked]:to-accent"
						/>
					</div>

					<!-- Sort by Severity -->
					<div class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group">
						<div class="flex-1">
							<Label for="toggle-severity" class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors">
								Sort by severity
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">Order by threat level</p>
						</div>
						<Switch
							id="toggle-severity"
							checked={sortBySeverity}
							onCheckedChange={onToggleSortBySeverity}
							class="data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-primary data-[state=checked]:to-accent"
						/>
					</div>

					<!-- Grouped View -->
					<div class="flex items-center justify-between px-3 py-2.5 rounded-lg border border-border/30 bg-background/30 hover:bg-background/50 transition-all duration-200 group">
						<div class="flex-1">
							<Label for="toggle-grouped" class="text-xs font-medium cursor-pointer group-hover:text-foreground transition-colors">
								Grouped view
							</Label>
							<p class="text-[10px] text-muted-foreground mt-0.5">Organize by categories</p>
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
