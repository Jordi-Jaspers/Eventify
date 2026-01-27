<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '$lib/components/ui/collapsible';
	import { ChevronDown, Circle, Pause } from '@lucide/svelte';
	import TimelineBar from './TimelineBar.svelte';
	import { getSeverityColors } from './types';
	import type { ChannelResponse } from '$lib/api/models';

	interface Props {
		channel: ChannelResponse;
		rangeStart: Date;
		rangeEnd: Date;
	}

	let { channel, rangeStart, rangeEnd }: Props = $props();

	let isExpanded: boolean = $state(false);
	let selectedSegment: { startTime: string; endTime: string } | null = $state(null);

	function handleSegmentClick(startTime: string, endTime: string): void {
		if (selectedSegment?.startTime === startTime && selectedSegment?.endTime === endTime) {
			// Toggle off if clicking same segment
			selectedSegment = null;
			isExpanded = false;
		} else {
			selectedSegment = { startTime, endTime };
			isExpanded = true;
		}
	}

	const severityColors = $derived(
		channel.currentSeverity ? getSeverityColors(channel.currentSeverity) : null
	);
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-sm">
	<CardContent class="p-4">
		<Collapsible bind:open={isExpanded}>
			<div class="space-y-3">
				<!-- Channel header -->
				<div class="flex items-center justify-between">
					<div class="flex items-center gap-3">
						<!-- Status indicator -->
						{#if channel.status === 'ACTIVE'}
							<Circle class="h-3 w-3 text-green-500 fill-current" />
						{:else if channel.status === 'PAUSED'}
							<Pause class="h-3 w-3 text-gray-400" />
						{:else}
							<Circle class="h-3 w-3 text-gray-400" />
						{/if}

						<!-- Channel name -->
						<h3 class="font-medium text-foreground">{channel.channelName}</h3>

						<!-- Current severity badge -->
						{#if severityColors}
							<Badge variant="outline" class="border-{severityColors.border} {severityColors.text}">
								{channel.currentSeverity}
							</Badge>
						{/if}
					</div>

					<!-- Expand trigger if segment selected -->
					{#if selectedSegment}
						<CollapsibleTrigger class="p-1 rounded hover:bg-muted transition-colors">
							<ChevronDown
								class="h-4 w-4 transition-transform {isExpanded ? 'rotate-180' : ''}"
							/>
						</CollapsibleTrigger>
					{/if}
				</div>

				<!-- Timeline -->
				<TimelineBar
					timeline={channel.timeline}
					{rangeStart}
					{rangeEnd}
					onSegmentClick={handleSegmentClick}
				/>

				<!-- Expanded event details -->
				<CollapsibleContent>
					{#if selectedSegment}
						<div class="mt-4 pt-4 border-t border-border/30">
							<div class="flex items-center gap-2 mb-3">
								<span class="text-sm font-medium text-muted-foreground">
									Events: {new Date(selectedSegment.startTime).toLocaleString()} - {new Date(selectedSegment.endTime).toLocaleString()}
								</span>
							</div>

							<!-- Placeholder for events list (endpoint not yet implemented) -->
							<div class="bg-muted/20 rounded p-4 text-center">
								<p class="text-sm text-muted-foreground">
									Event details will be available here once the events endpoint is implemented.
								</p>
							</div>
						</div>
					{/if}
				</CollapsibleContent>
			</div>
		</Collapsible>
	</CardContent>
</Card>
