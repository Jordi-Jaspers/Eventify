import type { ChannelDetailsResponse } from '$lib/api/models';
import type { DataTableService } from '$lib/components/data-table/types';

/**
 * Reusable multi-select state for channel lists.
 * Accepts a getter for the current page items so derived state stays reactive.
 */
export function createChannelSelectionService(
	getItems: () => ChannelDetailsResponse[]
) {
	let selectedChannels: ChannelDetailsResponse[] = $state([]);

	const selectedIds: Set<number> = $derived(
		new Set(selectedChannels.map((c: ChannelDetailsResponse) => c.id ?? 0))
	);

	const isAllSelected: boolean = $derived.by((): boolean => {
		const items: ChannelDetailsResponse[] = getItems();
		return items.length > 0 && selectedChannels.length === items.length;
	});

	const isIndeterminate: boolean = $derived.by((): boolean => {
		const items: ChannelDetailsResponse[] = getItems();
		return selectedChannels.length > 0 && selectedChannels.length < items.length;
	});

	function toggleSelectChannel(channel: ChannelDetailsResponse): void {
		const id: number = channel.id ?? 0;
		if (selectedIds.has(id)) {
			selectedChannels = selectedChannels.filter(
				(c: ChannelDetailsResponse) => c.id !== id
			);
		} else {
			selectedChannels = [...selectedChannels, channel];
		}
	}

	function clearSelection(): void {
		selectedChannels = [];
	}

	function selectAll(): void {
		selectedChannels = [...getItems()];
	}

	function toggleSelectAll(): void {
		const items: ChannelDetailsResponse[] = getItems();
		if (selectedChannels.length === items.length && items.length > 0) {
			clearSelection();
		} else {
			selectAll();
		}
	}

	return {
		get selectedChannels(): ChannelDetailsResponse[] { return selectedChannels; },
		get selectedIds(): Set<number> { return selectedIds; },
		get isAllSelected(): boolean { return isAllSelected; },
		get isIndeterminate(): boolean { return isIndeterminate; },
		toggleSelectChannel,
		clearSelection,
		selectAll,
		toggleSelectAll
	};
}

export type ChannelSelectionService = ReturnType<typeof createChannelSelectionService>;
