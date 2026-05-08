import { client } from '../client';
import type {
	SortablePageInput,
	WatchlistDetailsResponse,
	PageResourceWatchlistDetailsResponse,
	CreateWatchlistRequest,
	UpdateWatchlistRequest
} from '$lib/api/models';

/**
 * Search organization watchlists with pagination
 */
export async function searchWatchlists(
	orgId: number,
	input: SortablePageInput
): Promise<PageResourceWatchlistDetailsResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/watchlist/search', {
		params: {
			path: { orgId }
		},
		body: input
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Get organization watchlist by ID
 */
export async function getWatchlist(orgId: number, id: number): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/watchlist/{id}', {
		params: {
			path: { orgId, id }
		}
	});
	if (error || !data) {
		throw error ?? new Error('Failed to get watchlist');
	}
	return data;
}

/**
 * Create new organization watchlist
 */
export async function createWatchlist(
	orgId: number,
	request: CreateWatchlistRequest
): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/watchlist', {
		params: {
			path: { orgId }
		},
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to create watchlist');
	}
	return data;
}

/**
 * Update existing organization watchlist
 */
export async function updateWatchlist(
	orgId: number,
	id: number,
	request: UpdateWatchlistRequest
): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.PUT('/v1/organization/{orgId}/watchlist/{id}', {
		params: {
			path: { orgId, id }
		},
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to update watchlist');
	}
	return data;
}

/**
 * Delete organization watchlist (soft delete)
 */
export async function deleteWatchlist(orgId: number, id: number): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.DELETE('/v1/organization/{orgId}/watchlist/{id}', {
		params: {
			path: { orgId, id }
		}
	});
	if (error || !data) {
		throw error ?? new Error('Failed to delete watchlist');
	}
	return data;
}
