import {
	createOrganizationChannel,
	updateOrganizationChannel,
	pauseOrganizationChannel,
	resumeOrganizationChannel,
	deleteOrganizationChannel
} from '$lib/api/organization/OrganizationChannelController';
import type { ChannelDetailsResponse } from '$lib/api/models';
import { toast } from 'svelte-sonner';

/**
 * Service for managing channel operations with error handling and toast notifications
 */
export class ChannelService {
	constructor(private orgId: number) {}

	/**
	 * Create a new channel
	 */
	async createChannel(
		name: string,
		description: string | undefined
	): Promise<ChannelDetailsResponse> {
		try {
			const channel: ChannelDetailsResponse = await createOrganizationChannel(this.orgId, {
				name,
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
			const channel: ChannelDetailsResponse = await updateOrganizationChannel(
				this.orgId,
				channelId,
				{ name, description }
			);
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
			await pauseOrganizationChannel(this.orgId, channelId);
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
			await resumeOrganizationChannel(this.orgId, channelId);
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
			await deleteOrganizationChannel(this.orgId, channel.id ?? 0);
			toast.success('Channel deleted');
		} catch (error) {
			toast.error('Failed to delete channel');
			throw error;
		}
	}
}
