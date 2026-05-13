import { client } from '../client';
import type {
	SortablePageInput,
	PageResource,
	BroadcastResponse,
	RecipientResponse,
	AudienceRequest,
	CreateBroadcastRequest,
	PreviewResponse
} from '$lib/api/models';

export async function sendBroadcast(request: CreateBroadcastRequest): Promise<BroadcastResponse> {
	const { data, error } = await client.POST('/v1/admin/notifications/broadcasts', {
		body: request
	});
	if (error) throw error;
	return data as BroadcastResponse;
}

export async function previewRecipientCount(audience: AudienceRequest): Promise<PreviewResponse> {
	const { data, error } = await client.POST('/v1/admin/notifications/broadcasts/preview', {
		body: audience
	});
	if (error) throw error;
	return data as PreviewResponse;
}

export async function searchBroadcasts(input: SortablePageInput): Promise<PageResource<BroadcastResponse>> {
	const { data, error } = await client.POST('/v1/admin/notifications/broadcasts/search', {
		body: input
	});
	if (error) throw error;
	return data as PageResource<BroadcastResponse>;
}

export async function searchBroadcastRecipients(
	id: number,
	input: SortablePageInput
): Promise<PageResource<RecipientResponse>> {
	const { data, error } = await client.POST('/v1/admin/notifications/broadcasts/{id}/recipients/search', {
		params: { path: { id } },
		body: input
	});
	if (error) throw error;
	return data as PageResource<RecipientResponse>;
}
