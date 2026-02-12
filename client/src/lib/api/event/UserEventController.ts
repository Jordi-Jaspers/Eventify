import { client } from '$lib/api/client';
import type { components } from '$lib/types/api';

type PageResourceEventSearchResponse = components['schemas']['PageResourceEventSearchResponse'];

export async function searchUserEvents(
	channelId: number,
	startTime: string,
	endTime: string,
	page: number = 0,
	severity?: string
): Promise<PageResourceEventSearchResponse> {
	const searchInputs: components['schemas']['SearchInput'][] = [
		{ fieldName: 'channelId', textValue: String(channelId) },
		{ fieldName: 'timestamp', fromDateValue: startTime, toDateValue: endTime }
	];

	// Add severity filter if provided
	if (severity) {
		searchInputs.push({ fieldName: 'severity', textValue: severity });
	}

	const { data, error } = await client.POST('/v1/user/event/search', {
		body: {
			pageNumber: page,
			pageSize: 20,
			sortOrder: [{ name: 'timestamp', direction: 'DESC' }],
			searchInputs
		}
	});
	if (error) throw error;
	return data;
}
