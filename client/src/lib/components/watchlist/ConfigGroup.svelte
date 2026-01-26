<script lang="ts">
	import { GripVertical, ChevronDown as ExpandDown, ChevronRight, X, Plus, Folder } from '@lucide/svelte';
	import type { ConfigGroupItem, ConfigChannelItem } from './types';
	import ConfigChannel from './ConfigChannel.svelte';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		item: ConfigGroupItem;
		onUpdate: (updated: ConfigGroupItem) => void;
		onDelete: () => void;
		onAddChannel: () => void;
		onDragStart?: (e: DragEvent) => void;
		onDragOver?: (e: DragEvent) => void;
		onDrop?: (e: DragEvent) => void;
		onDragEnd?: (e: DragEvent) => void;
	}

	let { item, onUpdate, onDelete, onAddChannel, onDragStart, onDragOver, onDrop, onDragEnd }: Props = $props();

	// Derive isExpanded from item prop
	let isExpanded: boolean = $derived(item.isExpanded);

	let isDragging: boolean = $state(false);
	let isDropTarget: boolean = $state(false);
	let draggedChannelId: string | null = $state(null);

	function toggleExpanded(): void {
		onUpdate({ ...item, isExpanded: !isExpanded });
	}

	function handleDeleteChannel(channelId: number): void {
		const updated: ConfigChannelItem[] = item.channels.filter(
			(c: ConfigChannelItem) => c.channelId !== channelId
		);
		onUpdate({ ...item, channels: updated });
	}

	// Header drag handlers (for group reordering at root level)
	function handleHeaderDragStart(e: DragEvent): void {
		isDragging = true;
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/plain', item.id);
		}
		onDragStart?.(e);
	}

	function handleHeaderDragEnd(e: DragEvent): void {
		isDragging = false;
		isDropTarget = false;
		onDragEnd?.(e);
	}

	function handleHeaderDragOver(e: DragEvent): void {
		e.preventDefault();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'move';
		}
		if (!isDragging) {
			isDropTarget = true;
		}
		onDragOver?.(e);
	}

	function handleHeaderDragLeave(): void {
		isDropTarget = false;
	}

	function handleHeaderDrop(e: DragEvent): void {
		e.preventDefault();
		isDropTarget = false;
		onDrop?.(e);
	}

	// Channel drag handlers (for reordering channels within group)
	function handleChannelDragStart(e: DragEvent, channelId: string): void {
		e.stopPropagation();
		draggedChannelId = channelId;
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/plain', channelId);
		}
	}

	function handleChannelDragEnd(): void {
		draggedChannelId = null;
	}

	function handleChannelDragOver(e: DragEvent): void {
		e.preventDefault();
		e.stopPropagation();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'move';
		}
	}

	function handleChannelDrop(e: DragEvent, targetChannelId: string): void {
		e.preventDefault();
		e.stopPropagation();

		const draggedId: string | undefined = e.dataTransfer?.getData('text/plain');
		if (!draggedId || draggedId === targetChannelId) return;

		const draggedIndex: number = item.channels.findIndex((c: ConfigChannelItem) => c.id === draggedId);
		const targetIndex: number = item.channels.findIndex((c: ConfigChannelItem) => c.id === targetChannelId);

		if (draggedIndex === -1 || targetIndex === -1) return;

		const newChannels: ConfigChannelItem[] = [...item.channels];
		const [draggedChannel] = newChannels.splice(draggedIndex, 1);
		newChannels.splice(targetIndex, 0, draggedChannel);

		onUpdate({ ...item, channels: newChannels });
	}
</script>

<div
	role="group"
	class="rounded-md border border-border/30 transition-all"
	class:opacity-50={isDragging}
>
	<!-- Group Header - Draggable for reordering groups -->
	<div
		draggable="true"
		ondragstart={handleHeaderDragStart}
		ondragend={handleHeaderDragEnd}
		ondragover={handleHeaderDragOver}
		ondragleave={handleHeaderDragLeave}
		ondrop={handleHeaderDrop}
		class:border={isDropTarget}
		class:border-primary={isDropTarget}
		class="group flex items-center gap-3 px-3 py-2.5 bg-muted/20 hover:bg-muted/40 transition-all cursor-move {isDropTarget ? 'bg-primary/10' : ''}"
		role="button"
		tabindex="0"
		aria-label="Drag to reorder group"
	>
		<!-- Drag handle - subtle, shows on hover -->
		<div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
			<GripVertical class="h-4 w-4 text-muted-foreground/50" />
		</div>

		<!-- Expand/Collapse Button -->
		<button
			onclick={toggleExpanded}
			class="shrink-0 text-muted-foreground hover:text-foreground transition-colors"
			aria-label={isExpanded ? 'Collapse group' : 'Expand group'}
		>
			{#if isExpanded}
				<ExpandDown class="h-4 w-4" />
			{:else}
				<ChevronRight class="h-4 w-4" />
			{/if}
		</button>

		<!-- Info -->
		<div class="flex-1 min-w-0 flex items-center gap-2">
			<Folder class="h-4 w-4 text-primary/70 shrink-0" />
			<div class="min-w-0">
				<p class="font-medium text-sm truncate text-foreground">{item.name}</p>
				<p class="text-xs text-muted-foreground">
					{item.channels.length} {item.channels.length === 1 ? 'channel' : 'channels'}
				</p>
			</div>
		</div>

		<!-- Delete Button - subtle, shows on hover -->
		<Button
			variant="ghost"
			size="icon"
			class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive shrink-0 transition-all"
			onclick={onDelete}
			aria-label="Remove group"
		>
			<X class="h-4 w-4" />
		</Button>
	</div>

	<!-- Group Content (Channels) - Show when expanded -->
	{#if isExpanded}
		<div class="pl-10 pr-3 pb-3 pt-2 space-y-1">
			<!-- Channels List -->
			{#if item.channels.length > 0}
				{#each item.channels as channel (channel.id)}
					<ConfigChannel
						item={channel}
						onDelete={() => handleDeleteChannel(channel.channelId)}
						onDragStart={(e) => handleChannelDragStart(e, channel.id)}
						onDragEnd={handleChannelDragEnd}
						onDragOver={handleChannelDragOver}
						onDrop={(e) => handleChannelDrop(e, channel.id)}
					/>
				{/each}
			{/if}

			<!-- Add Channel Button -->
			<button
				class="w-full text-left px-3 py-2 rounded-md border border-dashed border-border/50 hover:border-primary/50 hover:bg-muted/30 text-sm text-muted-foreground hover:text-primary transition-all flex items-center gap-2"
				onclick={onAddChannel}
			>
				<Plus class="h-3.5 w-3.5" />
				Add Channel
			</button>
		</div>
	{/if}
</div>
