import { client } from '$lib/api/client';
import type { components } from '$lib/types/api';
import { buildEventSearchBody } from './eventSearchUtils';
import type { EventSearchParams } from './eventSearchUtils';

type PageResourceEventSearchResponse = components['schemas']['PageResourceEventSearchResponse'];

export async function searchOrgEvents(
	orgId: number,
	params: EventSearchParams,
	page: number = 0
): Promise<PageResourceEventSearchResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/event/search', {
		params: { path: { orgId } },
		body: buildEventSearchBody(params, page)
	});
	if (error) throw error;
	return data;
}
