import type { ColumnDef } from '@tanstack/table-core';
import { createRawSnippet } from 'svelte';
import { renderComponent, renderSnippet } from '$lib/components/ui/data-table/index.js';
import { TeamTableActions } from '$lib/components/teams';
import DataTableNameButton from '$lib/components/teams/table/header/name-header-button.svelte';
import DataTableCreatedButton from '$lib/components/teams/table/header/created-header-button.svelte';
import DataTableMembersButton from '$lib/components/teams/table/header/members-header-button.svelte';

export const columns: ColumnDef<TeamResponse>[] = [
	{
		id: 'actions',
		cell: ({ row }) => {
			// You can pass whatever you need from `row.original` to the component
			return renderComponent(TeamTableActions, {
				id: row.original.id,
				name: row.original.name,
				description: row.original.description
			});
		}
	},
	{
		accessorKey: 'members',
		header: ({ column }) =>
			renderComponent(DataTableMembersButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const members: TeamMemberResponse[] = row.getValue('members');
			const membersSnippet = createRawSnippet(() => ({
				render: () => `<p class="min-w-16 max-w-min text-muted-foreground text-center">${members.length}</p>`
			}));

			return renderSnippet(membersSnippet, members.length);
		}
	},
	{
		accessorKey: 'created',
		header: ({ column }) =>
			renderComponent(DataTableCreatedButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const time: string = new Date(row.getValue('created')).toLocaleTimeString(navigator.language);
			const date: string = new Date(row.getValue('created')).toLocaleDateString(navigator.language);
			const created = time + ' ' + date;

			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-muted-foreground text-wrap">${created}</p>`
			}));

			return renderSnippet(createdSnippet, created);
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
