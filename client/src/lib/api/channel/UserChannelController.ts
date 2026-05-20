import { client } from '../client';
import type {
	CreateChannelRequest,
	UpdateChannelRequest,
	SortablePageInput
} from '$lib/api/models';
import type { components } from '$lib/types/api';

type ChannelDetailsResponse = components['schemas']['ChannelDetailsResponse'];
type PageResourceChannelDetailsResponse = components['schemas']['PageResourceChannelDetailsResponse'];

/**
 * Search personal channels with pagination
 */
export async function searchChannels(
	input: SortablePageInput
): Promise<PageResourceChannelDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/channel/search', {
		body: input
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Create a new channel
 */
export async function createChannel(
	request: CreateChannelRequest
): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/channel', {
		body: request
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Get channel details
 */
export async function getChannel(id: number): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.GET('/v1/user/channel/{id}', {
		params: {
			path: { id }
		}
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Update channel
 */
export async function updateChannel(
	id: number,
	request: UpdateChannelRequest
): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.PUT('/v1/user/channel/{id}', {
		params: {
			path: { id }
		},
		body: request
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Batch pause channels
 */
export async function pauseChannels(ids: number[]): Promise<void> {
	const { error } = await client.POST('/v1/user/channel/pause', {
		body: { channelIds: ids }
	});
	if (error) {
		throw error;
	}
}

/**
 * Batch resume channels
 */
export async function resumeChannels(ids: number[]): Promise<void> {
	const { error } = await client.POST('/v1/user/channel/resume', {
		body: { channelIds: ids }
	});
	if (error) {
		throw error;
	}
}

/**
 * Batch delete channels (soft delete)
 */
export async function deleteChannels(ids: number[]): Promise<void> {
	const { error } = await client.DELETE('/v1/user/channel', {
		body: { channelIds: ids }
	});
	if (error) {
		throw error;
	}
}
