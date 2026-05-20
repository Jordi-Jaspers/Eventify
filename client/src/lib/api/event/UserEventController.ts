import { client } from '$lib/api/client';
import type { components } from '$lib/types/api';
import { buildEventSearchBody } from './eventSearchUtils';
import type { EventSearchParams } from './eventSearchUtils';

type PageResourceEventSearchResponse = components['schemas']['PageResourceEventSearchResponse'];

export async function searchUserEvents(
	params: EventSearchParams,
	page: number = 0
): Promise<PageResourceEventSearchResponse> {
	const { data, error } = await client.POST('/v1/user/event/search', {
		body: buildEventSearchBody(params, page)
	});
	if (error) throw error;
	return data;
}
