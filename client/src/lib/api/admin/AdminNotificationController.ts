import { SERVER_BASE_URL } from '$lib/config/constants';
import type {
	SortablePageInput,
	PageResource,
	BroadcastResponse,
	RecipientResponse,
	BroadcastCategory,
	AudienceType,
	AudienceRequest,
	CreateBroadcastRequest,
	PreviewResponse
} from '$lib/api/models';

export type { BroadcastResponse, RecipientResponse, BroadcastCategory, AudienceType, AudienceRequest, CreateBroadcastRequest, PreviewResponse };

async function apiPost<T>(path: string, body: unknown): Promise<T> {
	const response: Response = await fetch(`${SERVER_BASE_URL}${path}`, {
		method: 'POST',
		credentials: 'include',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(body)
	});
	if (!response.ok) {
		const error: unknown = await response.json().catch(() => ({ message: response.statusText }));
		throw error;
	}
	if (response.status === 204) return undefined as unknown as T;
	return response.json() as Promise<T>;
}

export async function sendBroadcast(request: CreateBroadcastRequest): Promise<BroadcastResponse> {
	return apiPost<BroadcastResponse>('/v1/admin/notifications/broadcasts', request);
}

export async function previewRecipientCount(
	audience: AudienceRequest
): Promise<PreviewResponse> {
	return apiPost<PreviewResponse>(
		'/v1/admin/notifications/broadcasts/preview',
		audience
	);
}

export async function searchBroadcasts(
	input: SortablePageInput
): Promise<PageResource<BroadcastResponse>> {
	return apiPost<PageResource<BroadcastResponse>>(
		'/v1/admin/notifications/broadcasts/search',
		input
	);
}

export async function searchBroadcastRecipients(
	id: number,
	input: SortablePageInput
): Promise<PageResource<RecipientResponse>> {
	return apiPost<PageResource<RecipientResponse>>(
		`/v1/admin/notifications/broadcasts/${id}/recipients/search`,
		input
	);
}
