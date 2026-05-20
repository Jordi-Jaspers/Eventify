import {
	createChannel,
	updateChannel,
	pauseChannels,
	resumeChannels,
	deleteChannels
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
	 * Pause a single channel (uses batch endpoint)
	 */
	async pauseChannel(channelId: number): Promise<void> {
		try {
			await pauseChannels([channelId]);
			toast.success('Channel paused');
		} catch (error) {
			toast.error('Failed to pause channel');
			throw error;
		}
	}

	/**
	 * Resume a single paused channel (uses batch endpoint)
	 */
	async resumeChannel(channelId: number): Promise<void> {
		try {
			await resumeChannels([channelId]);
			toast.success('Channel resumed');
		} catch (error) {
			toast.error('Failed to resume channel');
			throw error;
		}
	}

	/**
	 * Delete a single channel (uses batch endpoint)
	 */
	async deleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		try {
			await deleteChannels([channel.id ?? 0]);
			toast.success('Channel deleted');
		} catch (error) {
			toast.error('Failed to delete channel');
			throw error;
		}
	}

	/**
	 * Batch pause channels
	 */
	async pauseChannels(ids: number[]): Promise<void> {
		try {
			await pauseChannels(ids);
			toast.success(`${ids.length} channel${ids.length > 1 ? 's' : ''} paused`);
		} catch (error) {
			toast.error('Failed to pause channels');
			throw error;
		}
	}

	/**
	 * Batch resume channels
	 */
	async resumeChannels(ids: number[]): Promise<void> {
		try {
			await resumeChannels(ids);
			toast.success(`${ids.length} channel${ids.length > 1 ? 's' : ''} resumed`);
		} catch (error) {
			toast.error('Failed to resume channels');
			throw error;
		}
	}

	/**
	 * Batch delete channels
	 */
	async deleteChannels(ids: number[]): Promise<void> {
		try {
			await deleteChannels(ids);
			toast.success(`${ids.length} channel${ids.length > 1 ? 's' : ''} deleted`);
		} catch (error) {
			toast.error('Failed to delete channels');
			throw error;
		}
	}
}
