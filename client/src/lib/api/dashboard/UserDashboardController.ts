import { client } from '../client';
import type { UserDashboardResponse } from '$lib/api/models';

export async function getUserDashboard(): Promise<UserDashboardResponse> {
	const { data, error } = await client.GET('/v1/user/dashboard');
	if (error) throw error;
	return data;
}
