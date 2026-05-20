<script lang="ts">
	import { onDestroy, untrack } from 'svelte';
	import { ChevronDown, ChevronUp, Radio, AlertCircle, Inbox, Clock, X } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { MultiEnumFilter } from '$lib/components/data-table/filters';
	import {
		createEventFeedService,
		type EventFeedSeverity,
		type EventFeedChannel
	} from '$lib/api/event/service/EventFeedService.svelte';
	import { getSeverityColors } from '$lib/components/monitor/types';
	import { formatRelativeTime } from '$lib/utils/date';
	import type { EventSearchResponse, Severity } from '$lib/api/models';

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

	// Expanded messages map: event key -> boolean
	let expandedMessages: Record<string, boolean> = $state({});

	// Scroll container ref for fixed-height scroll + "new events" banner
	let scrollContainer: HTMLDivElement | undefined = $state(undefined);
	let sentinel: HTMLDivElement | undefined = $state(undefined);
	let isScrolledDown: boolean = $state(false);
	let observer: IntersectionObserver | undefined;

	// Open filter popover: 'channels' | 'severity' | null
	let openFilter: string | null = $state(null);

	const SEVERITY_OPTIONS: { value: string; label: string }[] = [
		{ value: 'CRITICAL', label: 'Critical' },
		{ value: 'WARNING', label: 'Warning' },
		{ value: 'OK', label: 'Ok' },
		{ value: 'NO_DATA', label: 'No Data' }
	];

	const channelIds: number[] = $derived(channels.map((c: EventFeedChannel) => c.id));

	const hasFilters: boolean = $derived(
		service.selectedChannelIds.length > 0 || service.selectedSeverities.length > 0
	);

	const selectedChannelCount: number = $derived(service.selectedChannelIds.length);
	const selectedSeverityCount: number = $derived(service.selectedSeverities.length);

	// IntersectionObserver with root: scrollContainer (like EventsList.svelte)
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

	onDestroy(() => {
		service.cleanup();
		observer?.disconnect();
	});

	// Start/stop polling based on isLive prop
	$effect(() => {
		if (isLive) {
			service.startPolling();
		} else {
			service.stopPolling();
		}
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

	function getChannelName(id: number): string {
		return channels.find((c: EventFeedChannel) => c.id === id)?.name ?? String(id);
	}

	function getSeverityLabel(sev: string): string {
		return SEVERITY_OPTIONS.find((o) => o.value === sev)?.label ?? sev;
	}

	function getEventKey(event: EventSearchResponse, idx: number): string {
		return `${event.timestamp}-${idx}`;
	}
</script>

<div class="flex flex-col gap-3">
	<!-- Click-outside overlay for filter dropdowns -->
	{#if openFilter}
		<button
			type="button"
			class="fixed inset-0 z-[99] cursor-default"
			aria-label="Close filter"
			onclick={() => { openFilter = null; }}
		></button>
	{/if}

	<!-- Filter bar (outside card, matching DataTable pattern) -->
	<div class="relative z-[100] rounded-xl border border-border/50 bg-card/50 backdrop-blur-xl shadow-lg mb-4 px-4 py-3">
		<div class="space-y-2">
			<div class="flex items-center gap-2 flex-wrap">
				<!-- Channels filter button -->
				{#if channels.length > 0}
					<div class="relative z-50">
						<button
							onclick={() => { openFilter = openFilter === 'channels' ? null : 'channels'; }}
							class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap
								{selectedChannelCount > 0
									? 'bg-primary/10 border-primary/30 text-primary'
									: openFilter === 'channels'
										? 'bg-background/50 border-primary text-primary'
										: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
						>
							Channels
							{#if selectedChannelCount > 0}
								<span class="bg-primary text-primary-foreground rounded-full px-1.5 py-0 text-[10px] font-medium">
									{selectedChannelCount}
								</span>
							{/if}
							<ChevronDown class="h-3 w-3 transition-transform {openFilter === 'channels' ? 'rotate-180' : ''}" />
						</button>

						{#if openFilter === 'channels'}
							<!-- svelte-ignore a11y_no_static_element_interactions -->
							<div
								class="absolute top-full left-0 mt-1 z-50 min-w-[180px] rounded-lg border border-border/50 bg-card shadow-xl p-2"
								onmousedown={(e: MouseEvent) => e.stopPropagation()}
							>
								<div class="flex flex-col gap-1">
									{#each channels as ch (ch.id)}
										<label class="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-muted/40 cursor-pointer text-sm">
											<Checkbox
												checked={service.selectedChannelIds.includes(ch.id)}
												onCheckedChange={() => toggleChannelFilter(ch.id)}
											/>
											<span class="truncate">{ch.name}</span>
										</label>
									{/each}
								</div>
							</div>
						{/if}
					</div>
				{/if}

				<!-- Severity filter button -->
				<div class="relative z-50">
					<button
						onclick={() => { openFilter = openFilter === 'severity' ? null : 'severity'; }}
						class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap
							{selectedSeverityCount > 0
								? 'bg-primary/10 border-primary/30 text-primary'
								: openFilter === 'severity'
									? 'bg-background/50 border-primary text-primary'
									: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
					>
						Severity
						{#if selectedSeverityCount > 0}
							<span class="bg-primary text-primary-foreground rounded-full px-1.5 py-0 text-[10px] font-medium">
								{selectedSeverityCount}
							</span>
						{/if}
						<ChevronDown class="h-3 w-3 transition-transform {openFilter === 'severity' ? 'rotate-180' : ''}" />
					</button>

					{#if openFilter === 'severity'}
						<!-- svelte-ignore a11y_no_static_element_interactions -->
						<div
							class="absolute top-full left-0 mt-1 z-50 min-w-[220px] rounded-lg border border-border/50 bg-card shadow-xl p-3"
							onmousedown={(e: MouseEvent) => e.stopPropagation()}
						>
							<MultiEnumFilter
								value={service.selectedSeverities}
								options={SEVERITY_OPTIONS}
								onChange={handleSeverityChange}
							/>
						</div>
					{/if}
				</div>

			<!-- Live toggle (right-aligned) -->
			<button
				onclick={onToggleLive}
				class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors ml-auto
						{isLive
							? 'bg-green-500/10 text-green-500 border-green-500/30'
							: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
					title="Toggle live mode"
				>
					{#if isLive}
						<span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
					{:else}
						<Radio class="h-3 w-3" />
					{/if}
				Live
			</button>

			<!-- Clear all -->
			{#if hasFilters}
				<Button
					variant="ghost"
					size="sm"
					onclick={clearFilters}
					class="text-xs text-muted-foreground hover:text-destructive h-9 px-2"
				>
					<X class="h-3 w-3 mr-1" />
					Clear
				</Button>
			{/if}
		</div>

			<!-- Active filter chips -->
			{#if hasFilters}
				<div class="flex items-center gap-1.5 flex-wrap">
					{#each service.selectedChannelIds as id (id)}
						<span class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md bg-primary/10 text-primary border border-primary/20">
							{getChannelName(id)}
							<button
								onclick={() => toggleChannelFilter(id)}
								class="hover:bg-primary/20 rounded-full p-0.5"
								aria-label="Remove channel filter"
							>
								<X class="h-2.5 w-2.5" />
							</button>
						</span>
					{/each}
					{#each service.selectedSeverities as sev (sev)}
						<span class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md bg-primary/10 text-primary border border-primary/20">
							{getSeverityLabel(sev)}
							<button
								onclick={() => handleSeverityChange(service.selectedSeverities.filter((s: EventFeedSeverity) => s !== sev))}
								class="hover:bg-primary/20 rounded-full p-0.5"
								aria-label="Remove severity filter"
							>
								<X class="h-2.5 w-2.5" />
							</button>
						</span>
					{/each}
				</div>
			{/if}
		</div>
	</div>

	<!-- Feed content card -->
	<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg overflow-hidden">
		<CardContent class="p-4">
			<!-- New events banner — only when scrolled away from top -->
			{#if service.newEventCount > 0 && isScrolledDown}
				<button
					onclick={scrollToTop}
					class="w-full mb-3 flex items-center justify-center gap-2 px-4 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs shadow-lg animate-fade-in"
				>
					<ChevronUp class="h-3 w-3" />
					↑ {service.newEventCount} new event{service.newEventCount > 1 ? 's' : ''}
				</button>
			{/if}

			<!-- Event count -->
			{#if !service.initialLoad && service.totalEvents > 0}
				<p class="text-xs text-muted-foreground mb-3">
					Showing {service.events.length} of {service.totalEvents} events
				</p>
			{/if}

			<!-- Fixed-height scroll container -->
			<div
				bind:this={scrollContainer}
				onscroll={handleScroll}
				class="overflow-y-auto h-[500px] relative"
			>
				<!-- Skeleton loading — shimmer animation -->
				{#if service.initialLoad && service.loading}
					<style>
						@keyframes shimmer {
							0% { background-position: -200% 0; }
							100% { background-position: 200% 0; }
						}
						.shimmer {
							background: linear-gradient(90deg, transparent 25%, hsl(var(--muted)/0.4) 50%, transparent 75%);
							background-size: 200% 100%;
							animation: shimmer 1.5s infinite;
						}
					</style>
					<div class="p-4">
						<div class="relative pl-6 border-l-2 border-border/40 space-y-4">
							{#each Array(5) as _, i (i)}
								<div class="relative">
									<div class="absolute -left-[22px] -translate-x-1/2 top-[3px] h-3 w-3 rounded-full bg-muted/60 border-2 border-background"></div>
									<div class="pl-2 space-y-1.5">
										<div class="rounded-lg border border-border/50 bg-card/30 h-3 w-24 shimmer"></div>
										<div class="rounded-lg border border-border/50 bg-card/30 h-3.5 w-1/3 shimmer"></div>
										<div class="rounded-lg border border-border/50 bg-card/30 h-3 w-2/3 shimmer"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>

				<!-- Empty: no channels -->
				{:else if channels.length === 0}
					<div class="flex flex-col items-center justify-center h-full py-12">
						<div class="relative">
							<div class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"></div>
							<div class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50">
								<Inbox class="w-12 h-12 text-primary" />
							</div>
						</div>
						<h3 class="mt-6 text-lg font-semibold">No channels selected</h3>
						<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
							Add channels to start seeing events in the feed
						</p>
					</div>

				<!-- Empty: no events matching filters -->
				{:else if service.events.length === 0 && !service.loading && hasFilters}
					<div class="flex flex-col items-center justify-center h-full py-12">
						<div class="relative">
							<div class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"></div>
							<div class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50">
								<AlertCircle class="w-12 h-12 text-primary" />
							</div>
						</div>
						<h3 class="mt-6 text-lg font-semibold">No events found</h3>
						<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
							No events match the current filters
						</p>
						<button onclick={clearFilters} class="mt-4 text-xs text-primary hover:underline">
							Clear filters
						</button>
					</div>

				<!-- Empty: no events in time range -->
				{:else if service.events.length === 0 && !service.loading}
					<div class="flex flex-col items-center justify-center h-full py-12">
						<div class="relative">
							<div class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"></div>
							<div class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50">
								<Clock class="w-12 h-12 text-primary" />
							</div>
						</div>
						<h3 class="mt-6 text-lg font-semibold">No events in time range</h3>
						<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
							This time period has no recorded events
						</p>
					</div>

				<!-- Events timeline -->
				{:else}
					<div class="p-4">
						<div class="relative pl-6 border-l-2 border-border/40 space-y-4">
							{#each service.events as event, idx (getEventKey(event, idx))}
								{@const colors = getSeverityColors(event.severity as Severity)}
								{@const key = getEventKey(event, idx)}
								<div class="relative group animate-fade-in">
									<!-- Timeline dot -->
									<div class="absolute -left-[22px] -translate-x-1/2 top-[3px] h-3 w-3 rounded-full {colors.bg} border-2 border-background shadow-sm transition-transform group-hover:scale-125"></div>

									<div class="pl-2">
										<!-- Timestamp + channel pill -->
										<div class="flex items-center gap-2 mb-0.5">
											<span class="text-xs text-muted-foreground font-mono">
												{formatRelativeTime(event.timestamp)}
											</span>
											<button
												onclick={() => {
													const ch: EventFeedChannel | undefined = channels.find(
														(c: EventFeedChannel) => c.name === event.channelName
													);
													if (ch) toggleChannelFilter(ch.id);
												}}
												class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs border border-border/40 bg-muted/30 hover:bg-muted/60 transition-colors text-muted-foreground"
												title="Filter by channel"
											>
												{event.channelName}
											</button>
										</div>

										<!-- Title -->
										<p class="text-sm font-semibold leading-tight">{event.title}</p>

										<!-- Message (expandable) -->
										{#if event.message}
											<p class="text-xs text-muted-foreground mt-0.5 {expandedMessages[key] ? '' : 'line-clamp-2'}">
												{event.message}
											</p>
											{#if event.message.length > 120}
												<button
													onclick={() => toggleExpand(key)}
													class="text-xs text-primary hover:underline mt-0.5"
												>
													{expandedMessages[key] ? 'Show less' : 'Show more'}
												</button>
											{/if}
										{/if}
									</div>
								</div>
							{/each}
						</div>

						<!-- Infinite scroll sentinel -->
						<div bind:this={sentinel} class="py-6 flex justify-center">
							{#if service.loading && !service.initialLoad}
								<div class="flex items-center gap-2 text-muted-foreground">
									<div class="flex gap-1">
										{#each Array(3) as _, i (i)}
											<div
												class="w-1.5 h-1.5 rounded-full bg-muted-foreground/40 animate-pulse"
												style="animation-delay: {i * 150}ms"
											></div>
										{/each}
									</div>
									<span class="text-sm">Loading more...</span>
								</div>
							{:else if !service.hasMore && service.events.length > 0}
								<span class="text-xs text-muted-foreground/50">All events loaded</span>
							{/if}
						</div>
					</div>
				{/if}

				<!-- Error overlay -->
				{#if service.error}
					<div class="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm">
						<div class="text-center p-6">
							<p class="text-destructive font-medium mb-2">Failed to load events</p>
							<p class="text-sm text-muted-foreground mb-4">{service.error}</p>
							<button
								class="text-sm text-primary hover:underline"
								onclick={reload}
							>
								Try again
							</button>
						</div>
					</div>
				{/if}
			</div>
		</CardContent>
	</Card>
</div>

