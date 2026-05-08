<script lang="ts">
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import { Filter } from '@lucide/svelte';
	import type { components } from '$lib/types/api';

	type TimeRange = NonNullable<components['schemas']['WatchlistFiltersRequest']['timeRange']>;

	interface Props {
		timeRange: TimeRange;
		onlyCritical: boolean;
		sortBySeverity: boolean;
		groupedView: boolean;
		onTimeRangeChange: (range: TimeRange) => void;
		onOnlyCriticalChange: (value: boolean) => void;
		onSortBySeverityChange: (value: boolean) => void;
		onGroupedViewChange: (value: boolean) => void;
	}

	let {
		timeRange = $bindable(),
		onlyCritical = $bindable(),
		sortBySeverity = $bindable(),
		groupedView = $bindable(),
		onTimeRangeChange,
		onOnlyCriticalChange,
		onSortBySeverityChange,
		onGroupedViewChange
	}: Props = $props();

	// Time ranges from OpenAPI enum
	const timeRanges: { value: TimeRange; label: string }[] = [
		{ value: '2h', label: '2h' },
		{ value: '4h', label: '4h' },
		{ value: '12h', label: '12h' },
		{ value: '24h', label: '24h' },
		{ value: '7d', label: '7d' },
		{ value: '30d', label: '30d' }
	];

	function handleTimeRangeClick(range: TimeRange): void {
		timeRange = range;
		onTimeRangeChange(range);
	}

	function handleOnlyCriticalChange(checked: boolean | 'indeterminate'): void {
		const value: boolean = checked === true;
		onlyCritical = value;
		onOnlyCriticalChange(value);
	}

	function handleSortBySeverityChange(checked: boolean | 'indeterminate'): void {
		const value: boolean = checked === true;
		sortBySeverity = value;
		onSortBySeverityChange(value);
	}

	function handleGroupedViewChange(checked: boolean | 'indeterminate'): void {
		const value: boolean = checked === true;
		groupedView = value;
		onGroupedViewChange(value);
	}
</script>

<Card class="bg-card/50 backdrop-blur-xl border-border/50 shadow-lg">
	<CardHeader>
		<CardTitle class="text-lg flex items-center gap-2">
			<Filter class="h-5 w-5 text-primary" />
			Default Filters
		</CardTitle>
	</CardHeader>
	<CardContent class="space-y-6">
		<!-- Time Range -->
		<div class="space-y-3">
			<Label class="text-sm font-medium">Time Range</Label>
			<div class="flex flex-wrap gap-2">
				{#each timeRanges as range (range.value)}
					<button
						onclick={() => handleTimeRangeClick(range.value)}
						class="
							px-3 py-1.5 rounded-lg border text-sm font-medium transition-all
							{timeRange === range.value
								? 'bg-primary text-primary-foreground border-primary shadow-sm'
								: 'bg-background border-border hover:border-primary/50 text-muted-foreground hover:text-foreground'}
						"
					>
						{range.label}
					</button>
				{/each}
			</div>
		</div>

		<!-- Checkboxes -->
		<div class="space-y-5 pt-4 border-t border-border/40">
			<div class="flex items-center space-x-3">
				<Checkbox
					id="only-critical"
					checked={onlyCritical}
					onCheckedChange={handleOnlyCriticalChange}
				/>
				<Label
					for="only-critical"
					class="text-sm font-normal cursor-pointer peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
				>
					Only critical events
				</Label>
			</div>

			<div class="flex items-center space-x-3">
				<Checkbox
					id="sort-severity"
					checked={sortBySeverity}
					onCheckedChange={handleSortBySeverityChange}
				/>
				<Label
					for="sort-severity"
					class="text-sm font-normal cursor-pointer peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
				>
					Sort by severity
				</Label>
			</div>

			<div class="flex items-center space-x-3">
				<Checkbox
					id="grouped-view"
					checked={groupedView}
					onCheckedChange={handleGroupedViewChange}
				/>
				<Label
					for="grouped-view"
					class="text-sm font-normal cursor-pointer peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
				>
					Show grouped view
				</Label>
			</div>
		</div>
	</CardContent>
</Card>
