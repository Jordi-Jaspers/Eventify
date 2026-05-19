import { client } from '$lib/api/client.ts';
import type {
	AssignOwnerRequest,
	OrganizationMembershipResponse,
	TableSizeEntry,
	AdminCountsResponse,
	AdminGrowthResponse,
	AdminEventVolumeResponse,
	AdminEventStatsResponse
} from '$lib/api/models.ts';

/**
 * Get admin counts (organizations, users, channels)
 */
export async function getAdminCounts(): Promise<AdminCountsResponse> {
	const { data, error } = await client.GET('/v1/admin/stats/counts');
	if (error) throw error;
	return data;
}

/**
 * Get admin growth data over time
 */
export async function getAdminGrowth(days?: number): Promise<AdminGrowthResponse> {
	const { data, error } = await client.GET('/v1/admin/stats/growth', { params: { query: { days } } });
	if (error) throw error;
	return data;
}

/**
 * Get event volume data over time
 */
export async function getEventVolume(days?: number): Promise<AdminEventVolumeResponse> {
	const { data, error } = await client.GET('/v1/admin/stats/event-volume', { params: { query: { days } } });
	if (error) throw error;
	return data;
}

/**
 * Get storage statistics for all tracked database tables
 */
export async function getStorageStats(): Promise<TableSizeEntry[]> {
	const { data, error } = await client.GET('/v1/admin/stats/storage');
	if (error) throw error;
	return data ?? [];
}

/**
 * Get event statistics for admin dashboard
 */
export async function getEventStats(days?: number): Promise<AdminEventStatsResponse> {
	const { data, error } = await client.GET('/v1/admin/stats/events', { params: { query: { days } } });
	if (error) throw error;
	return data;
}

/**
 * Assign an owner to an organization (Admin only)
 * Used when an organization doesn't have an owner yet
 */
export async function assignOrganizationOwner(orgId: number, request: AssignOwnerRequest): Promise<OrganizationMembershipResponse> {
	const { data, error } = await client.POST('/v1/admin/organization/{orgId}/owner', {
		params: { path: { orgId } },
		body: request
	});
	if (error) throw error;
	if (!data) throw new Error('No data returned from assign owner');
	return data as OrganizationMembershipResponse;
}
