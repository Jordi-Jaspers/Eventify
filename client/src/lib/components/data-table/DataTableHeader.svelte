<script lang="ts" generics="T">
	import { ArrowUp, ArrowDown, ChevronsUpDown } from '@lucide/svelte';
	import type { SortDirection } from '$lib/api/models';
	import type { DataTableColumn } from './types';
	import type { Component } from 'svelte';
	import Checkbox from '$lib/components/ui/checkbox/checkbox.svelte';

	interface Props<T> {
		columns: DataTableColumn<T>[];
		currentSortKey: string | null;
		currentSortDirection: SortDirection;
		onSort: (key: string) => void;
		selectable?: boolean;
		allSelected?: boolean;
		indeterminate?: boolean;
		onToggleSelectAll?: () => void;
	}

	let {
		columns,
		currentSortKey,
		currentSortDirection,
		onSort,
		selectable = false,
		allSelected = false,
		indeterminate = false,
		onToggleSelectAll
	}: Props<T> = $props();

	const totalCols: number = $derived(
		columns.reduce((sum: number, col: DataTableColumn<T>) => sum + (col.colSpan ?? 1), 0)
	);

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
	class="hidden md:grid gap-4 px-4 py-3 border-b border-border/50 font-medium text-sm text-muted-foreground bg-muted/30"
	style="grid-template-columns: repeat({totalCols}, minmax(0, 1fr));"
>
	{#each columns as column}
		{@const colSpan = column.colSpan ?? 1}
		{#if colSpan > 0}
			<div style="grid-column: span {colSpan} / span {colSpan};">
		{#if column.key === 'checkbox'}
				{#if selectable}
					<div class="flex items-center justify-center h-full">
					<Checkbox
						checked={allSelected}
						{indeterminate}
						onCheckedChange={() => onToggleSelectAll?.()}
						aria-label="Select all"
						class="cursor-pointer"
					/>
					</div>
				{/if}
			{:else if column.sortable}
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
