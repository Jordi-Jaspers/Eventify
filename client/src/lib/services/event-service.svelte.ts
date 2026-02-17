import type { EventSearchResponse, PageResourceEventSearchResponse } from '$lib/api/models';
import { searchUserEvents } from '$lib/api/event/UserEventController';
import { searchOrgEvents } from '$lib/api/event/OrganizationEventController';
import { handleError } from '$lib/utils/error-handler';

export function createEventService() {
	let events = $state<EventSearchResponse[]>([]);
	let loading = $state(false);
	let initialLoad = $state(true);
	let error = $state<string | null>(null);
	let page = $state(0);
	let hasMore = $state(true);
	let totalEvents = $state(0);
	
	// Keep track of current request parameters to prevent race conditions or invalid state
	let currentChannelId: number | undefined = undefined;
	let currentOrgId: number | undefined = undefined;
	let currentStartTime = '';
	let currentEndTime = '';
	let currentSeverity: string | undefined = undefined;

	async function load(
		channelId: number,
		startTime: string,
		endTime: string,
		reset: boolean = false,
		severity?: string,
		orgId?: number
	): Promise<void> {
		// Check if channel/org changed - force reset
		const channelChanged = channelId !== currentChannelId || orgId !== currentOrgId;
		if (channelChanged) {
			reset = true;
			currentChannelId = channelId;
			currentOrgId = orgId;
		}

		// Update current time window and severity
		if (reset) {
			currentStartTime = startTime;
			currentEndTime = endTime;
			currentSeverity = severity;
		} else if (startTime !== currentStartTime || endTime !== currentEndTime || severity !== currentSeverity) {
			currentStartTime = startTime;
			currentEndTime = endTime;
			currentSeverity = severity;
		}

		if (loading && !reset) return;
		if (!hasMore && !reset) return;

		loading = true;
		error = null;

		if (reset) {
			events = [];
			page = 0;
			hasMore = true;
			totalEvents = 0;
		}

		try {
			let response: PageResourceEventSearchResponse;

			if (orgId) {
				response = await searchOrgEvents(orgId, channelId, startTime, endTime, page, currentSeverity);
			} else {
				response = await searchUserEvents(channelId, startTime, endTime, page, currentSeverity);
			}

			const newEvents = response.content || [];
			events = reset ? newEvents : [...events, ...newEvents];
			totalEvents = response.totalElements ?? 0;

			hasMore = page < (response.totalPages ?? 1) - 1;
			if (hasMore) page++;

		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load events');
			console.error('Failed to load events', err);
			error = message;
		} finally {
			loading = false;
			initialLoad = false;
		}
	}

	return {
		get events() { return events; },
		get loading() { return loading; },
		get initialLoad() { return initialLoad; },
		get error() { return error; },
		get hasMore() { return hasMore; },
		get totalEvents() { return totalEvents; },
		load
	};
}
