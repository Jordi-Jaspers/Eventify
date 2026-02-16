import type { DataTableColumn } from '$lib/components/data-table/types';
import type { ChannelDetailsResponse } from '$lib/api/models';

/**
 * Shared column configuration for channel data tables
 */
export const channelColumns: DataTableColumn<ChannelDetailsResponse>[] = [
	{
		key: 'name',
		label: 'Channel',
		sortable: true,
		filterable: true,
		filterType: 'FUZZY_TEXT',
		filterPlaceholder: 'Search channels...',
		colSpan: 2
	},
	{
		key: 'description',
		label: 'Description',
		colSpan: 6
	},
	{
		key: 'status',
		label: 'Status',
		sortable: true,
		filterable: true,
		filterType: 'MULTI_ENUM',
		filterOptions: [
			{ value: 'ACTIVE', label: 'Active' },
			{ value: 'PAUSED', label: 'Paused' }
		],
		colSpan: 1
	},
	{
		key: 'createdAt',
		label: 'Created',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'actions',
		colSpan: 1
	}
];
