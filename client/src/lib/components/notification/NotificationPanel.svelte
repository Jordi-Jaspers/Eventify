<!--
  NotificationPanel Component

  Right-side Sheet panel displaying notifications from the API.
  Shows notifications with category icons, relative time, and action buttons.

  Props:
  - open: boolean — controls sheet visibility
  - onOpenChange: (open: boolean) => void — callback for open state changes
-->
<script lang="ts">
	import { goto } from '$app/navigation';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Button } from '$lib/components/ui/button';
	import { Bell, Sparkles, Settings, AlertTriangle, Inbox, CheckCheck, ExternalLink } from '@lucide/svelte';
	import { notificationStore } from '$lib/stores/notification.svelte';
	import type { NotificationResponse } from '$lib/api/models';
	import { formatRelativeTime } from '$lib/utils/date';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
	}

	let { open, onOpenChange }: Props = $props();

	const notifications: NotificationResponse[] = $derived(notificationStore.notifications);
	const hasNotifications: boolean = $derived(notifications.length > 0);
	const hasUnread: boolean = $derived(notificationStore.hasUnread);
	const hasMore: boolean = $derived(notificationStore.hasMore);

	function isUnread(notification: NotificationResponse): boolean {
		return !notification.readAt;
	}

	function isInternalUrl(url: string): boolean {
		return url.startsWith('/');
	}

	async function handleItemClick(notification: NotificationResponse): Promise<void> {
		if (isUnread(notification)) {
			await notificationStore.markAsRead(notification.id);
		}
		if (notification.actionUrl) {
			onOpenChange(false);
			if (isInternalUrl(notification.actionUrl)) {
				await goto(notification.actionUrl);
			} else {
				window.open(notification.actionUrl, '_blank');
			}
		}
	}
</script>

<Sheet.Root {open} {onOpenChange}>
	<Sheet.Content
		class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col sm:max-w-md"
		side="right"
	>
		<Sheet.Header class="pt-6 pb-4 border-b border-border/50">
			<div class="flex items-center justify-between">
				<Sheet.Title class="flex items-center gap-2">
					<div
						class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary/10 border border-primary/20"
					>
						<Bell class="h-4 w-4 text-primary" />
					</div>
					Notifications
				</Sheet.Title>
				{#if hasUnread}
					<Button
						variant="ghost"
						size="sm"
						class="text-xs text-muted-foreground hover:text-foreground"
						onclick={() => notificationStore.markAllAsRead()}
					>
						<CheckCheck class="h-3.5 w-3.5 mr-1.5" />
						Mark all read
					</Button>
				{/if}
			</div>
			<Sheet.Description class="text-muted-foreground text-sm">
				Stay up to date with the latest updates and announcements.
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 overflow-y-auto py-4">
			{#if hasNotifications}
				<div class="space-y-3 px-4">
					{#each notifications as notification (notification.id)}
						{@const unread = isUnread(notification)}
						<button
							type="button"
							class="w-full text-left rounded-lg border p-4 space-y-2 transition-colors cursor-pointer
								{notification.urgent ? 'border-l-4 border-l-destructive border-border/50' : 'border-border/50'}
								{unread ? 'bg-primary/5 hover:bg-primary/10' : 'bg-transparent hover:bg-muted/30'}"
							onclick={() => handleItemClick(notification)}
						>
							<div class="flex items-start justify-between gap-2">
								<div class="flex items-center gap-2">
									{#if notification.category === 'ANNOUNCEMENT'}
										<div class="flex items-center justify-center w-7 h-7 rounded-md bg-blue-500/10">
											<Sparkles class="h-3.5 w-3.5 text-blue-400" />
										</div>
									{:else if notification.category === 'SYSTEM'}
										<div class="flex items-center justify-center w-7 h-7 rounded-md bg-muted/50">
											<Settings class="h-3.5 w-3.5 text-muted-foreground" />
										</div>
									{:else if notification.category === 'ALERT'}
										<div class="flex items-center justify-center w-7 h-7 rounded-md bg-destructive/10">
											<AlertTriangle class="h-3.5 w-3.5 text-destructive" />
										</div>
									{/if}
									<div>
										<p class="font-medium text-sm leading-tight">{notification.title}</p>
										<p class="text-xs text-muted-foreground mt-0.5">
											{formatRelativeTime(notification.createdAt)}
										</p>
									</div>
								</div>
								{#if unread}
									<span class="w-2 h-2 rounded-full bg-primary shrink-0 mt-1"></span>
								{/if}
							</div>

							{#if notification.message}
								<p class="text-xs text-muted-foreground leading-relaxed line-clamp-2">
									{notification.message}
								</p>
							{/if}

						{#if notification.actionUrl && notification.actionLabel}
							<div class="pt-1">
								<span
									class="inline-flex items-center gap-1 text-xs text-primary hover:underline"
								>
									{notification.actionLabel}
									{#if isInternalUrl(notification.actionUrl)}
										→
									{:else}
										<ExternalLink class="h-3 w-3" />
									{/if}
								</span>
							</div>
						{/if}
						</button>
					{/each}

					{#if hasMore}
						<Button
							variant="outline"
							class="w-full border-border/50 hover:bg-primary/10 text-xs"
							onclick={() => notificationStore.loadMore()}
						>
							Load more
						</Button>
					{/if}
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
	</Sheet.Content>
</Sheet.Root>
