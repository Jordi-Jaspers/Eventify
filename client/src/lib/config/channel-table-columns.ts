import type { DataTableColumn } from '$lib/components/data-table/types';
import type { ChannelDetailsResponse } from '$lib/api/models';

/**
 * Shared column definitions for channel tables across user and organization pages.
 * Centralizes table configuration to avoid duplication.
 * 
 * Note: "Created" column removed - now shown in ChannelDetailsSheet.
 */
export const channelTableColumns: DataTableColumn<ChannelDetailsResponse>[] = [
	{
		key: 'name',
		label: 'Channel',
		sortable: true,
		filterable: true,
		filterType: 'FUZZY_TEXT',
		filterPlaceholder: 'Search channels...',
		colSpan: 4
	},
	{
		key: 'description',
		label: 'Description',
		colSpan: 7
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
		colSpan: 2
	},
	{
		key: 'lastEventAt',
		label: 'Last Activity',
		sortable: true,
		colSpan: 2
	},
	{
		key: 'isStale',
		label: 'Stale',
		filterable: true,
		filterType: 'BOOLEAN',
		filterOptions: [
			{ value: 'true', label: 'Stale only' },
			{ value: 'false', label: 'Active only' }
		],
		colSpan: 0
	},
	{
		key: 'actions',
		colSpan: 1
	}
];
