<script lang="ts">
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
	import { Activity } from '@lucide/svelte';
	import TimelineBar from './TimelineBar.svelte';
	import type { Timeline } from '$lib/api/models';

	interface Props {
		timeline: Timeline;
		rangeStart: Date;
		rangeEnd: Date;
		watchlistName: string;
	}

	let { timeline, rangeStart, rangeEnd, watchlistName }: Props = $props();
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
	<CardHeader>
		<CardTitle class="flex items-center gap-3 text-lg">
			<Activity class="h-5 w-5 text-primary" />
			Dashboard - {watchlistName}
		</CardTitle>
	</CardHeader>
	<CardContent>
		<div class="space-y-2">
			<p class="text-xs text-muted-foreground">
				Consolidated status across all monitored channels
			</p>
			<TimelineBar
				{timeline}
				{rangeStart}
				{rangeEnd}
			/>
		</div>
	</CardContent>
</Card>
