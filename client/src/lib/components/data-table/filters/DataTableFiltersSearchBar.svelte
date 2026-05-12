<script lang="ts" generics="T">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Input } from '$lib/components/ui/input';
	import { Search, X, Calendar, Tag, Filter, ChevronDown } from '@lucide/svelte';
	import type { DataTableColumn, FilterValue, DateRange } from '../types';
	import {
		TextFilter,
		EnumFilter,
		MultiEnumFilter,
		BooleanFilter,
		DateFilter,
		NumericFilter
	} from './index';

	interface Props<T> {
		columns: DataTableColumn<T>[];
		filters: Record<string, FilterValue>;
		onFilterChange: (fieldName: string, value: FilterValue) => void;
		onClearAll: () => void;
	}

	let { columns, filters, onFilterChange, onClearAll }: Props<T> = $props();

	// First text/fuzzy column = main search bar
	const primarySearch = $derived(
		columns.find(
			(col: DataTableColumn<T>) =>
				col.filterable && (col.filterType === 'TEXT' || col.filterType === 'FUZZY_TEXT')
		)
	);

	// All other filterable columns = filter buttons (including other text columns)
	const filterColumns = $derived(
		columns.filter(
			(col: DataTableColumn<T>) =>
				col.filterable && col.filterType && col !== primarySearch
		)
	);

	let openFilter: string | null = $state(null);
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;
	let popoverDebounceTimers: Record<string, ReturnType<typeof setTimeout>> = {};

	function handleSearchInput(event: Event): void {
		if (!primarySearch) return;
		const target = event.target as HTMLInputElement;
		const value = target.value;
		if (debounceTimer) clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			onFilterChange(primarySearch.key, value || null);
		}, 300);
	}

	function handlePopoverTextInput(key: string, event: Event): void {
		const target = event.target as HTMLInputElement;
		const value = target.value;
		if (popoverDebounceTimers[key]) clearTimeout(popoverDebounceTimers[key]);
		popoverDebounceTimers[key] = setTimeout(() => {
			onFilterChange(key, value || null);
		}, 300);
	}

	const hasActiveFilters: boolean = $derived(
		Object.keys(filters).some((key: string) => {
			const value: FilterValue = filters[key];
			if (value === null || value === undefined || value === '') return false;
			if (Array.isArray(value) && value.length === 0) return false;
			return true;
		})
	);

	// Active filter chips (only for filter buttons, not primary search)
	const activeChips = $derived(
		filterColumns.filter((col) => {
			const value = filters[col.key];
			if (value === null || value === undefined || value === '') return false;
			if (Array.isArray(value) && value.length === 0) return false;
			return true;
		})
	);

	function isFilterActive(key: string): boolean {
		const value = filters[key];
		if (value === null || value === undefined || value === '') return false;
		if (Array.isArray(value) && value.length === 0) return false;
		return true;
	}

	function getChipLabel(col: DataTableColumn<T>): string {
		const value = filters[col.key];
		if (col.filterType === 'ENUM' && col.filterOptions) {
			const opt = col.filterOptions.find((o) => o.value === value);
			return `${col.label}: ${opt?.label ?? value}`;
		}
		if (col.filterType === 'MULTI_ENUM' && Array.isArray(value)) {
			return `${col.label} (${value.length})`;
		}
		if (col.filterType === 'DATE') {
			const dr = value as DateRange;
			return `${dr.from ?? '…'} – ${dr.to ?? '…'}`;
		}
		if (typeof value === 'string' && value.length > 20) {
			return `${col.label}: ${value.slice(0, 20)}…`;
		}
		return `${col.label}: ${value}`;
	}

	function toggleFilter(key: string): void {
		openFilter = openFilter === key ? null : key;
	}
</script>

