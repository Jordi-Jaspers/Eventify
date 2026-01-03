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
	class="hidden md:grid gap-4 px-4 py-3 border-b border-border/50 font-medium text-sm text-muted-foreground"
	style="grid-template-columns: repeat({columns.reduce((sum, col) => sum + (col.colSpan ?? 1), 0)}, minmax(0, 1fr));"
>
	{#each columns as column}
		{@const colSpan = column.colSpan ?? 1}
		<div class="col-span-{colSpan}">
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
			{:else}
				<span>{column.label}</span>
			{/if}
		</div>
	{/each}
</div>
