<script lang="ts">
	import { LoaderCircle, Inbox } from '@lucide/svelte';
	import { onMount } from 'svelte';
	import { formatDateTime } from '$lib/utils/date';
	import { createEventService } from '$lib/services/event-service.svelte';

	interface Props {
		channelId: number;
		startTime: string;
		endTime: string;
		orgId?: number;
	}

	let { channelId, startTime, endTime, orgId }: Props = $props();

	// Use extracted service for logic
	const eventService = createEventService(channelId, orgId);

	// Initial load on mount
	onMount(() => {
		eventService.load(startTime, endTime, true);
	});

	// Infinite scroll observer
	let listContainer: HTMLDivElement | undefined = $state(undefined);
	let sentinel: HTMLDivElement | undefined = $state(undefined);
	let observer: IntersectionObserver | undefined;

	$effect(() => {
		if (!listContainer || !sentinel) return;
		
		observer?.disconnect();
		observer = new IntersectionObserver((entries) => {
			if (entries[0].isIntersecting && !eventService.loading && eventService.hasMore) {
				eventService.load(startTime, endTime, false);
			}
		}, { root: listContainer, threshold: 0.1 });

		observer.observe(sentinel);

		return () => observer?.disconnect();
	});
</script>

<div 
	bind:this={listContainer} 
	class="overflow-y-auto min-h-[300px] max-h-[400px] relative"
>
	<!-- Initial Loading State -->
	{#if eventService.initialLoad && eventService.loading}
		<div class="flex flex-col items-center justify-center h-[300px] text-muted-foreground">
			<LoaderCircle class="h-8 w-8 animate-spin text-primary mb-3" />
			<p class="text-sm">Loading events...</p>
		</div>
	<!-- Empty State -->
	{:else if eventService.events.length === 0 && !eventService.loading && !eventService.error}
		<div class="flex flex-col items-center justify-center h-[300px] text-muted-foreground">
			<div class="h-16 w-16 rounded-full bg-muted/30 flex items-center justify-center mb-4">
				<Inbox class="h-8 w-8 opacity-50" />
			</div>
			<p class="font-medium">No events in this duration</p>
			<p class="text-sm text-muted-foreground/70 mt-1">This time period has no recorded events</p>
		</div>
	<!-- Events List -->
	{:else}
		<div class="p-4">
			<!-- Event count header -->
			<div class="text-xs text-muted-foreground mb-4 flex items-center justify-between">
				<span>{eventService.totalEvents} event{eventService.totalEvents !== 1 ? 's' : ''}</span>
			</div>
			
			<div class="relative pl-6 border-l-2 border-border/40 space-y-4">
				{#each eventService.events as event, idx (event.timestamp + '-' + idx)}
					<div class="relative group animate-fade-in">
						<!-- Timeline dot - centered on the border line -->
						<div class="absolute -left-[22px] -translate-x-1/2 top-[3px] h-3 w-3 rounded-full bg-primary/80 border-2 border-background shadow-sm shadow-primary/20 transition-transform group-hover:scale-125"></div>
						
						<!-- Event content -->
						<div class="pl-2">
							<div class="text-xs text-muted-foreground font-mono mb-0.5">
								{formatDateTime(event.timestamp)}
							</div>
							<div class="text-sm text-foreground leading-relaxed">
								{event.message}
							</div>
						</div>
					</div>
				{/each}
			</div>
			
			<!-- Sentinel for infinite scroll -->
			<div bind:this={sentinel} class="py-6 flex justify-center">
				{#if eventService.loading && !eventService.initialLoad}
					<div class="flex items-center gap-2 text-muted-foreground">
						<LoaderCircle class="h-5 w-5 animate-spin text-primary" />
						<span class="text-sm">Loading more...</span>
					</div>
				{:else if !eventService.hasMore && eventService.events.length > 0}
					<span class="text-xs text-muted-foreground/50">End of events</span>
				{/if}
			</div>
		</div>
	{/if}
	
	<!-- Error State -->
	{#if eventService.error}
		<div class="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm">
			<div class="text-center p-6">
				<p class="text-destructive font-medium mb-2">Failed to load events</p>
				<p class="text-sm text-muted-foreground mb-4">{eventService.error}</p>
				<button 
					class="text-sm text-primary hover:underline"
					onclick={() => eventService.load(startTime, endTime, true)}
				>
					Try again
				</button>
			</div>
		</div>
	{/if}
</div>
