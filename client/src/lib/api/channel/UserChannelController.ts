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
 * Pause channel
 */
export async function pauseChannel(id: number): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/channel/{id}/pause', {
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
 * Resume channel
 */
export async function resumeChannel(id: number): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/channel/{id}/resume', {
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
 * Delete channel (soft delete)
 */
export async function deleteChannel(id: number): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.DELETE('/v1/user/channel/{id}', {
		params: {
			path: { id }
		}
	});
	if (error) {
		throw error;
	}
	return data;
}
