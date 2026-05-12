<script lang="ts" generics="T">
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import Button from '$lib/components/ui/button/button.svelte';
	import { CircleAlert } from '@lucide/svelte';
	import type { Snippet, Component } from 'svelte';
	import type { DataTableColumn, DataTableService } from './types';
	import DataTableHeader from './DataTableHeader.svelte';
	import DataTableFiltersSearchBar from './filters/DataTableFiltersSearchBar.svelte';
	import DataTablePagination from './DataTablePagination.svelte';
	import DataTableSkeleton from './DataTableSkeleton.svelte';
	import DataTableEmpty from './DataTableEmpty.svelte';

	interface Props {
		columns: DataTableColumn<T>[];
		service: DataTableService<T>;
		row: Snippet<[T]>;
		empty?: Snippet;
		headerActions?: Snippet;
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
		headerActions,
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

<!-- Filters -->
{#if hasFilterableColumns}
	<div class="relative z-10 rounded-xl border border-border/50 bg-card/50 backdrop-blur-xl shadow-lg mb-6 px-4 py-3">
		<DataTableFiltersSearchBar
			{columns}
			filters={service.filters}
			onFilterChange={service.setFilter}
			onClearAll={service.reset}
		/>
	</div>
{/if}

<!-- Main Table Card -->
<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
	{#if title || description}
		<CardHeader>
			<div class="flex items-center justify-between">
				<div class="flex-1">
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
				</div>
				{#if headerActions}
					<div class="flex items-center gap-2">
						{@render headerActions()}
					</div>
				{/if}
			</div>
		</CardHeader>
	{/if}

	<CardContent>
		{#if service.loading}
			<DataTableSkeleton rows={skeletonRows} columns={totalCols} />
		{:else if service.items.length === 0}
			{#if empty}
				{@render empty()}
			{:else}
				<DataTableEmpty />
			{/if}
		{:else}
			<div class="rounded-lg border border-border/50 overflow-hidden">
				<DataTableHeader
					{columns}
					currentSortKey={service.sortKey}
					currentSortDirection={service.sortDirection}
					onSort={service.setSort}
				/>
				<div class="divide-y divide-border/30">
					{#each service.items as item (item)}
						{@render row(item)}
					{/each}
				</div>
			</div>

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
