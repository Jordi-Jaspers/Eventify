<script lang="ts">
	import { Settings, Clock, Filter, Calendar } from '@lucide/svelte';
	import {
		Sheet,
		SheetContent,
		SheetHeader,
		SheetTitle,
		SheetTrigger
	} from '$lib/components/ui/sheet';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Separator } from '$lib/components/ui/separator';
	import { Input } from '$lib/components/ui/input';

	type TimeRange = '2h' | '4h' | '12h' | '24h' | '7d' | '30d' | 'custom';

	interface Props {
		timeRange: TimeRange;
		onlyCritical: boolean;
		sortBySeverity: boolean;
		groupedView: boolean;
		isLive: boolean;
		customStartTime: string;
		customEndTime: string;
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
		onTimeRangeChange,
		onToggleOnlyCritical,
		onToggleSortBySeverity,
		onToggleGroupedView,
		onCustomStartTimeChange,
		onCustomEndTimeChange
	}: Props = $props();

	let open: boolean = $state(false);

	const timeRangeOptions: { value: TimeRange; label: string }[] = [
		{ value: '2h', label: '2 hours' },
		{ value: '4h', label: '4 hours' },
		{ value: '12h', label: '12 hours' },
		{ value: '24h', label: '24 hours' },
		{ value: '7d', label: '7 days' },
		{ value: '30d', label: '30 days' }
	];

	function handleTimeRangeSelect(range: TimeRange): void {
		onTimeRangeChange(range);
		// Don't close the sheet to allow multiple config changes
	}

	// Convert ISO string to datetime-local format (YYYY-MM-DDTHH:mm)
	function isoToDatetimeLocal(iso: string): string {
		if (!iso) return '';
		try {
			const date = new Date(iso);
			// Format as local datetime
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const hours = String(date.getHours()).padStart(2, '0');
			const minutes = String(date.getMinutes()).padStart(2, '0');
			return `${year}-${month}-${day}T${hours}:${minutes}`;
		} catch {
			return '';
		}
	}

	// Convert datetime-local format to ISO string
	function datetimeLocalToIso(datetimeLocal: string): string {
		if (!datetimeLocal) return '';
		try {
			const date = new Date(datetimeLocal);
			return date.toISOString();
		} catch {
			return '';
		}
	}

	function handleStartTimeChange(event: Event): void {
		const input = event.target as HTMLInputElement;
		const iso = datetimeLocalToIso(input.value);
		onCustomStartTimeChange(iso);
	}

	function handleEndTimeChange(event: Event): void {
		const input = event.target as HTMLInputElement;
		const iso = datetimeLocalToIso(input.value);
		onCustomEndTimeChange(iso);
	}
</script>

<Sheet bind:open>
	<SheetTrigger>
		<Button variant="outline" class="gap-2 bg-background/50 border-border">
			<Settings class="h-4 w-4" />
			Configure
		</Button>
	</SheetTrigger>
	<SheetContent class="w-[400px]">
		<SheetHeader>
			<SheetTitle class="flex items-center gap-2">
				<Settings class="h-5 w-5 text-primary" />
				Monitor Configuration
			</SheetTitle>
		</SheetHeader>

		<div class="mt-6 space-y-6">
			<!-- Time Range Section -->
			<div class="space-y-3">
				<div class="flex items-center gap-2">
					<Clock class="h-4 w-4 text-muted-foreground" />
					<Label class="text-sm font-semibold">Time Range</Label>
				</div>
				<div class="grid grid-cols-3 gap-2">
					{#each timeRangeOptions as option (option.value)}
						<Button
							variant={timeRange === option.value ? 'default' : 'outline'}
							size="sm"
							onclick={() => handleTimeRangeSelect(option.value)}
							class={timeRange === option.value ? 'bg-gradient-to-r from-primary to-accent' : ''}
						>
							{option.label}
						</Button>
					{/each}
				</div>
				
				<!-- Custom Range Button -->
				<Button
					variant={timeRange === 'custom' ? 'default' : 'outline'}
					size="sm"
					onclick={() => handleTimeRangeSelect('custom')}
					class="w-full gap-2 {timeRange === 'custom' ? 'bg-gradient-to-r from-primary to-accent' : ''}"
				>
					<Calendar class="h-4 w-4" />
					Custom Range
				</Button>

				<!-- Custom DateTime Pickers -->
				{#if timeRange === 'custom'}
					<div class="space-y-3 pt-2">
						<div class="space-y-1.5">
							<Label for="custom-start-time" class="text-xs text-muted-foreground">Start Date & Time</Label>
							<Input
								id="custom-start-time"
								type="datetime-local"
								value={isoToDatetimeLocal(customStartTime)}
								onchange={handleStartTimeChange}
								class="bg-background/50"
							/>
						</div>
						<div class="space-y-1.5">
							<Label for="custom-end-time" class="text-xs text-muted-foreground">End Date & Time</Label>
							<Input
								id="custom-end-time"
								type="datetime-local"
								value={isoToDatetimeLocal(customEndTime)}
								onchange={handleEndTimeChange}
								class="bg-background/50"
							/>
						</div>
						{#if !customStartTime || !customEndTime}
							<p class="text-xs text-amber-500">
								Select both start and end times to view data
							</p>
						{/if}
					</div>
				{/if}

				{#if isLive}
					<p class="text-xs text-muted-foreground">
						Live mode - auto-refreshes every 60 seconds
					</p>
				{/if}
			</div>

			<Separator />

			<!-- Filter Options Section -->
			<div class="space-y-4">
				<div class="flex items-center gap-2">
					<Filter class="h-4 w-4 text-muted-foreground" />
					<Label class="text-sm font-semibold">Filters</Label>
				</div>

				<!-- Only Critical -->
				<div class="flex items-center gap-3">
					<Checkbox
						id="config-only-critical"
						checked={onlyCritical}
						onCheckedChange={onToggleOnlyCritical}
					/>
					<Label for="config-only-critical" class="text-sm font-normal cursor-pointer">
						Show only critical channels
					</Label>
				</div>

				<!-- Sort by Severity -->
				<div class="flex items-center gap-3">
					<Checkbox
						id="config-sort-severity"
						checked={sortBySeverity}
						onCheckedChange={onToggleSortBySeverity}
					/>
					<Label for="config-sort-severity" class="text-sm font-normal cursor-pointer">
						Sort by severity level
					</Label>
				</div>

				<!-- Grouped View -->
				<div class="flex items-center gap-3">
					<Checkbox
						id="config-grouped-view"
						checked={groupedView}
						onCheckedChange={onToggleGroupedView}
					/>
					<Label for="config-grouped-view" class="text-sm font-normal cursor-pointer">
						Show channels in groups
					</Label>
				</div>
			</div>
		</div>
	</SheetContent>
</Sheet>
