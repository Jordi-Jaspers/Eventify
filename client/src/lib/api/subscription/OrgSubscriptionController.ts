import { client } from '$lib/api/client';
import type {
	SubscriptionResponse,
	CreateSubscriptionRequest,
	UpdateSubscriptionRequest,
	SortablePageInput,
	PageResource
} from '$lib/api/models';

export async function searchOrgSubscriptions(
	orgId: number,
	input: SortablePageInput
): Promise<PageResource<SubscriptionResponse>> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/subscriptions/search', {
		params: { path: { orgId } },
		body: input
	});
	if (error) throw error;
	return data as PageResource<SubscriptionResponse>;
}

export async function createOrgSubscription(
	orgId: number,
	body: CreateSubscriptionRequest
): Promise<SubscriptionResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/subscriptions', {
		params: { path: { orgId } },
		body
	});
	if (error) throw error;
	return data!;
}

export async function updateOrgSubscription(
	orgId: number,
	id: number,
	body: UpdateSubscriptionRequest
): Promise<SubscriptionResponse> {
	const { data, error } = await client.PUT('/v1/organization/{orgId}/subscriptions/{id}', {
		params: { path: { orgId, id } },
		body
	});
	if (error) throw error;
	return data!;
}

export async function deleteOrgSubscription(orgId: number, id: number): Promise<void> {
	const { error } = await client.DELETE('/v1/organization/{orgId}/subscriptions/{id}', {
		params: { path: { orgId, id } }
	});
	if (error) throw error;
}