<div class="space-y-2">
	<!-- Main row: search bar + filter buttons -->
	<div class="flex items-center gap-2">
		<!-- Primary search input -->
		{#if primarySearch}
			<div class="relative flex-1">
				<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
				<Input
					type="text"
					placeholder={primarySearch.filterPlaceholder ?? `Search ${(primarySearch.label ?? primarySearch.key).toLowerCase()}...`}
					value={(filters[primarySearch.key] as string) ?? ''}
					oninput={handleSearchInput}
					class="pl-9 bg-background/50 border-border/50 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
				/>
			</div>
		{/if}

		<!-- Filter buttons -->
		{#each filterColumns as col}
			{@const active = isFilterActive(col.key)}
			<div class="relative">
				<button
					onclick={() => toggleFilter(col.key)}
					class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap
						{active
						? 'bg-primary/10 border-primary/30 text-primary'
						: openFilter === col.key
							? 'bg-background/50 border-primary text-primary'
							: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
				>
					{col.label ?? col.key}
					<ChevronDown class="h-3 w-3 transition-transform {openFilter === col.key ? 'rotate-180' : ''}" />
				</button>

				<!-- Popover -->
				{#if openFilter === col.key}
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						class="absolute top-full right-0 mt-1 z-50 min-w-[220px] rounded-lg border border-border/50 bg-card shadow-xl p-3"
						onmousedown={(e) => e.stopPropagation()}
					>
						{#if col.filterType === 'TEXT' || col.filterType === 'FUZZY_TEXT'}
							<input
								type="text"
								value={(filters[col.key] as string) ?? ''}
								oninput={(e) => handlePopoverTextInput(col.key, e)}
								placeholder={col.filterPlaceholder ?? `Filter by ${(col.label ?? col.key).toLowerCase()}...`}
								class="h-8 w-full rounded-md border border-border/50 bg-background/50 px-3 text-sm placeholder:text-muted-foreground/60 transition-colors focus:border-primary focus:outline-none"
							/>
						{:else if col.filterType === 'ENUM'}
							<EnumFilter
								value={(filters[col.key] as string | null) ?? null}
								options={col.filterOptions ?? []}
								onChange={(value: string | null) => { onFilterChange(col.key, value); openFilter = null; }}
								placeholder="All"
							/>
						{:else if col.filterType === 'MULTI_ENUM'}
							<MultiEnumFilter
								value={(filters[col.key] as string[]) ?? []}
								options={col.filterOptions ?? []}
								onChange={(value: string[]) => onFilterChange(col.key, value)}
							/>
						{:else if col.filterType === 'BOOLEAN'}
							<BooleanFilter
								value={(filters[col.key] as boolean | null) ?? null}
								onChange={(value: boolean | null) => { onFilterChange(col.key, value); openFilter = null; }}
							/>
						{:else if col.filterType === 'DATE'}
							<DateFilter
								value={(filters[col.key] as DateRange | null) ?? null}
								onChange={(value) => onFilterChange(col.key, value)}
							/>
						{:else if col.filterType === 'NUMERIC'}
							<NumericFilter
								value={(filters[col.key] as number | null) ?? null}
								onChange={(value: number | null) => onFilterChange(col.key, value)}
							/>
						{/if}
					</div>
				{/if}
			</div>
		{/each}

		<!-- Clear all -->
		{#if hasActiveFilters}
			<Button
				variant="ghost"
				size="sm"
				onclick={onClearAll}
				class="text-xs text-muted-foreground hover:text-destructive h-9 px-2"
			>
				<X class="h-3 w-3 mr-1" />
				Clear
			</Button>
		{/if}
	</div>

	<!-- Active filter chips -->
	{#if activeChips.length > 0}
		<div class="flex items-center gap-1.5 flex-wrap">
			{#each activeChips as col}
				<span class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md bg-primary/10 text-primary border border-primary/20">
					{getChipLabel(col)}
					<button onclick={() => onFilterChange(col.key, null)} class="hover:bg-primary/20 rounded-full p-0.5">
						<X class="h-2.5 w-2.5" />
					</button>
				</span>
			{/each}
		</div>
	{/if}
</div>

<!-- Click outside to close popover -->
{#if openFilter}
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div class="fixed inset-0 z-40" onclick={() => { openFilter = null; }}></div>
{/if}
