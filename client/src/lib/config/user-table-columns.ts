import type { DataTableColumn } from '$lib/components/data-table/types';
import type { UserDetailsResponse } from '$lib/api/models';

export const userTableColumns: DataTableColumn<UserDetailsResponse>[] = [
	{
		key: 'search',
		label: 'User',
		filterable: true,
		filterType: 'FUZZY_TEXT',
		filterPlaceholder: 'Search by name or email...',
		colSpan: 3
	},
	{
		key: 'email',
		label: 'Email',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'role',
		label: 'Role',
		sortable: true,
		filterable: true,
		filterType: 'MULTI_ENUM',
		filterOptions: [
			{ value: 'USER', label: 'User' },
			{ value: 'ADMIN', label: 'Admin' }
		],
		colSpan: 1
	},
	{
		key: 'enabled',
		label: 'Status',
		sortable: true,
		filterable: true,
		filterType: 'BOOLEAN',
		colSpan: 1
	},
	{
		key: 'createdAt',
		label: 'Created',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'lastLogin',
		label: 'Last Login',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'actions'
	}
];

export function getStatusBadgeVariant(
	enabled: boolean | undefined,
	validated: boolean | undefined
): 'success' | 'destructive' | 'default' {
	if (!enabled) return 'destructive';
	if (!validated) return 'default';
	return 'success';
}

export function getStatusLabel(enabled: boolean | undefined, validated: boolean | undefined): string {
	if (!enabled) return 'Locked';
	if (!validated) return 'Pending Verification';
	return 'Active';
}
