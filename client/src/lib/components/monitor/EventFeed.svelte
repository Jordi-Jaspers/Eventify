<script lang="ts">
	import { onDestroy, untrack } from 'svelte';
	import {
		createEventFeedService,
		type EventFeedSeverity,
		type EventFeedChannel
	} from '$lib/api/event/service/EventFeedService.svelte';
	import EventFeedFilters from './EventFeedFilters.svelte';
	import EventFeedList from './EventFeedList.svelte';

	interface Props {
		channels: EventFeedChannel[];
		startTime: string;
		endTime: string;
		orgId?: number;
		isLive: boolean;
		onToggleLive: () => void;
	}

	let { channels, startTime, endTime, orgId, isLive, onToggleLive }: Props = $props();

	const service = createEventFeedService(untrack(() => orgId));

	let expandedMessages: Record<string, boolean> = $state({});
	let scrollContainer: HTMLDivElement | undefined = $state(undefined);
	let sentinel: HTMLDivElement | undefined = $state(undefined);
	let isScrolledDown: boolean = $state(false);
	let observer: IntersectionObserver | undefined;

	const channelIds: number[] = $derived(channels.map((c: EventFeedChannel) => c.id));
	const hasFilters: boolean = $derived(
		service.selectedChannelIds.length > 0 || service.selectedSeverities.length > 0
	);

	// IntersectionObserver for infinite scroll
	$effect(() => {
		if (!scrollContainer || !sentinel) return;

		observer?.disconnect();
		observer = new IntersectionObserver(
			(entries: IntersectionObserverEntry[]) => {
				if (entries[0].isIntersecting && service.hasMore && !service.loading) {
					service.loadMore();
				}
			},
			{ root: scrollContainer, threshold: 0.1 }
		);
		observer.observe(sentinel);

		return () => observer?.disconnect();
	});

	// Reload when time range or channels change
	$effect(() => {
		const ids: number[] = channelIds;
		const start: string = startTime;
		const end: string = endTime;
		if (ids.length > 0 && start && end) {
			untrack(() => service.load(ids, start, end, true));
		}
	});

	// Start/stop polling based on isLive prop
	$effect(() => {
		if (isLive) {
			service.startPolling();
		} else {
			service.stopPolling();
		}
	});

	onDestroy(() => {
		service.cleanup();
		observer?.disconnect();
	});

	function toggleChannelFilter(id: number): void {
		const current: number[] = service.selectedChannelIds;
		if (current.includes(id)) {
			service.selectedChannelIds = current.filter((c: number) => c !== id);
		} else {
			service.selectedChannelIds = [...current, id];
		}
		reload();
	}

	function handleSeverityChange(values: string[]): void {
		service.selectedSeverities = values as EventFeedSeverity[];
		reload();
	}

	function clearFilters(): void {
		service.clearFilters();
		reload();
	}

	function reload(): void {
		service.load(channelIds, startTime, endTime, true);
	}

	function toggleExpand(key: string): void {
		expandedMessages[key] = !expandedMessages[key];
	}

	function handleScroll(): void {
		if (!scrollContainer) return;
		const { scrollTop, scrollHeight, clientHeight } = scrollContainer;
		isScrolledDown = scrollTop < scrollHeight - clientHeight - 100;
	}

	function scrollToTop(): void {
		scrollContainer?.scrollTo({ top: 0, behavior: 'smooth' });
		service.clearNewEventBanner();
	}
</script>

<div class="flex flex-col gap-3">
	<EventFeedFilters
		{channels}
		selectedChannelIds={service.selectedChannelIds}
		selectedSeverities={service.selectedSeverities}
		{isLive}
		{hasFilters}
		onToggleChannel={toggleChannelFilter}
		onSeverityChange={handleSeverityChange}
		onClearFilters={clearFilters}
		{onToggleLive}
	/>

	<EventFeedList
		events={service.events}
		loading={service.loading}
		initialLoad={service.initialLoad}
		hasMore={service.hasMore}
		totalEvents={service.totalEvents}
		newEventCount={service.newEventCount}
		error={service.error}
		{hasFilters}
		{channels}
		bind:scrollContainer
		bind:sentinel
		{isScrolledDown}
		{expandedMessages}
		onScrollToTop={scrollToTop}
		onToggleExpand={toggleExpand}
		onToggleChannelFilter={toggleChannelFilter}
		onClearFilters={clearFilters}
		onReload={reload}
		onScroll={handleScroll}
	/>
</div>
