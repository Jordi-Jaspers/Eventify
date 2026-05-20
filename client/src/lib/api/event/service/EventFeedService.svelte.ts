import type { EventSearchResponse, PageResourceEventSearchResponse } from '$lib/api/models';
import type { EventFeedSeverity, EventFeedChannel } from '$lib/components/monitor/types';
import { searchUserEvents } from '$lib/api/event/UserEventController';
import { searchOrgEvents } from '$lib/api/event/OrganizationEventController';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

export type { EventFeedSeverity, EventFeedChannel };

const PAGE_SIZE: number = 20;
const POLL_INTERVAL_MS: number = 10_000;

export function createEventFeedService(orgId?: number) {
	let events = $state<EventSearchResponse[]>([]);
	let loading = $state(false);
	let initialLoad = $state(true);
	let error = $state<string | null>(null);
	let page = $state(0);
	let hasMore = $state(true);
	let totalEvents = $state(0);

	// Filters
	let selectedChannelIds = $state<number[]>([]);
	let selectedSeverities = $state<EventFeedSeverity[]>([]);

	// Polling
	let newEventCount = $state(0);
	let lastSeenTimestamp = $state<string | null>(null);
	let pollTimer: ReturnType<typeof setInterval> | null = null;

	// Current params
	let currentChannelIds: number[] = [];
	let currentStartTime: string = '';
	let currentEndTime: string = '';

	async function load(
		channelIds: number[],
		startTime: string,
		endTime: string,
		reset: boolean = false
	): Promise<void> {
		const paramsChanged =
			JSON.stringify(channelIds) !== JSON.stringify(currentChannelIds) ||
			startTime !== currentStartTime ||
			endTime !== currentEndTime;

		if (paramsChanged) reset = true;

		if (loading && !reset) return;
		if (!hasMore && !reset) return;

		if (reset) {
			initialLoad = true;
			currentChannelIds = channelIds;
			currentStartTime = startTime;
			currentEndTime = endTime;
			events = [];
			page = 0;
			hasMore = true;
			totalEvents = 0;
			newEventCount = 0;
			lastSeenTimestamp = null;
		}

		loading = true;
		error = null;

		try {
			const effectiveChannelIds: number[] =
				selectedChannelIds.length > 0 ? selectedChannelIds : channelIds;
			const params = {
				channelIds: effectiveChannelIds,
				startTime,
				endTime,
				severities: selectedSeverities
			};

			const response: PageResourceEventSearchResponse = orgId
				? await searchOrgEvents(orgId, params, page)
				: await searchUserEvents(params, page);

			const newEvents: EventSearchResponse[] = response.content ?? [];
			events = reset ? newEvents : [...events, ...newEvents];
			totalEvents = response.totalElements ?? 0;

			if (newEvents.length > 0 && reset) {
				lastSeenTimestamp = newEvents[0].timestamp;
			}

			hasMore = page < (response.totalPages ?? 1) - 1;
			if (hasMore) page++;
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load events');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
			initialLoad = false;
		}
	}

	async function loadMore(): Promise<void> {
		await load(currentChannelIds, currentStartTime, currentEndTime, false);
	}

	async function pollForNew(): Promise<void> {
		if (!lastSeenTimestamp || currentChannelIds.length === 0) return;

		try {
			const effectiveChannelIds: number[] =
				selectedChannelIds.length > 0 ? selectedChannelIds : currentChannelIds;
			const params = {
				channelIds: effectiveChannelIds,
				startTime: currentStartTime,
				endTime: currentEndTime,
				severities: selectedSeverities,
				afterTimestamp: lastSeenTimestamp
			};

			const response: PageResourceEventSearchResponse = orgId
				? await searchOrgEvents(orgId, params, 0)
				: await searchUserEvents(params, 0);

			const incoming: EventSearchResponse[] = (response.content ?? []).filter(
				(e: EventSearchResponse) => e.timestamp > (lastSeenTimestamp ?? '')
			);

			if (incoming.length > 0) {
				lastSeenTimestamp = incoming[0].timestamp;
				newEventCount += incoming.length;
				events = [...incoming, ...events];
				totalEvents += incoming.length;
			}
		} catch {
			// Silent poll failure
		}
	}

	function startPolling(): void {
		if (pollTimer) clearInterval(pollTimer);
		pollTimer = setInterval(() => {
			pollForNew();
		}, POLL_INTERVAL_MS);
	}

	function stopPolling(): void {
		if (pollTimer) {
			clearInterval(pollTimer);
			pollTimer = null;
		}
	}

	function startLive(): void {
		startPolling();
	}

	function stopLive(): void {
		stopPolling();
	}

	function toggleLive(): void {
		if (pollTimer) stopLive();
		else startLive();
	}

	function clearNewEventBanner(): void {
		newEventCount = 0;
	}

	function clearFilters(): void {
		selectedChannelIds = [];
		selectedSeverities = [];
	}

	function cleanup(): void {
		stopPolling();
	}

	return {
		get events(): EventSearchResponse[] {
			return events;
		},
		get loading(): boolean {
			return loading;
		},
		get initialLoad(): boolean {
			return initialLoad;
		},
		get error(): string | null {
			return error;
		},
		get hasMore(): boolean {
			return hasMore;
		},
		get totalEvents(): number {
			return totalEvents;
		},
		get newEventCount(): number {
			return newEventCount;
		},
		get selectedChannelIds(): number[] {
			return selectedChannelIds;
		},
		set selectedChannelIds(v: number[]) {
			selectedChannelIds = v;
		},
		get selectedSeverities(): EventFeedSeverity[] {
			return selectedSeverities;
		},
		set selectedSeverities(v: EventFeedSeverity[]) {
			selectedSeverities = v;
		},
		load,
		loadMore,
		clearNewEventBanner,
		clearFilters,
		cleanup,
		startPolling,
		stopPolling
	};
}

export type EventFeedService = ReturnType<typeof createEventFeedService>;
