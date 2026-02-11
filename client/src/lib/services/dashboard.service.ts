import { client } from '$lib/api/client';
import type { DashboardStatsResponse } from '$lib/api/models';

export type ErrorRateVariant = 'green' | 'yellow' | 'red';

/**
 * Get StatCard variant based on error rate thresholds.
 * - green: < 1%
 * - yellow: 1% - 5%
 * - red: >= 5%
 */
export function getErrorRateVariant(rate: number): ErrorRateVariant {
    if (rate < 1) return 'green';
    if (rate < 5) return 'yellow';
    return 'red';
}

export async function getDashboardStats(): Promise<DashboardStatsResponse> {
    const { data, error } = await client.GET('/v1/user/dashboard/stats');
    if (error) throw error;
    return data;
}

export async function getOrgDashboardStats(orgId: number): Promise<DashboardStatsResponse> {
    const { data, error } = await client.GET('/v1/organization/{orgId}/dashboard/stats', {
        params: { path: { orgId } }
    });
    if (error) throw error;
    return data;
}
