<script lang="ts">
	import { ChevronUp, ChevronDown, ArrowUpDown } from '@lucide/svelte';
	import type { SortDirection } from '$lib/api/models';

	interface Column {
		key: string;
		label: string;
		sortable: boolean;
		colSpan?: number;
	}

	interface Props {
		columns: Column[];
		currentSortKey: string | null;
		currentSortDirection: SortDirection;
		onSort: (key: string) => void;
	}

	let { columns, currentSortKey, currentSortDirection, onSort }: Props = $props();

	function handleSort(column: Column): void {
		if (column.sortable) {
			onSort(column.key);
		}
	}

	function getSortIcon(column: Column): typeof ChevronUp | typeof ChevronDown | typeof ArrowUpDown {
		if (!column.sortable) return ArrowUpDown;
		if (currentSortKey !== column.key) return ArrowUpDown;
		return currentSortDirection === 'ASC' ? ChevronUp : ChevronDown;
	}

	function getIconClass(column: Column): string {
		return `h-4 w-4 ${currentSortKey === column.key ? 'text-primary' : 'text-muted-foreground/50'}`;
	}
</script>

<div
	class="hidden md:grid gap-4 px-4 py-2 text-sm font-medium text-muted-foreground border-b border-border/50"
	style="grid-template-columns: {columns
		.map((col: Column) => `repeat(${col.colSpan ?? 1}, minmax(0, 1fr))`)
		.join(' ')}"
>
	{#each columns as column (column.key)}
		{#if column.sortable}
			{@const Icon = getSortIcon(column)}
			<button
				type="button"
				onclick={() => handleSort(column)}
				class="flex items-center gap-1 hover:text-foreground transition-colors text-left"
				style="grid-column: span {column.colSpan ?? 1}"
			>
				<span>{column.label}</span>
				<Icon class={getIconClass(column)} />
			</button>
		{:else}
			<div style="grid-column: span {column.colSpan ?? 1}">
				{column.label}
			</div>
		{/if}
	{/each}
</div>
