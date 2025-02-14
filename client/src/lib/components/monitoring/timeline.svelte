<script lang="ts">
	import { HoverCard, HoverCardContent, HoverCardTrigger } from '$lib/components/ui/hover-card';
	import { MonitoringService } from '$lib/components/monitoring/service/monitoring.service.js';
	import { formatDuration, formatTime } from '$lib/utils.ts';
	import { CalendarArrowDown, CalendarArrowUp } from 'lucide-svelte';

	let { timeline = $bindable(null), window = $bindable(120) } = $props<{
		timeline: TimelineResponse;
		window: number;
	}>();

	const windowMs = window * 60 * 1000;

	// Calculate window boundaries
	let now: Date = $state(new Date());
	let windowStart: Date = $derived(new Date(now.getTime() - windowMs));
	let visibleDurations = $derived.by(() => {
		return timeline.durations
			.map((d) => ({
				...d,
				start: new Date(d.startTime),
				end: d.endTime ? new Date(d.endTime) : now
			}))
			.filter((d) => d.end > windowStart && d.start < now)
			.map((d) => ({
				...d,
				// Clip to window boundaries
				visualStart: d.start < windowStart ? windowStart : d.start,
				visualEnd: d.end > now ? now : d.end
			}))
			.map((d) => ({
				...d,
				// Calculate positions relative to window
				position: ((d.visualStart.getTime() - windowStart.getTime()) / windowMs) * 100,
				width: ((d.visualEnd.getTime() - d.visualStart.getTime()) / windowMs) * 100
			}));
	});

	$effect(() => {
		const interval = setInterval(() => {
			now = new Date();
		}, 1000);

		return () => clearInterval(interval);
	});
</script>

<div class="relative h-4 w-full overflow-hidden rounded">
	{#each visibleDurations as duration}
		<HoverCard>
			<HoverCardTrigger>
				<div
					class={'absolute h-full ' + MonitoringService.getStatusBackgroundColor(duration.status)}
					style="left: {duration.position}%; width: {duration.width}%"
				></div>
			</HoverCardTrigger>

			<HoverCardContent>
				<div class="space-y-1">
					<div class="font-medium">{duration.status}</div>
					<div class="flex flex-col text-sm text-gray-500">
						<span class="flex items-center">
							<CalendarArrowUp class="mr-1 h-4 w-4" />
							{formatTime(duration.startTime)}
						</span>
						<span class="flex items-center">
							<CalendarArrowDown class="mr-1 h-4 w-4" />
							{duration.endTime ? formatTime(duration.visualEnd) : 'Now'}
						</span>
					</div>
					<div class="text-xs text-gray-400">
						Duration: {formatDuration(duration.startTime, duration.visualEnd)}
					</div>
				</div>
			</HoverCardContent>
		</HoverCard>
	{/each}
</div>
