import type { ChannelDetailsResponse } from '$lib/api/models';
import { createInlineEditService } from '$lib/utils/inline-edit.svelte';

interface ChannelDetailsSheetService {
	nameEdit: ReturnType<typeof createInlineEditService>;
	descriptionEdit: ReturnType<typeof createInlineEditService>;
	pauseLoading: boolean;
	resumeLoading: boolean;
	deleteLoading: boolean;
	resetEditStates: () => void;
	handlePause: (
		channel: ChannelDetailsResponse,
		onPause: (channel: ChannelDetailsResponse) => Promise<void>
	) => Promise<void>;
	handleResume: (
		channel: ChannelDetailsResponse,
		onResume: (channel: ChannelDetailsResponse) => Promise<void>
	) => Promise<void>;
	handleDelete: (
		channel: ChannelDetailsResponse,
		onDelete: (channel: ChannelDetailsResponse) => Promise<void>
	) => Promise<void>;
}

/**
 * Service for managing channel details sheet state and actions.
 * Encapsulates edit state, action loading states, and business logic.
 */
export function createChannelDetailsSheetService(): ChannelDetailsSheetService {
	// Edit services for name and description
	const nameEdit = createInlineEditService();
	const descriptionEdit = createInlineEditService();

	// Action loading states
	let pauseLoading: boolean = $state(false);
	let resumeLoading: boolean = $state(false);
	let deleteLoading: boolean = $state(false);

	/**
	 * Reset all edit states (called when sheet closes or channel changes)
	 */
	function resetEditStates(): void {
		nameEdit.cancel();
		descriptionEdit.cancel();
	}

	/**
	 * Handle pause action with loading state
	 */
	async function handlePause(
		channel: ChannelDetailsResponse,
		onPause: (channel: ChannelDetailsResponse) => Promise<void>
	): Promise<void> {
		pauseLoading = true;
		try {
			await onPause(channel);
		} finally {
			pauseLoading = false;
		}
	}

	/**
	 * Handle resume action with loading state
	 */
	async function handleResume(
		channel: ChannelDetailsResponse,
		onResume: (channel: ChannelDetailsResponse) => Promise<void>
	): Promise<void> {
		resumeLoading = true;
		try {
			await onResume(channel);
		} finally {
			resumeLoading = false;
		}
	}

	/**
	 * Handle delete action with loading state
	 */
	async function handleDelete(
		channel: ChannelDetailsResponse,
		onDelete: (channel: ChannelDetailsResponse) => Promise<void>
	): Promise<void> {
		deleteLoading = true;
		try {
			await onDelete(channel);
		} finally {
			deleteLoading = false;
		}
	}

	return {
		nameEdit,
		descriptionEdit,
		get pauseLoading() {
			return pauseLoading;
		},
		get resumeLoading() {
			return resumeLoading;
		},
		get deleteLoading() {
			return deleteLoading;
		},
		resetEditStates,
		handlePause,
		handleResume,
		handleDelete
	};
}
