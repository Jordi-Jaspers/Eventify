import type { DataTableColumn } from '$lib/components/data-table/types';
import type { OrganizationMembershipResponse } from '$lib/api/models';

export const memberTableColumns: DataTableColumn<OrganizationMembershipResponse>[] = [
	{
		key: 'search',
		label: 'Search',
		filterable: true,
		filterType: 'FUZZY_TEXT',
		filterPlaceholder: 'Search members...',
		colSpan: 0
	},
	{
		key: 'member',
		label: 'Member',
		colSpan: 4
	},
	{
		key: 'email',
		label: 'Email',
		sortable: true,
		colSpan: 3
	},
	{
		key: 'role',
		label: 'Role',
		sortable: true,
		filterable: true,
		filterType: 'MULTI_ENUM',
		filterOptions: [
			{ value: 'OWNER', label: 'Owner' },
			{ value: 'ADMIN', label: 'Admin' },
			{ value: 'MEMBER', label: 'Member' }
		],
		colSpan: 2
	},
	{
		key: 'joinedAt',
		label: 'Joined',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'actions'
	}
];
