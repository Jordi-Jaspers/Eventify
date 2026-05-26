import { client } from '$lib/api/client';
import type { OrgTimelineResponse, OrgSummaryResponse, OrgApiKeyStatsResponse } from '$lib/api/models';

export async function getOrganizationTimeline(orgId: number, days: number = 30): Promise<OrgTimelineResponse> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/stats/timeline', {
		params: { path: { orgId }, query: { days } }
	});
	if (error) throw error;
	return data;
}

export async function getOrganizationSummary(orgId: number, days: number = 30): Promise<OrgSummaryResponse> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/stats/summary', {
		params: { path: { orgId }, query: { days } }
	});
	if (error) throw error;
	return data;
}

export async function getOrganizationApiKeyStats(orgId: number): Promise<OrgApiKeyStatsResponse> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/stats/api-keys', {
		params: { path: { orgId } }
	});
	if (error) throw error;
	return data;
}
