import { client } from '../client';
import type { SortablePageInput } from '$lib/api/models';
import type { components } from '$lib/types/api';

type WatchlistDetailsResponse = components['schemas']['WatchlistDetailsResponse'];
type PageResourceWatchlistDetailsResponse =
	components['schemas']['PageResourceWatchlistDetailsResponse'];

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
