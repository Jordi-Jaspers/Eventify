import { client } from '../client';
import type { MonitorRequest, MonitorResponse } from '$lib/api/models';

/**
 * Get monitor timeline data for a watchlist
 */
export async function getUserMonitor(request: MonitorRequest): Promise<MonitorResponse> {
	const { data, error } = await client.POST('/v1/user/monitor', {
		body: request
	});
	if (error || !data) {
		throw error ?? new Error('Failed to get monitor data');
	}
	return data;
}
