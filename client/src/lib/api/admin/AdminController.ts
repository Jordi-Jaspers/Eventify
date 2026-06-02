import { client } from '$lib/api/client.ts';
import type {
	AssignOwnerRequest,
	OrganizationMembershipResponse,
	TableSizeEntry,
	AdminCountsResponse,
	AdminGrowthResponse,
	AdminEventVolumeResponse,
	AdminEventStatsResponse,
	AdminStatsRequest
} from '$lib/api/models.ts';

export async function getAdminCounts(): Promise<AdminCountsResponse> {
	const { data, error } = await client.GET('/v1/admin/stats/counts');
	if (error) throw error;
	return data;
}

export async function getAdminGrowth(request: AdminStatsRequest): Promise<AdminGrowthResponse> {
	const { data, error } = await client.POST('/v1/admin/stats/growth', { body: request });
	if (error) throw error;
	return data;
}

export async function getEventVolume(request: AdminStatsRequest): Promise<AdminEventVolumeResponse> {
	const { data, error } = await client.POST('/v1/admin/stats/event-volume', { body: request });
	if (error) throw error;
	return data;
}

export async function getStorageStats(): Promise<TableSizeEntry[]> {
	const { data, error } = await client.GET('/v1/admin/stats/storage');
	if (error) throw error;
	return data ?? [];
}

export async function getEventStats(request: AdminStatsRequest): Promise<AdminEventStatsResponse> {
	const { data, error } = await client.POST('/v1/admin/stats/events', { body: request });
	if (error) throw error;
	return data;
}

export async function assignOrganizationOwner(orgId: number, request: AssignOwnerRequest): Promise<OrganizationMembershipResponse> {
	const { data, error } = await client.POST('/v1/admin/organization/{orgId}/owner', {
		params: { path: { orgId } },
		body: request
	});
	if (error) throw error;
	if (!data) throw new Error('No data returned from assign owner');
	return data as OrganizationMembershipResponse;
}
