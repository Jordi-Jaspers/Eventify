import { client } from '$lib/api/client';
import type { components } from '$lib/types/api';

type PageResourceEventSearchResponse = components['schemas']['PageResourceEventSearchResponse'];

export async function searchOrgEvents(
	orgId: number,
	channelId: number,
	startTime: string,
	endTime: string,
	page: number = 0
): Promise<PageResourceEventSearchResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/events/search', {
		params: {
			path: { orgId }
		},
		body: {
			pageNumber: page,
			pageSize: 20,
			sortOrder: [{ name: 'timestamp', direction: 'DESC' }],
			searchInputs: [
				{ fieldName: 'channelId', textValue: String(channelId) },
				{ fieldName: 'timestamp', fromDateValue: startTime, toDateValue: endTime }
			]
		}
	});
	if (error) throw error;
	return data;
}
