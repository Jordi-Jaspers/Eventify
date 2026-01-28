<script lang="ts">
	import { GripVertical, Radio, X } from '@lucide/svelte';
	import type { ConfigChannelItem } from './types';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		item: ConfigChannelItem;
		onDelete: () => void;
		onDragStart?: (e: DragEvent) => void;
		onDragOver?: (e: DragEvent) => void;
		onDrop?: (e: DragEvent) => void;
		onDragEnd?: (e: DragEvent) => void;
	}

	let { item, onDelete, onDragStart, onDragOver, onDrop, onDragEnd }: Props = $props();

	let isDragging: boolean = $state(false);
	let isDropTarget: boolean = $state(false);

	function handleDragStart(e: DragEvent): void {
		isDragging = true;
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'move';
			e.dataTransfer.setData('text/plain', item.id);
		}
		onDragStart?.(e);
	}

	function handleDragEnd(e: DragEvent): void {
		isDragging = false;
		isDropTarget = false;
		onDragEnd?.(e);
	}

	function handleDragOver(e: DragEvent): void {
		e.preventDefault();
		if (e.dataTransfer) {
			e.dataTransfer.dropEffect = 'move';
		}
		if (!isDragging) {
			isDropTarget = true;
		}
		onDragOver?.(e);
	}

	function handleDragLeave(): void {
		isDropTarget = false;
	}

	function handleDrop(e: DragEvent): void {
		e.preventDefault();
		isDropTarget = false;
		onDrop?.(e);
	}
</script>

<div
	data-channel-id={item.channelId}
	data-item-id={item.id}
	draggable="true"
	ondragstart={handleDragStart}
	ondragend={handleDragEnd}
	ondragover={handleDragOver}
	ondragleave={handleDragLeave}
	ondrop={handleDrop}
	class:opacity-50={isDragging}
	class:border={isDropTarget}
	class:border-primary={isDropTarget}
	class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move {isDropTarget ? 'bg-primary/10' : ''}"
	role="button"
	tabindex="0"
	aria-label="Drag to reorder channel"
>
	<!-- Drag handle - subtle, shows on hover -->
	<div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
		<GripVertical class="h-4 w-4 text-muted-foreground/50" />
	</div>

	<!-- Icon - more subtle -->
	<Radio class="h-4 w-4 text-primary/70 shrink-0" />

	<!-- Info -->
	<div class="flex-1 min-w-0">
		<p class="text-sm font-medium truncate text-foreground">{item.channel.name}</p>
		{#if item.channel.description}
			<p class="text-xs text-muted-foreground truncate" title={item.channel.description}>{item.channel.description}</p>
		{/if}
	</div>

	<!-- Delete Button - subtle, shows on hover -->
	<Button
		variant="ghost"
		size="icon"
		class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive shrink-0 transition-all"
		onclick={onDelete}
		aria-label="Remove channel"
	>
		<X class="h-4 w-4" />
	</Button>
</div>
