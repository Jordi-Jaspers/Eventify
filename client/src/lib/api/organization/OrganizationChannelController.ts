import type { PageResource, ChannelDetailsResponse, CreateChannelRequest, UpdateChannelRequest, SortablePageInput } from '$lib/api/models';
import { client } from '$lib/api/client';

/**
 * Search organization channels with pagination, sorting, and filtering
 */
export async function searchOrganizationChannels(
	orgId: number,
	input: SortablePageInput
): Promise<PageResource<ChannelDetailsResponse>> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/channels/search', {
		params: { path: { orgId } },
		body: input
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Get organization channel by ID
 */
export async function getOrganizationChannel(orgId: number, id: number): Promise<ChannelDetailsResponse | null> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/channels/{id}', {
		params: { path: { orgId, id } }
	});

	if (error) {
		return null;
	}

	return data;
}

/**
 * Create organization channel
 */
export async function createOrganizationChannel(orgId: number, input: CreateChannelRequest): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/channels', {
		params: { path: { orgId } },
		body: input
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Update organization channel
 */
export async function updateOrganizationChannel(
	orgId: number,
	id: number,
	input: UpdateChannelRequest
): Promise<ChannelDetailsResponse> {
	const { data, error } = await client.PUT('/v1/organization/{orgId}/channels/{id}', {
		params: { path: { orgId, id } },
		body: input
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Batch pause organization channels
 */
export async function pauseOrganizationChannels(orgId: number, ids: number[]): Promise<void> {
	const { error } = await client.POST('/v1/organization/{orgId}/channels/pause', {
		params: { path: { orgId } },
		body: { channelIds: ids }
	});

	if (error) {
		throw error;
	}
}

/**
 * Batch resume organization channels
 */
export async function resumeOrganizationChannels(orgId: number, ids: number[]): Promise<void> {
	const { error } = await client.POST('/v1/organization/{orgId}/channels/resume', {
		params: { path: { orgId } },
		body: { channelIds: ids }
	});

	if (error) {
		throw error;
	}
}

/**
 * Batch delete organization channels
 */
export async function deleteOrganizationChannels(orgId: number, ids: number[]): Promise<void> {
	const { error } = await client.DELETE('/v1/organization/{orgId}/channels', {
		params: { path: { orgId } },
		body: { channelIds: ids }
	});

	if (error) {
		throw error;
	}
}
