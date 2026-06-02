import { client } from '$lib/api/client';
import type { OrgStatsRequest, OrgTimelineResponse, OrgSummaryResponse, OrgApiKeyStatsResponse } from '$lib/api/models';

export async function getOrganizationTimeline(orgId: number, request: OrgStatsRequest): Promise<OrgTimelineResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/stats/timeline', {
		params: { path: { orgId } },
		body: request
	});
	if (error) throw error;
	return data;
}

export async function getOrganizationSummary(orgId: number, request: OrgStatsRequest): Promise<OrgSummaryResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/stats/summary', {
		params: { path: { orgId } },
		body: request
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
