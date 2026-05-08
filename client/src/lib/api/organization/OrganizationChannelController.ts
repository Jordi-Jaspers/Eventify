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
 * Pause organization channel
 */
export async function pauseOrganizationChannel(orgId: number, id: number): Promise<void> {
	const { error } = await client.POST('/v1/organization/{orgId}/channels/{id}/pause', {
		params: { path: { orgId, id } }
	});

	if (error) {
		throw error;
	}
}

/**
 * Resume organization channel
 */
export async function resumeOrganizationChannel(orgId: number, id: number): Promise<void> {
	const { error } = await client.POST('/v1/organization/{orgId}/channels/{id}/resume', {
		params: { path: { orgId, id } }
	});

	if (error) {
		throw error;
	}
}

/**
 * Delete organization channel
 */
export async function deleteOrganizationChannel(orgId: number, id: number): Promise<void> {
	const { error } = await client.DELETE('/v1/organization/{orgId}/channels/{id}', {
		params: { path: { orgId, id } }
	});

	if (error) {
		throw error;
	}
}
