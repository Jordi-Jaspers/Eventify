import {
	createChannel,
	updateChannel,
	pauseChannel,
	resumeChannel,
	deleteChannel
} from '$lib/api/channel/UserChannelController';
import type { ChannelDetailsResponse } from '$lib/api/models';
import { toast } from 'svelte-sonner';

/**
 * Service for managing user channel operations with error handling and toast notifications
 */
export class UserChannelService {
	/**
	 * Create a new channel
	 */
	async createChannel(
		name: string,
		slug: string,
		description: string | undefined
	): Promise<ChannelDetailsResponse> {
		try {
			const channel: ChannelDetailsResponse = await createChannel({
				name,
				slug,
				description
			});
			toast.success('Channel created successfully');
			return channel;
		} catch (error) {
			toast.error('Failed to create channel');
			throw error;
		}
	}

	/**
	 * Update an existing channel
	 */
	async updateChannel(
		channelId: number,
		name: string,
		description: string | undefined
	): Promise<ChannelDetailsResponse> {
		try {
			const channel: ChannelDetailsResponse = await updateChannel(channelId, {
				name,
				description
			});
			toast.success('Channel updated successfully');
			return channel;
		} catch (error) {
			toast.error('Failed to update channel');
			throw error;
		}
	}

	/**
	 * Pause a channel
	 */
	async pauseChannel(channelId: number): Promise<void> {
		try {
			await pauseChannel(channelId);
			toast.success('Channel paused');
		} catch (error) {
			toast.error('Failed to pause channel');
			throw error;
		}
	}

	/**
	 * Resume a paused channel
	 */
	async resumeChannel(channelId: number): Promise<void> {
		try {
			await resumeChannel(channelId);
			toast.success('Channel resumed');
		} catch (error) {
			toast.error('Failed to resume channel');
			throw error;
		}
	}

	/**
	 * Delete a channel with confirmation
	 */
	async deleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		const confirmed: boolean = confirm(
			`Are you sure you want to delete "${channel.name}"? This action cannot be undone.`
		);

		if (!confirmed) {
			return;
		}

		try {
			await deleteChannel(channel.id ?? 0);
			toast.success('Channel deleted');
		} catch (error) {
			toast.error('Failed to delete channel');
			throw error;
		}
	}
}
