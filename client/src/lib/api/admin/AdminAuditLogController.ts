import { client } from '$lib/api/client';
import type { AuditLogResponse, AuditLogStatsResponse, PageResource, SortablePageInput } from '$lib/api/models';

export async function getAuditLogStats(from: string, to: string): Promise<AuditLogStatsResponse> {
	const { data, error } = await client.GET('/v1/admin/audit-log/stats', {
		params: { query: { from, to } }
	});
	if (error) throw error;
	return data;
}

export async function searchAuditLog(input: SortablePageInput): Promise<PageResource<AuditLogResponse>> {
	const { data, error } = await client.POST('/v1/admin/audit-log/search', {
		body: input
	});
	if (error) throw error;
	return data as unknown as PageResource<AuditLogResponse>;
}
