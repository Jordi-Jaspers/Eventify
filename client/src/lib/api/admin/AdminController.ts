import { client } from '$lib/api/client.ts';
import type {AdminStatsResponse, AssignOwnerRequest, OrganizationMembershipResponse, TableSizeEntry} from "$lib/api/models.ts";
import type { components } from '$lib/types/api';

export type AdminEventStatsResponse = components['schemas']['AdminEventStatsResponse'];

/**
 * Get admin dashboard statistics
 */
export async function getAdminStats(days?: number): Promise<AdminStatsResponse> {
	const { data, error } = await client.GET('/v1/admin/stats', { params: { query: { days } } });
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
