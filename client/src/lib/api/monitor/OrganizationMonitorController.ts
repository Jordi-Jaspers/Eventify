import { client } from '../client';
import type { MonitorRequest, MonitorResponse } from '$lib/api/models';

/**
 * Get monitor timeline data for an organization watchlist
 */
export async function getOrganizationMonitor(
	orgId: number,
	request: MonitorRequest
): Promise<MonitorResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/monitor', {
		params: { path: { orgId } },
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to get monitor data');
	}
	return data;
}
