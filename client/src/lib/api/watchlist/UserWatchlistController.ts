import { client } from '../client';
import type {
	SortablePageInput,
	WatchlistDetailsResponse,
	PageResourceWatchlistDetailsResponse,
	CreateWatchlistRequest,
	UpdateWatchlistRequest
} from '$lib/api/models';

/**
 * Search personal watchlists with pagination
 */
export async function searchWatchlists(
	input: SortablePageInput
): Promise<PageResourceWatchlistDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/watchlists/search', {
		body: input
	});
	if (error) {
		throw error;
	}
	return data;
}

/**
 * Delete watchlist (soft delete)
 */
export async function deleteWatchlist(id: number): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.DELETE('/v1/user/watchlists/{id}', {
		params: {
			path: { id }
		}
	});
	if (error || !data) {
		throw error ?? new Error('Failed to delete watchlist');
	}
	return data;
}

/**
 * Get watchlist by ID
 */
export async function getWatchlist(id: number): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.GET('/v1/user/watchlists/{id}', {
		params: {
			path: { id }
		}
	});
	if (error || !data) {
		throw error ?? new Error('Failed to get watchlist');
	}
	return data;
}

/**
 * Create new watchlist
 */
export async function createWatchlist(
	request: CreateWatchlistRequest
): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.POST('/v1/user/watchlists', {
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to create watchlist');
	}
	return data;
}

/**
 * Update existing watchlist
 */
export async function updateWatchlist(
	id: number,
	request: UpdateWatchlistRequest
): Promise<WatchlistDetailsResponse> {
	const { data, error } = await client.PUT('/v1/user/watchlists/{id}', {
		params: {
			path: { id }
		},
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to update watchlist');
	}
	return data;
}
