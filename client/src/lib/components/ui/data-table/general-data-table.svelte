<script lang="ts" generics="TData, TValue">
	import {
		type ColumnDef,
		type ColumnFiltersState,
		getCoreRowModel,
		getFilteredRowModel,
		getPaginationRowModel,
		getSortedRowModel,
		type PaginationState,
		type SortingState
	} from '@tanstack/table-core';
	import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../table';
	import { createSvelteTable, FlexRender } from './index.ts';
	import { Button } from '../button';
	import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-svelte';
	import { Input } from '../input';
	import type { Snippet } from 'svelte';

	type DataTableProps<TData, TValue> = {
		columns: ColumnDef<TData, TValue>[];
		data: TData[];
		children?: Snippet;
	};

	let { data, columns, children }: DataTableProps<TData, TValue> = $props();
	let sorting = $state<SortingState>([]);
	let columnFilters = $state<ColumnFiltersState>([]);
	let pagination: PaginationState = $state({ pageIndex: 0, pageSize: 25 });
	const pageCount: number = $derived(Math.ceil(data.length / pagination.pageSize));

	const table = createSvelteTable({
		get data() {
			return data;
		},
		columns,
		getCoreRowModel: getCoreRowModel(),
		getPaginationRowModel: getPaginationRowModel(),
		getSortedRowModel: getSortedRowModel(),
		getFilteredRowModel: getFilteredRowModel(),
		onPaginationChange: (updater) => {
			if (typeof updater === 'function') {
				pagination = updater(pagination);
			} else {
				pagination = updater;
			}
		},
		onSortingChange: (updater) => {
			if (typeof updater === 'function') {
				sorting = updater(sorting);
			} else {
				sorting = updater;
			}
		},
		onColumnFiltersChange: (updater) => {
			if (typeof updater === 'function') {
				columnFilters = updater(columnFilters);
			} else {
				columnFilters = updater;
			}
		},
		state: {
			get pagination() {
				return pagination;
			},
			get sorting() {
				return sorting;
			},
			get columnFilters() {
				return columnFilters;
			}
		}
	});
</script>

<div>
	<div class="flex items-center justify-between py-4">
		<Input
			placeholder="Search..."
			value={table.getColumn('name')?.getFilterValue() ?? ''}
			onchange={(e) => table.getColumn('name')?.setFilterValue(e.currentTarget.value)}
			oninput={(e) => table.getColumn('name')?.setFilterValue(e.currentTarget.value)}
			class="max-w-sm"
		/>
		{#if children}
			{@render children()}
		{/if}
	</div>
	<div class="rounded-md border">
		<Table>
			<TableHeader>
				{#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
					<TableRow>
						{#each headerGroup.headers as header (header.id)}
							<TableHead class={header.column.columnDef.meta?.headerClassName}>
								{#if !header.isPlaceholder}
									<FlexRender content={header.column.columnDef.header} context={header.getContext()} />
								{/if}
							</TableHead>
						{/each}
					</TableRow>
				{/each}
			</TableHeader>
			<TableBody>
				{#each table.getRowModel().rows as row (row.id)}
					<TableRow data-state={row.getIsSelected() && 'selected'}>
						{#each row.getVisibleCells() as cell (cell.id)}
							<TableCell class={cell.column.columnDef.meta?.cellClassName}>
								<FlexRender content={cell.column.columnDef.cell} context={cell.getContext()} />
							</TableCell>
						{/each}
					</TableRow>
				{:else}
					<TableRow>
						<TableCell colspan={columns.length} class="h-24 text-center">No results.</TableCell>
					</TableRow>
				{/each}
			</TableBody>
		</Table>
	</div>

	<div class="flex items-center justify-between p-2">
		<div class="flex items-center justify-center text-sm text-muted-foreground">
			Page {pagination.pageIndex + 1} of {pageCount}
		</div>
		<div class="flex items-center space-x-2">
			<Button variant="outline" class="hidden h-8 w-8 p-0 lg:flex" disabled={!table.getCanPreviousPage()} onclick={() => table.firstPage()}>
				<span class="sr-only">Go to first page</span>
				<ChevronsLeft size={15} />
			</Button>
			<Button variant="outline" class="h-8 w-8 p-0" disabled={!table.getCanPreviousPage()} onclick={() => table.previousPage()}>
				<span class="sr-only">Go to previous page</span>
				<ChevronLeft size={15} />
			</Button>
			<Button variant="outline" class="h-8 w-8 p-0" onclick={() => table.nextPage()} disabled={!table.getCanNextPage()}>
				<span class="sr-only">Go to next page</span>
				<ChevronRight size={15} />
			</Button>
			<Button variant="outline" class="hidden h-8 w-8 p-0 lg:flex" disabled={!table.getCanNextPage()} onclick={() => table.lastPage()}>
				<span class="sr-only">Go to last page</span>
				<ChevronsRight size={15} />
			</Button>
		</div>
	</div>
</div>
