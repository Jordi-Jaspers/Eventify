import { client } from '$lib/api/client';
import type {
	SubscriptionResponse,
	CreateSubscriptionRequest,
	UpdateSubscriptionRequest,
	SortablePageInput,
	PageResource
} from '$lib/api/models';

export async function searchPersonalSubscriptions(
	input: SortablePageInput
): Promise<PageResource<SubscriptionResponse>> {
	const { data, error } = await client.POST('/v1/subscriptions/search', { body: input });
	if (error) throw error;
	return data as PageResource<SubscriptionResponse>;
}

export async function createPersonalSubscription(
	body: CreateSubscriptionRequest
): Promise<SubscriptionResponse> {
	const { data, error } = await client.POST('/v1/subscriptions', { body });
	if (error) throw error;
	return data!;
}

export async function updatePersonalSubscription(
	id: number,
	body: UpdateSubscriptionRequest
): Promise<SubscriptionResponse> {
	const { data, error } = await client.PUT('/v1/subscriptions/{id}', {
		params: { path: { id } },
		body
	});
	if (error) throw error;
	return data!;
}

export async function deletePersonalSubscription(id: number): Promise<void> {
	const { error } = await client.DELETE('/v1/subscriptions/{id}', {
		params: { path: { id } }
	});
	if (error) throw error;
}

export async function getSubscriptionByWatchlistId(watchlistId: number): Promise<SubscriptionResponse | null> {
	const { data, error } = await client.POST('/v1/subscriptions/search', {
		body: {
			pageNumber: 0,
			pageSize: 1,
			sortOrder: [],
			searchInputs: [{ fieldName: 'watchlistId', value: String(watchlistId), searchType: 'NUMERIC' }]
		}
	});
	if (error) throw error;
	const content = (data as { content?: SubscriptionResponse[] }).content;
	return content && content.length > 0 ? content[0] : null;
}
