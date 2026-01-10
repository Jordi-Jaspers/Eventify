<script lang="ts" generics="T">
	import { ArrowUp, ArrowDown, ChevronsUpDown } from '@lucide/svelte';
	import type { SortDirection } from '$lib/api/models';
	import type { DataTableColumn } from './types';
	import type { Component } from 'svelte';

	interface Props<T> {
		columns: DataTableColumn<T>[];
		currentSortKey: string | null;
		currentSortDirection: SortDirection;
		onSort: (key: string) => void;
	}

	let {
		columns,
		currentSortKey,
		currentSortDirection,
		onSort
	}: Props<T> = $props();

	const totalCols: number = $derived(
		columns.reduce((sum: number, col: DataTableColumn<T>) => sum + (col.colSpan ?? 1), 0)
	);

	// Get the appropriate grid class based on total columns
	const gridClass: string = $derived(`grid-cols-${totalCols}`);

	function handleSort(key: string, sortable: boolean = false): void {
		if (!sortable) return;
		onSort(key);
	}

	function getSortIcon(key: string, sortable: boolean = false): Component {
		if (!sortable) return ChevronsUpDown;
		if (currentSortKey !== key) return ChevronsUpDown;
		return currentSortDirection === 'ASC' ? ArrowUp : ArrowDown;
	}

	const SortIconComponent = $derived.by(() => {
		return getSortIcon;
	});
</script>

<div
	class="hidden md:grid {gridClass} gap-4 px-4 py-3 border-b border-border/50 font-medium text-sm text-muted-foreground bg-muted/30"
>
	{#each columns as column}
		{@const colSpan = column.colSpan ?? 1}
		{#if colSpan > 0}
			<div style="grid-column: span {colSpan} / span {colSpan};">
				{#if column.sortable}
					{@const IconComponent = getSortIcon(column.key, column.sortable)}
					<button
						type="button"
						onclick={() => handleSort(column.key, column.sortable)}
						class="flex items-center gap-1 hover:text-primary transition-colors cursor-pointer"
					>
						{column.label}
						<IconComponent class="h-3 w-3" />
					</button>
				{:else if column.label}
					<span>{column.label}</span>
				{/if}
			</div>
		{/if}
	{/each}
</div>
