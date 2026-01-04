<script lang="ts" generics="T">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Filter, X } from '@lucide/svelte';
	import type { DataTableColumn, FilterValue } from './types';
	import {
		TextFilter,
		EnumFilter,
		MultiEnumFilter,
		BooleanFilter,
		NumericFilter,
		DateFilter
	} from './filters';

	interface Props<T> {
		columns: DataTableColumn<T>[];
		filters: Record<string, FilterValue>;
		onFilterChange: (fieldName: string, value: FilterValue) => void;
		onClearAll: () => void;
	}

	let { columns, filters, onFilterChange, onClearAll }: Props<T> = $props();

	// Separate text/fuzzy filters (search boxes) from other filters (pills/selects)
	const textFilters = $derived(
		columns.filter(
			(col: DataTableColumn<T>) =>
				col.filterable && (col.filterType === 'TEXT' || col.filterType === 'FUZZY_TEXT')
		)
	);

	const pillFilters = $derived(
		columns.filter(
			(col: DataTableColumn<T>) =>
				col.filterable &&
				(col.filterType === 'ENUM' ||
					col.filterType === 'MULTI_ENUM' ||
					col.filterType === 'BOOLEAN')
		)
	);

	const otherFilters = $derived(
		columns.filter(
			(col: DataTableColumn<T>) =>
				col.filterable && (col.filterType === 'NUMERIC' || col.filterType === 'DATE')
		)
	);

	const hasActiveFilters: boolean = $derived(
		Object.keys(filters).some((key: string) => {
			const value: FilterValue = filters[key];
			if (value === null || value === undefined || value === '') return false;
			if (Array.isArray(value) && value.length === 0) return false;
			return true;
		})
	);

	const hasFilters: boolean = $derived(
		textFilters.length > 0 || pillFilters.length > 0 || otherFilters.length > 0
	);
</script>

{#if hasFilters}
	<div class="flex flex-col gap-4">
		<!-- Row 1: Text search + Pill filters -->
		<div class="flex flex-col md:flex-row gap-4">
			<!-- Text/Search filters (expandable) -->
			{#each textFilters as column}
				{@const filterValue = filters[column.key] ?? ''}
				<div class="flex-1 min-w-0">
					{#if column.filterType === 'TEXT'}
						<TextFilter
							value={filterValue as string}
							onChange={(value: string) => onFilterChange(column.key, value)}
							placeholder={column.filterPlaceholder ?? `Filter by ${column.label.toLowerCase()}...`}
							debounce={false}
						/>
					{:else if column.filterType === 'FUZZY_TEXT'}
						<TextFilter
							value={filterValue as string}
							onChange={(value: string) => onFilterChange(column.key, value)}
							placeholder={column.filterPlaceholder ?? `Search ${column.label.toLowerCase()}...`}
							debounce={true}
						/>
					{/if}
				</div>
			{/each}

			<!-- Pill filters (inline) -->
			{#each pillFilters as column}
				{@const filterValue = filters[column.key] ?? null}
				<div class="flex items-center gap-2 flex-shrink-0">
					<Filter class="h-4 w-4 text-muted-foreground hidden md:block" />
					{#if column.filterType === 'ENUM'}
						<EnumFilter
							value={filterValue as string | null}
							options={column.filterOptions ?? []}
							onChange={(value: string | null) => onFilterChange(column.key, value)}
							placeholder={column.filterPlaceholder ?? 'All'}
						/>
					{:else if column.filterType === 'MULTI_ENUM'}
						<MultiEnumFilter
							value={(filterValue as string[]) ?? []}
							options={column.filterOptions ?? []}
							onChange={(value: string[]) => onFilterChange(column.key, value)}
						/>
					{:else if column.filterType === 'BOOLEAN'}
						<BooleanFilter
							value={filterValue as boolean | null}
							onChange={(value: boolean | null) => onFilterChange(column.key, value)}
						/>
					{/if}
				</div>
			{/each}

			<!-- Clear All button -->
			{#if hasActiveFilters}
				<div class="flex items-center flex-shrink-0">
					<Button
						variant="ghost"
						size="sm"
						onclick={onClearAll}
						class="text-xs text-muted-foreground hover:text-destructive"
					>
						<X class="h-3 w-3 mr-1" />
						Clear
					</Button>
				</div>
			{/if}
		</div>

		<!-- Row 2: Other filters (Numeric, Date) if present -->
		{#if otherFilters.length > 0}
			<div class="flex flex-col md:flex-row gap-4">
				{#each otherFilters as column}
					{@const filterValue = filters[column.key] ?? null}
					<div class="flex items-center gap-2">
						<span class="text-sm text-muted-foreground whitespace-nowrap">{column.label}:</span>
						{#if column.filterType === 'NUMERIC'}
							<NumericFilter
								value={filterValue as number | null}
								onChange={(value: number | null) => onFilterChange(column.key, value)}
								placeholder={column.filterPlaceholder ?? 'Enter number...'}
							/>
						{:else if column.filterType === 'DATE'}
							<DateFilter
								value={filterValue as { from?: string; to?: string } | null}
								onChange={(value) => onFilterChange(column.key, value)}
								placeholder={column.filterPlaceholder ?? 'Select date range'}
							/>
						{/if}
					</div>
				{/each}
			</div>
		{/if}
	</div>
{/if}
