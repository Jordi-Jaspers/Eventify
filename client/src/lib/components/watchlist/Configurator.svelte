<script lang="ts">
	import { Settings, Inbox, Radio, FolderTree } from '@lucide/svelte';
	import type { ConfigItem, ConfigGroupItem, ConfigChannelItem } from './types';
	import { isConfigChannelItem, isConfigGroupItem } from './types';
	import { sortConfigItems } from './utils';
	import ConfigChannel from './ConfigChannel.svelte';
	import ConfigGroup from './ConfigGroup.svelte';
	import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		items: ConfigItem[];
		onUpdate: (items: ConfigItem[]) => void;
		onAddChannel: (groupId?: string) => void;
		onAddGroup: () => void;
	}

	let { items = $bindable(), onUpdate, onAddChannel, onAddGroup }: Props = $props();

	let internalItems: ConfigItem[] = $state([...items]);
	let draggedItemId: string | null = $state(null);

	// Sync external items with internal and auto-sort
	$effect(() => {
		const sorted: ConfigItem[] = sortConfigItems([...items]);
		internalItems = sorted;
	});

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

	// Root-level drag handlers
	function handleRootDragStart(e: DragEvent, itemId: string): void {
		draggedItemId = itemId;
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/plain', itemId);
		}
	}

	function handleRootDragEnd(): void {
		draggedItemId = null;
	}

	function handleRootDragOver(e: DragEvent): void {
		e.preventDefault();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'move';
		}
	}

	function handleRootDrop(e: DragEvent, targetItemId: string): void {
		e.preventDefault();

		const draggedId: string | undefined = e.dataTransfer?.getData('text/plain');
		if (!draggedId || draggedId === targetItemId) return;

		const draggedIndex: number = internalItems.findIndex((item: ConfigItem) => item.id === draggedId);
		const targetIndex: number = internalItems.findIndex((item: ConfigItem) => item.id === targetItemId);

		if (draggedIndex === -1 || targetIndex === -1) return;

		const newItems: ConfigItem[] = [...internalItems];
		const [draggedItem] = newItems.splice(draggedIndex, 1);
		newItems.splice(targetIndex, 0, draggedItem);

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
		<!-- Add buttons at top -->
		<div class="flex gap-4 mb-6">
			<Button onclick={() => onAddChannel()} class="flex-1">
				<Radio class="mr-2 h-4 w-4" />
				Add Channel
			</Button>
			<Button
				onclick={onAddGroup}
				variant="outline"
				class="flex-1 hover:bg-muted/50 transition-colors border-dashed border-border hover:border-primary/50"
			>
				<FolderTree class="mr-2 h-4 w-4" />
				Add Group
			</Button>
		</div>

		{#if internalItems.length === 0}
			<!-- Empty state -->
			<div class="flex flex-col items-center justify-center py-16 text-center min-h-[400px]">
				<div
					class="p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 mb-6"
				>
					<Inbox class="w-12 h-12 text-primary" />
				</div>
				<h3 class="text-xl font-semibold text-foreground">No channels yet</h3>
				<p class="text-muted-foreground mt-2 max-w-sm">
					Click "Add Channel" above to start building your watchlist
				</p>
			</div>
		{:else}
			<!-- Items list -->
			<div class="space-y-3">
				{#each internalItems as item (item.id)}
					{#if isConfigGroupItem(item)}
						<ConfigGroup
							{item}
							onUpdate={handleUpdateGroup}
							onDelete={() => handleDeleteGroup(item.id)}
							onAddChannel={() => onAddChannel(item.id)}
							onDragStart={(e: DragEvent) => handleRootDragStart(e, item.id)}
							onDragEnd={handleRootDragEnd}
							onDragOver={handleRootDragOver}
							onDrop={(e: DragEvent) => handleRootDrop(e, item.id)}
						/>
					{:else if isConfigChannelItem(item)}
						<ConfigChannel
							{item}
							onDelete={() => handleDeleteChannel(item.channelId)}
							onDragStart={(e: DragEvent) => handleRootDragStart(e, item.id)}
							onDragEnd={handleRootDragEnd}
							onDragOver={handleRootDragOver}
							onDrop={(e: DragEvent) => handleRootDrop(e, item.id)}
						/>
					{/if}
				{/each}
			</div>
		{/if}
	</CardContent>
</Card>
