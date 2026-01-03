<script lang="ts" generics="T">
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import Button from '$lib/components/ui/button/button.svelte';
	import { CircleAlert } from '@lucide/svelte';
	import type { Snippet, Component } from 'svelte';
	import type { DataTableColumn, DataTableService } from './types';
	import DataTableHeader from './DataTableHeader.svelte';
	import DataTableFilters from './DataTableFilters.svelte';
	import DataTablePagination from './DataTablePagination.svelte';
	import DataTableSkeleton from './DataTableSkeleton.svelte';
	import DataTableEmpty from './DataTableEmpty.svelte';

	interface Props {
		columns: DataTableColumn<T>[];
		service: DataTableService<T>;
		row: Snippet<[T]>;
		empty?: Snippet;
		skeletonRows?: number;
		title?: string;
		description?: string;
		icon?: Component;
	}

	let {
		columns,
		service,
		row,
		empty,
		skeletonRows = 5,
		title,
		description,
		icon
	}: Props = $props();

	const totalCols: number = $derived(
		columns.reduce((sum: number, col: DataTableColumn<T>) => sum + (col.colSpan ?? 1), 0)
	);

	const hasFilterableColumns: boolean = $derived(
		columns.some((col: DataTableColumn<T>) => col.filterable && col.filterType)
	);
</script>

<!-- Error Alert -->
{#if service.error && !service.loading}
	<Alert variant="destructive" class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm">
		<CircleAlert class="h-4 w-4" />
		<AlertDescription>
			{service.error}
			<Button variant="outline" size="sm" class="ml-4" onclick={service.refresh}>
				Retry
			</Button>
		</AlertDescription>
	</Alert>
{/if}

<!-- Filters Card -->
{#if hasFilterableColumns}
	<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl mb-6">
		<CardContent class="pt-6">
			<DataTableFilters
				{columns}
				filters={service.filters}
				onFilterChange={service.setFilter}
				onClearAll={service.clearAllFilters}
			/>
		</CardContent>
	</Card>
{/if}

<!-- Main Table Card -->
<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
	{#if title || description}
		<CardHeader>
			{#if title}
				<div class="flex items-center gap-2">
					{#if icon}
						{@const IconComponent = icon}
						<IconComponent class="w-5 h-5 text-primary" />
					{/if}
					<CardTitle class="text-xl">{title}</CardTitle>
				</div>
			{/if}
			{#if description}
				<CardDescription>{description}</CardDescription>
			{:else if !service.loading}
				<CardDescription>{service.showingRange}</CardDescription>
			{/if}
		</CardHeader>
	{/if}

	<CardContent>
		{#if service.loading}
			<!-- Loading Skeleton -->
			<DataTableSkeleton rows={skeletonRows} columns={totalCols} />
		{:else if service.items.length === 0}
			<!-- Empty State -->
			{#if empty}
				{@render empty()}
			{:else}
				<DataTableEmpty />
			{/if}
		{:else}
			<!-- Data Table -->
			<div class="space-y-2">
				<!-- Table Header -->
				<DataTableHeader
					{columns}
					currentSortKey={service.sortKey}
					currentSortDirection={service.sortDirection}
					onSort={service.setSort}
				/>

				<!-- Table Rows -->
				{#each service.items as item (item)}
					{@render row(item)}
				{/each}
			</div>

			<!-- Pagination -->
			<DataTablePagination
				currentPage={service.currentPage}
				totalPages={service.totalPages}
				showingRange={service.showingRange}
				hasPreviousPage={service.hasPreviousPage}
				hasNextPage={service.hasNextPage}
				onPreviousPage={service.previousPage}
				onNextPage={service.nextPage}
			/>
		{/if}
	</CardContent>
</Card>
