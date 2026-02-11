import type { EventSearchResponse, PageResourceEventSearchResponse } from '$lib/api/models';
import { searchUserEvents } from '$lib/api/event/UserEventController';
import { searchOrgEvents } from '$lib/api/event/OrganizationEventController';
import { handleError } from '$lib/utils/error-handler';

export function createEventService(channelId: number, orgId?: number) {
	let events = $state<EventSearchResponse[]>([]);
	let loading = $state(false);
	let initialLoad = $state(true);
	let error = $state<string | null>(null);
	let page = $state(0);
	let hasMore = $state(true);
	let totalEvents = $state(0);
	
	// Keep track of current request parameters to prevent race conditions or invalid state
	let currentStartTime = '';
	let currentEndTime = '';

	async function load(startTime: string, endTime: string, reset: boolean = false): Promise<void> {
		// Update current time window
		if (reset) {
			currentStartTime = startTime;
			currentEndTime = endTime;
		} else if (startTime !== currentStartTime || endTime !== currentEndTime) {
			// If parameters changed but reset wasn't requested, we should probably reset anyway
			// but strict adherence to the function signature means we just update our tracking
			currentStartTime = startTime;
			currentEndTime = endTime;
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
				response = await searchOrgEvents(orgId, channelId, startTime, endTime, page);
			} else {
				response = await searchUserEvents(channelId, startTime, endTime, page);
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
