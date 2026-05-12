import { SERVER_BASE_URL } from '$lib/config/constants';
import type { SortablePageInput, PageResource } from '$lib/api/models.ts';

export type BroadcastCategory = 'ANNOUNCEMENT' | 'SYSTEM' | 'ALERT';
export type AudienceType =
	| 'ALL_USERS'
	| 'ORGANIZATION'
	| 'ALL_ORGANIZATION_OWNERS'
	| 'USER'
	| 'GLOBAL_ROLE';

export interface BroadcastAudience {
	type: AudienceType;
	targetId?: number;
	role?: string;
}

export interface SendBroadcastRequest {
	category: BroadcastCategory;
	title: string;
	message: string;
	actionUrl?: string;
	actionLabel?: string;
	audience: BroadcastAudience;
}

export interface BroadcastResponse {
	id: number;
	category: BroadcastCategory;
	title: string;
	message: string;
	actionUrl?: string;
	actionLabel?: string;
	audienceType: AudienceType;
	audienceTargetId?: number;
	audienceRole?: string;
	audienceTargetName?: string;
	recipientCount: number;
	sentByEmail: string;
	createdAt: string;
}

export interface PreviewRecipientCountResponse {
	recipientCount: number;
}

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

export async function sendBroadcast(request: SendBroadcastRequest): Promise<BroadcastResponse> {
	return apiPost<BroadcastResponse>('/v1/admin/notifications/broadcasts', request);
}

export async function previewRecipientCount(
	audience: BroadcastAudience
): Promise<PreviewRecipientCountResponse> {
	return apiPost<PreviewRecipientCountResponse>(
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
