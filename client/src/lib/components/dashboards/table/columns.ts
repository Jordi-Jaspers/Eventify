import type { ColumnDef } from '@tanstack/table-core';
import { createRawSnippet } from 'svelte';
import { renderComponent, renderSnippet } from '$lib/components/ui/data-table/index.js';
import { DashboardTableActions } from '$lib/components/dashboards';
import DataTableNameButton from '$lib/components/dashboards/table/header/name-header-button.svelte';
import DataTableLastUpdatedButton from '$lib/components/dashboards/table/header/last-updated-header-button.svelte';
import DataTablePublicButton from '$lib/components/dashboards/table/header/public-header-button.svelte';

export const columns: ColumnDef<DashboardResponse>[] = [
	{
		id: 'actions',
		cell: ({ row }) => {
			// You can pass whatever you need from `row.original` to the component
			return renderComponent(DashboardTableActions, {
				id: row.original.id,
				name: row.original.name,
				description: row.original.description,
				global: row.original.global,
				team: row.original.team
			});
		}
	},
	{
		accessorKey: 'lastUpdated',
		header: ({ column }) =>
			renderComponent(DataTableLastUpdatedButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const time: string = new Date(row.getValue('lastUpdated')).toLocaleTimeString(navigator.language);
			const date: string = new Date(row.getValue('lastUpdated')).toLocaleDateString(navigator.language);
			const lastUpdated = time + ' ' + date;

			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-muted-foreground text-wrap">${lastUpdated}</p>`
			}));

			return renderSnippet(createdSnippet, lastUpdated);
		}
	},
	{
		accessorKey: 'global',
		header: ({ column }) =>
			renderComponent(DataTablePublicButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const publiclyAvailable: boolean = row.getValue('global');
			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-wrap flex justify-center ">${publiclyAvailable ? '✓' : '✗'}</p>`
			}));
			return renderSnippet(createdSnippet, publiclyAvailable);
		}
	},
	{
		accessorKey: 'name',
		header: ({ column }) =>
			renderComponent(DataTableNameButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		meta: {
			cellClassName: 'text-nowrap'
		}
	},
	{
		accessorKey: 'description',
		header: 'Description',
		cell: ({ row }) => {
			const description: string = row.getValue('description');
			const descriptionSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-muted-foreground text-ellipsis whitespace-nowrap overflow-hidden">${description}</p>`
			}));

			return renderSnippet(descriptionSnippet, description);
		},
		meta: {
			cellClassName: 'min-w-[400px] max-w-screen-sm'
		}
	}
];
