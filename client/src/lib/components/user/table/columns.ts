import type { ColumnDef } from '@tanstack/table-core';
import { createRawSnippet } from 'svelte';
import { renderComponent, renderSnippet } from '$lib/components/ui/data-table/index.js';
import { UserTableActions } from '$lib/components/user';
import DataTableCreatedButton from '$lib/components/user/table/header/created-header-button.svelte';
import DataTableLastLoginButton from '$lib/components/user/table/header/last-login-header-button.svelte';
import DataTableNameButton from '$lib/components/user/table/header/name-header-button.svelte';
import DataTableEmailButton from '$lib/components/user/table/header/email-header-button.svelte';
import DataTableLockedButton from '$lib/components/user/table/header/locked-header-button.svelte';
import DataTableValidatedButton from '$lib/components/user/table/header/validated-header-button.svelte';
import DataTableAuthorityButton from '$lib/components/user/table/header/authority-header-button.svelte';

interface TeamMemberResponse {
	id: number;
	email: string;
	firstName: string;
	lastName: string;
}

interface UserDetailsResponse extends TeamMemberResponse {
	authority: string;
	permissions: string[];
	teams: TeamResponse[];
	lastLogin: Date;
	created: Date;
	enabled: boolean;
	validated: boolean;
}

export const columns: ColumnDef<UserDetailsResponse>[] = [
	{
		id: 'actions',
		cell: ({ row }) => {
			return renderComponent(UserTableActions, {
				id: row.original.id,
				enabled: row.original.enabled,
				authority: row.original.authority
			});
		}
	},
	{
		accessorKey: 'name',
		header: ({ column }) =>
			renderComponent(DataTableNameButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const firstName: string = row.original.firstName;
			const lastName: string = row.original.lastName;
			const email: string = row.original.email;

			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-nowrap">${firstName} ${lastName}</p>`
			}));
			return renderSnippet(createdSnippet, email);
		}
	},
	{
		accessorKey: 'email',
		header: ({ column }) =>
			renderComponent(DataTableEmailButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			})
	},
	{
		accessorKey: 'authority',
		header: ({ column }) =>
			renderComponent(DataTableAuthorityButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const authority: string = row.getValue('authority');
			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-wrap">${authority.charAt(0).toUpperCase() + authority.slice(1).toLowerCase()}</p>`
			}));
			return renderSnippet(createdSnippet, authority);
		}
	},
	{
		accessorKey: 'enabled',
		header: ({ column }) =>
			renderComponent(DataTableLockedButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const enabled: boolean = row.getValue('enabled');
			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-wrap flex justify-center ">${enabled ? '✗' : '✓'}</p>`
			}));
			return renderSnippet(createdSnippet, enabled);
		}
	},
	{
		accessorKey: 'validated',
		header: ({ column }) =>
			renderComponent(DataTableValidatedButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const validated: boolean = row.getValue('validated');
			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-wrap flex justify-center ">${validated ? '✓' : '✗'}</p>`
			}));
			return renderSnippet(createdSnippet, validated);
		}
	},
	{
		accessorKey: 'lastLogin',
		header: ({ column }) =>
			renderComponent(DataTableLastLoginButton, {
				onclick: () => column.toggleSorting(column.getIsSorted() === 'asc')
			}),
		cell: ({ row }) => {
			const time: string = new Date(row.getValue('lastLogin')).toLocaleTimeString(navigator.language);
			const date: string = new Date(row.getValue('lastLogin')).toLocaleDateString(navigator.language);
			const lastLogin = time + ' ' + date;

			const createdSnippet = createRawSnippet(() => ({
				render: () => `<p class="text-muted-foreground text-wrap">${lastLogin}</p>`
			}));

			return renderSnippet(createdSnippet, lastLogin);
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
	}
];
