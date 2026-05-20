import type { components } from '$lib/types/api';

type SearchInput = components['schemas']['SearchInput'];

export interface EventSearchParams {
	channelId?: number;
	channelIds?: number[];
	startTime: string;
	endTime: string;
	severities?: string[];
	severity?: string;
	afterTimestamp?: string;
}

export function buildEventSearchInputs(params: EventSearchParams): SearchInput[] {
	const inputs: SearchInput[] = [];

	if (params.channelIds?.length) {
		inputs.push({ fieldName: 'channelIds', textValueList: params.channelIds.map(String) });
	} else if (params.channelId != null) {
		inputs.push({ fieldName: 'channelId', textValue: String(params.channelId) });
	}

	const fromDate: string = params.afterTimestamp ?? params.startTime;
	inputs.push({ fieldName: 'timestamp', fromDateValue: fromDate, toDateValue: params.endTime });

	if (params.severities?.length) {
		for (const sev of params.severities) {
			inputs.push({ fieldName: 'severity', textValue: sev });
		}
	} else if (params.severity) {
		inputs.push({ fieldName: 'severity', textValue: params.severity });
	}

	return inputs;
}

export function buildEventSearchBody(
	params: EventSearchParams,
	page: number = 0,
	pageSize: number = 20
): {
	pageNumber: number;
	pageSize: number;
	sortOrder: { name: string; direction: 'DESC' }[];
	searchInputs: SearchInput[];
} {
	return {
		pageNumber: page,
		pageSize,
		sortOrder: [{ name: 'timestamp', direction: 'DESC' as const }],
		searchInputs: buildEventSearchInputs(params)
	};
}
