<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Bell, ChevronRight } from '@lucide/svelte';
	import { formatRelativeTime } from '$lib/utils/date';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';
	import type { RecentNotificationResponse } from '$lib/api/models';

	interface Props {
		recentNotifications: RecentNotificationResponse[];
		onNotificationClick: (notification: RecentNotificationResponse) => void;
	}

	let { recentNotifications, onNotificationClick }: Props = $props();

	const notifCount: number = $derived(recentNotifications.length);
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
	<CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
		<CardTitle class="text-lg flex items-center gap-2">
			<Bell class="w-5 h-5 text-primary" />
			Recent Notifications
			{#if notifCount > 0}
				<span class="text-sm font-normal text-muted-foreground">({notifCount})</span>
			{/if}
		</CardTitle>
	</CardHeader>
	<CardContent>
		{#if recentNotifications.length === 0}
			<div class="py-4 text-center text-sm text-muted-foreground">
				No recent activity in the last 24h
			</div>
		{:else}
			<ul class="space-y-2">
				{#each recentNotifications as notif (notif.id)}
					<li>
						<button
							onclick={() => onNotificationClick(notif)}
							class="w-full text-left p-3 rounded-lg bg-background/50 border border-border/50 hover:bg-muted/30 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background {notif.actionUrl ? 'cursor-pointer' : 'cursor-default'}"
							aria-label={notif.title}
						>
							<div class="flex items-start justify-between gap-2">
								<div class="min-w-0 flex-1">
									<div class="flex items-center gap-2">
										{#if notif.urgent}
											<PulseIndicator variant="red" size="sm" />
										{/if}
										<span class="text-sm font-medium truncate">{notif.title}</span>
									</div>
									<p class="text-xs text-muted-foreground mt-0.5 line-clamp-1">{notif.message}</p>
								</div>
								<div class="flex flex-col items-end gap-1 shrink-0">
									<Badge variant="outline" class="text-xs">{notif.category}</Badge>
									<span class="text-xs text-muted-foreground">{formatRelativeTime(notif.createdAt)}</span>
								</div>
								{#if notif.actionUrl}
									<ChevronRight class="w-4 h-4 text-muted-foreground shrink-0 self-center" />
								{/if}
							</div>
						</button>
					</li>
				{/each}
			</ul>
		{/if}
	</CardContent>
</Card>
