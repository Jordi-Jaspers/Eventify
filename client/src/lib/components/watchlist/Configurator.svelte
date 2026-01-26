<script lang="ts">
	import { Settings, Inbox } from '@lucide/svelte';
	import type { ConfigItem, ConfigGroupItem, ConfigChannelItem } from './types';
	import { isConfigChannelItem, isConfigGroupItem } from './types';
	import { sortConfigItems } from './utils';
	import ConfigChannel from './ConfigChannel.svelte';
	import ConfigGroup from './ConfigGroup.svelte';
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';

	interface Props {
		items: ConfigItem[];
		onUpdate: (items: ConfigItem[]) => void;
		onAddChannel: (groupId?: string) => void;
	}

	let { items = $bindable(), onUpdate, onAddChannel }: Props = $props();

	let internalItems: ConfigItem[] = $state([...items]);

	// Sync external items with internal and auto-sort
	$effect(() => {
		const sorted: ConfigItem[] = sortConfigItems([...items]);
		internalItems = sorted;
	});

	function moveItem(index: number, direction: number): void {
		const newIndex: number = index + direction;
		if (newIndex < 0 || newIndex >= internalItems.length) return;

		const newItems: ConfigItem[] = [...internalItems];
		[newItems[index], newItems[newIndex]] = [newItems[newIndex], newItems[index]];
		
		internalItems = newItems;
		onUpdate(newItems);
	}

	function handleDeleteChannel(channelId: number): void {
		const updated: ConfigItem[] = internalItems.filter(
			(item: ConfigItem) =>
				!(isConfigChannelItem(item) && item.channelId === channelId)
		);
		internalItems = updated;
		onUpdate(updated);
	}

	function handleDeleteGroup(groupId: string): void {
		const updated: ConfigItem[] = internalItems.filter(
			(item: ConfigItem) => !(isConfigGroupItem(item) && item.id === groupId)
		);
		internalItems = updated;
		onUpdate(updated);
	}

	function handleUpdateGroup(updated: ConfigGroupItem): void {
		const newItems: ConfigItem[] = internalItems.map((item: ConfigItem): ConfigItem => {
			if (isConfigGroupItem(item) && item.id === updated.id) {
				return updated;
			}
			return item;
		});

		internalItems = newItems;
		onUpdate(newItems);
	}
</script>

<Card class="bg-card/50 backdrop-blur-xl border-border/50 shadow-lg">
	<CardHeader>
		<CardTitle class="text-lg flex items-center gap-2">
			<Settings class="h-5 w-5 text-primary" />
			Configuration
		</CardTitle>
	</CardHeader>
	<CardContent>
		{#if internalItems.length === 0}
			<!-- Empty state -->
			<div class="flex flex-col items-center justify-center py-12 text-center min-h-[400px]">
				<div
					class="p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 mb-4"
				>
					<Inbox class="w-12 h-12 text-primary" />
				</div>
				<h3 class="text-lg font-semibold">Empty Configuration</h3>
				<p class="text-sm text-muted-foreground mt-2">
					Use the buttons on the left to add channels or groups
				</p>
			</div>
		{:else}
			<!-- Items list -->
			<div class="space-y-3">
				{#each internalItems as item, index (item.id)}
					{#if isConfigGroupItem(item)}
						<ConfigGroup
							{item}
							isFirst={index === 0}
							isLast={index === internalItems.length - 1}
							onMoveUp={() => moveItem(index, -1)}
							onMoveDown={() => moveItem(index, 1)}
							onUpdate={handleUpdateGroup}
							onDelete={() => handleDeleteGroup(item.id)}
							onAddChannel={() => onAddChannel(item.id)}
						/>
					{:else if isConfigChannelItem(item)}
						<ConfigChannel
							{item}
							isFirst={index === 0}
							isLast={index === internalItems.length - 1}
							onMoveUp={() => moveItem(index, -1)}
							onMoveDown={() => moveItem(index, 1)}
							onDelete={() => handleDeleteChannel(item.channelId)}
						/>
					{/if}
				{/each}
			</div>
		{/if}
	</CardContent>
</Card>
