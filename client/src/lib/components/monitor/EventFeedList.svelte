<script lang="ts">
	import { ChevronUp, AlertCircle, Inbox, Clock } from '@lucide/svelte';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { getSeverityColors } from '$lib/components/monitor/types';
	import { formatRelativeTime } from '$lib/utils/date';
	import type { EventSearchResponse, Severity } from '$lib/api/models';
	import type { EventFeedChannel } from '$lib/api/event/service/EventFeedService.svelte';

	interface Props {
		events: EventSearchResponse[];
		loading: boolean;
		initialLoad: boolean;
		hasMore: boolean;
		totalEvents: number;
		newEventCount: number;
		error: string | null;
		hasFilters: boolean;
		channels: EventFeedChannel[];
		scrollContainer: HTMLDivElement | undefined;
		sentinel: HTMLDivElement | undefined;
		isScrolledDown: boolean;
		expandedMessages: Record<string, boolean>;
		onScrollToTop: () => void;
		onToggleExpand: (key: string) => void;
		onToggleChannelFilter: (id: number) => void;
		onClearFilters: () => void;
		onReload: () => void;
		onScroll: () => void;
	}

	let {
		events,
		loading,
		initialLoad,
		hasMore,
		totalEvents,
		newEventCount,
		error,
		hasFilters,
		channels,
		scrollContainer = $bindable(),
		sentinel = $bindable(),
		isScrolledDown,
		expandedMessages,
		onScrollToTop,
		onToggleExpand,
		onToggleChannelFilter,
		onClearFilters,
		onReload,
		onScroll
	}: Props = $props();

	function getEventKey(event: EventSearchResponse, idx: number): string {
		return `${event.timestamp}-${idx}`;
	}
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg overflow-hidden">
	<CardContent class="p-4">
		<!-- New events banner -->
		{#if newEventCount > 0 && isScrolledDown}
			<button
				onclick={onScrollToTop}
				class="w-full mb-3 flex items-center justify-center gap-2 px-4 py-1.5 rounded-lg bg-primary text-primary-foreground text-xs shadow-lg animate-fade-in"
			>
				<ChevronUp class="h-3 w-3" />
				↑ {newEventCount} new event{newEventCount > 1 ? 's' : ''}
			</button>
		{/if}

		<!-- Event count -->
		{#if !initialLoad && totalEvents > 0}
			<p class="text-xs text-muted-foreground mb-3">
				Showing {events.length} of {totalEvents} events
			</p>
		{/if}

		<!-- Fixed-height scroll container -->
		<div
			bind:this={scrollContainer}
			onscroll={onScroll}
			class="overflow-y-auto h-[500px] relative"
		>
			<!-- Skeleton loading -->
			{#if initialLoad && loading}
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
			{:else if events.length === 0 && !loading && hasFilters}
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
					<button onclick={onClearFilters} class="mt-4 text-xs text-primary hover:underline">
						Clear filters
					</button>
				</div>

			<!-- Empty: no events in time range -->
			{:else if events.length === 0 && !loading}
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
						{#each events as event, idx (getEventKey(event, idx))}
							{@const colors = getSeverityColors(event.severity as Severity)}
							{@const key = getEventKey(event, idx)}
							<div class="relative group animate-fade-in">
								<div class="absolute -left-[22px] -translate-x-1/2 top-[3px] h-3 w-3 rounded-full {colors.bg} border-2 border-background shadow-sm transition-transform group-hover:scale-125"></div>

								<div class="pl-2">
									<div class="flex items-center gap-2 mb-0.5">
										<span class="text-xs text-muted-foreground font-mono">
											{formatRelativeTime(event.timestamp)}
										</span>
										<button
											onclick={() => {
												const ch: EventFeedChannel | undefined = channels.find(
													(c: EventFeedChannel) => c.name === event.channelName
												);
												if (ch) onToggleChannelFilter(ch.id);
											}}
											class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs border border-border/40 bg-muted/30 hover:bg-muted/60 transition-colors text-muted-foreground"
											title="Filter by channel"
										>
											{event.channelName}
										</button>
									</div>

									<p class="text-sm font-semibold leading-tight">{event.title}</p>

									{#if event.message}
										<p class="text-xs text-muted-foreground mt-0.5 {expandedMessages[key] ? '' : 'line-clamp-2'}">
											{event.message}
										</p>
										{#if event.message.length > 120}
											<button
												onclick={() => onToggleExpand(key)}
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
						{#if loading && !initialLoad}
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
						{:else if !hasMore && events.length > 0}
							<span class="text-xs text-muted-foreground/50">All events loaded</span>
						{/if}
					</div>
				</div>
			{/if}

			<!-- Error overlay -->
			{#if error}
				<div class="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm">
					<div class="text-center p-6">
						<p class="text-destructive font-medium mb-2">Failed to load events</p>
						<p class="text-sm text-muted-foreground mb-4">{error}</p>
						<button class="text-sm text-primary hover:underline" onclick={onReload}>
							Try again
						</button>
					</div>
				</div>
			{/if}
		</div>
	</CardContent>
</Card>
