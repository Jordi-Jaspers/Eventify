<!--
  NotificationPanel Component

  Right-side Sheet panel displaying notifications.
  Shows changelog notifications grouped by type.

  Props:
  - open: boolean — controls sheet visibility
  - onOpenChange: (open: boolean) => void — callback for open state changes
-->
<script lang="ts">
	import { goto } from '$app/navigation';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Badge } from '$lib/components/ui/badge';
	import { Button } from '$lib/components/ui/button';
	import { Bell, Sparkles, ExternalLink, Inbox } from '@lucide/svelte';
	import { notificationStore } from '$lib/stores/notification.svelte';
	import type { NotificationItem } from '$lib/types/notification';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { formatDate } from '$lib/utils/date';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
	}

	let { open, onOpenChange }: Props = $props();

	const notifications: NotificationItem[] = $derived(notificationStore.notifications);
	const hasNotifications: boolean = $derived(notifications.length > 0);

	async function handleAction(path: string): Promise<void> {
		onOpenChange(false);
		await goto(path);
	}
</script>

<Sheet.Root {open} {onOpenChange}>
	<Sheet.Content
		class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col sm:max-w-md"
		side="right"
	>
		<Sheet.Header class="pt-6 pb-4 border-b border-border/50">
			<Sheet.Title class="flex items-center gap-2">
				<div
					class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary/10 border border-primary/20"
				>
					<Bell class="h-4 w-4 text-primary" />
				</div>
				Notifications
			</Sheet.Title>
			<Sheet.Description class="text-muted-foreground text-sm">
				Stay up to date with the latest updates and announcements.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 overflow-y-auto py-4">
			{#if hasNotifications}
				<div class="space-y-3 px-4">
					{#each notifications as notification (notification.id)}
					<div
						class="rounded-lg border p-4 space-y-3 transition-colors border-primary/20 bg-primary/5 hover:bg-primary/10"
					>
						<div class="flex items-start justify-between gap-2">
							<div class="flex items-center gap-2">
								{#if notification.type === 'changelog'}
									<div
										class="flex items-center justify-center w-7 h-7 rounded-md bg-primary/10"
									>
										<Sparkles class="h-3.5 w-3.5 text-primary" />
									</div>
								{/if}
								<div>
									<p class="font-medium text-sm leading-tight">{notification.title}</p>
									<p class="text-xs text-muted-foreground mt-0.5">
										{formatDate(notification.date)}
									</p>
								</div>
							</div>
							<div class="flex items-center gap-1.5 shrink-0">
								<span class="w-2 h-2 rounded-full bg-primary shrink-0"></span>
								<Badge
									class="text-[10px] px-1.5 py-0 capitalize bg-primary/10 text-primary border-primary/20"
								>
									{notification.type}
								</Badge>
							</div>
						</div>

						{#if notification.description}
							<p class="text-xs text-muted-foreground leading-relaxed line-clamp-2">
								{notification.description}
							</p>
						{/if}

						{#if notification.actionLabel && notification.actionPath}
							<Button
								variant="outline"
								size="sm"
								class="w-full text-xs h-8 border-border/50 hover:bg-primary/10 hover:border-primary/30"
								onclick={() => handleAction(notification.actionPath!)}
							>
								<ExternalLink class="h-3 w-3 mr-1.5" />
								{notification.actionLabel}
							</Button>
						{/if}
					</div>
				{/each}
				</div>
			{:else}
				<div class="flex flex-col items-center justify-center h-full py-16 px-4 text-center">
					<div
						class="flex items-center justify-center w-12 h-12 rounded-full bg-muted/50 mb-4"
					>
						<Inbox class="h-6 w-6 text-muted-foreground/50" />
					</div>
					<p class="font-medium text-sm text-muted-foreground">All caught up!</p>
					<p class="text-xs text-muted-foreground/70 mt-1">No new notifications at this time.</p>
				</div>
			{/if}
		</div>

		<Sheet.Footer class="border-t border-border/50 pb-6 pt-4 px-4">
			<Button
				variant="outline"
				class="w-full border-border/50 hover:bg-primary/10"
				onclick={() => handleAction(CLIENT_ROUTES.CHANGELOG_PAGE.path)}
			>
				<Sparkles class="h-4 w-4 mr-2 text-primary" />
				View Full Changelog
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
