import { client } from '$lib/api/client.ts';
import type {AdminStatsResponse} from "$lib/api/models.ts";

/**
 * Get admin dashboard statistics
 */
export async function getAdminStats(): Promise<AdminStatsResponse> {
	const { data, error } = await client.GET('/admin/stats');

	if (error) {
		throw error;
	}

	return data;
}
