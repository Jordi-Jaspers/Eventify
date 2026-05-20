import { client } from '$lib/api/client';
import type { SubscribeRequest, SubscriptionResponse } from '$lib/api/models';

export async function getSubscription(watchlistId: number): Promise<SubscriptionResponse | null> {
	const { data, error, response } = await client.GET(
		'/v1/user/watchlist/{watchlistId}/subscription',
		{ params: { path: { watchlistId } } }
	);
	if (response.status === 404) return null;
	if (error) throw error;
	return data ?? null;
}

export async function subscribe(
	watchlistId: number,
	body: SubscribeRequest
): Promise<SubscriptionResponse> {
	const { data, error } = await client.POST('/v1/user/watchlist/{watchlistId}/subscription', {
		params: { path: { watchlistId } },
		body
	});
	if (error) throw error;
	return data!;
}

export async function unsubscribe(watchlistId: number): Promise<void> {
	const { error } = await client.DELETE('/v1/user/watchlist/{watchlistId}/subscription', {
		params: { path: { watchlistId } }
	});
	if (error) throw error;
}
