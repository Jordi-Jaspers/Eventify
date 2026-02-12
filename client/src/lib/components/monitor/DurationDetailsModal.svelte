<script lang="ts">
	import * as Dialog from '$lib/components/ui/dialog';
	import { Badge } from '$lib/components/ui/badge';
	import { Radio, Clock, Calendar, LoaderCircle } from '@lucide/svelte';
	import type { TimelineDuration, Severity } from '$lib/api/models';
	import { getSeverityColors } from './types';
	import MiniTimeline from './MiniTimeline.svelte';
	import EventsList from './EventsList.svelte';
	import { formatTime, formatDate } from '$lib/utils/date';
	import { formatDurationLength } from '$lib/utils/duration';
	import { createDurationService } from '$lib/api/monitor/service/DurationService.svelte';
	import { untrack } from 'svelte';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		channelName: string;
		currentSeverity: Severity | null;
		selectedDuration: TimelineDuration | null;
		orgId?: number; // If present, use org API
		channelId?: number;
	}

	let { 
		open = $bindable(false), 
		onOpenChange, 
		channelName, 
		currentSeverity, 
		selectedDuration = $bindable(null), 
		orgId,
		channelId
	}: Props = $props();

	const service = createDurationService();
	
	// Derived values for reactivity - service getters need to be tracked
	const serviceDurations = $derived(service.durations);
	const serviceCanGoPrevious = $derived(service.canGoPrevious);
	const serviceCanGoNext = $derived(service.canGoNext);
	const serviceHasPrevious = $derived(service.hasPrevious);
	const serviceLoading = $derived(service.loading);

	// Load data when modal opens
	let wasOpen = false;
	$effect(() => {
		if (open && !wasOpen) {
			untrack(() => {
				if (channelId && selectedDuration) {
					service.load(channelId, orgId, selectedDuration.startTime);
				}
			});
		}
		wasOpen = open;
	});

	// Sync service selection back to prop
	$effect(() => {
		if (service.selectedDuration) {
			selectedDuration = service.selectedDuration;
		}
	});

	// Derived from selectedDuration (which is synced with service)
	const severityColors = $derived(
		currentSeverity ? getSeverityColors(currentSeverity) : null
	);

	const actualStartTime = $derived(selectedDuration?.startTime ?? '');
	// For ongoing durations (null endTime), use current time for event search
	const actualEndTime = $derived(selectedDuration?.endTime ?? new Date().toISOString());

	const durationTimeRange = $derived(
		selectedDuration 
			? `${formatTime(actualStartTime)} → ${actualEndTime ? formatTime(actualEndTime) : 'Now'}` 
			: ''
	);

	const durationLength = $derived(
		selectedDuration ? formatDurationLength(actualStartTime, actualEndTime || new Date()) : ''
	);

	const durationDate = $derived(
		selectedDuration ? formatDate(actualStartTime) : ''
	);
</script>

<Dialog.Root open={open} onOpenChange={onOpenChange}>
	<Dialog.Content class="sm:max-w-3xl bg-card/95 backdrop-blur-xl border-border/50 shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
		<!-- Gradient accent line -->
		<div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-primary via-accent to-primary opacity-80"></div>
		
		<Dialog.Header class="border-b border-border/30 pb-4 pt-2 space-y-4">
			<!-- Header: Channel Info -->
			<div class="flex items-start gap-4 pr-8">
				<!-- Channel icon with pulse -->
				<div class="relative flex-shrink-0">
					<div class="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
						<Radio class="h-5 w-5 text-primary" />
					</div>
					{#if currentSeverity && severityColors}
						<div class="absolute -bottom-1 -right-1 h-4 w-4 rounded-full {severityColors.bg} border-2 border-card animate-pulse"></div>
					{/if}
				</div>
				
				<!-- Channel details -->
				<div class="flex-1 min-w-0">
					<Dialog.Title class="text-xl font-bold text-foreground truncate">
						{channelName}
					</Dialog.Title>
					
					<div class="flex items-center gap-4 mt-1 text-sm text-muted-foreground">
						{#if currentSeverity && severityColors}
							<Badge 
								variant="outline" 
								class="{severityColors.bg}/20 {severityColors.text} border-current"
							>
								{currentSeverity}
							</Badge>
						{/if}
						{#if durationDate}
							<span class="flex items-center gap-1">
								<Calendar class="h-3.5 w-3.5" />
								{durationDate}
							</span>
						{/if}
					</div>
				</div>
			</div>
			
			<Dialog.Description class="sr-only">
				Details for {channelName} including event history and timeline.
			</Dialog.Description>

			<!-- Mini Timeline -->
			<div class="pt-2 relative min-h-[60px]">
				{#if serviceLoading && serviceDurations.length === 0}
					<div class="absolute inset-0 flex items-center justify-center z-10">
						<LoaderCircle class="h-6 w-6 animate-spin text-primary" />
					</div>
				{:else}
					<!-- Show loading overlay if loading more but keep content -->
					{#if serviceLoading}
						<div class="absolute inset-0 bg-background/20 z-20 flex items-center justify-center backdrop-blur-[1px]">
							<LoaderCircle class="h-5 w-5 animate-spin text-primary" />
						</div>
					{/if}
					
					<MiniTimeline 
						durations={serviceDurations} 
						selectedDuration={selectedDuration}
                        canGoPrevious={serviceCanGoPrevious}
                        canGoNext={serviceCanGoNext}
                        hasPrevious={serviceHasPrevious}
						onDurationClick={(d) => service.selectDuration(d)}
                        onPrevious={() => service.goToPrevious()}
                        onNext={() => service.goToNext()}
					/>
				{/if}
			</div>
		</Dialog.Header>

		<!-- Selected Duration Info Bar -->
		<div class="px-6 py-3 bg-muted/30 border-b border-border/30 flex justify-between items-center text-sm">
			<div class="flex items-center gap-4">
				<div class="flex items-center gap-2 text-foreground font-medium">
					<Clock class="h-4 w-4 text-primary" />
					<span class="font-mono">{durationTimeRange}</span>
				</div>
				{#if durationLength}
					<Badge variant="secondary" class="text-xs">
						{durationLength}
					</Badge>
				{/if}
			</div>
			{#if selectedDuration?.severity}
				{@const segmentColors = getSeverityColors(selectedDuration.severity)}
				<div class="flex items-center gap-2">
					<div class="h-2 w-2 rounded-full {segmentColors.bg}"></div>
					<span class="text-xs text-muted-foreground uppercase tracking-wide">{selectedDuration.severity}</span>
				</div>
			{/if}
		</div>

		<!-- Events List (Scrollable) -->
		<div class="flex-1 min-h-0 relative">
			{#if selectedDuration && channelId}
				{#key `${actualStartTime}-${actualEndTime}-${selectedDuration.severity}`}
					<EventsList 
						{channelId}
						{orgId}
						startTime={actualStartTime}
						endTime={actualEndTime}
						severity={selectedDuration.severity}
					/>
				{/key}
			{:else}
				<div class="flex items-center justify-center h-40 text-muted-foreground">
					Select a duration to view events
				</div>
			{/if}
		</div>
	</Dialog.Content>
</Dialog.Root>
