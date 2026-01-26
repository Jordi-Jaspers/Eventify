<script lang="ts">
	import { ChevronUp, ChevronDown, FolderTree, ChevronDown as ExpandDown, ChevronRight, Trash2, Plus } from '@lucide/svelte';
	import type { ConfigGroupItem, ConfigChannelItem } from './types';
	import ConfigChannel from './ConfigChannel.svelte';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		item: ConfigGroupItem;
		isFirst: boolean;
		isLast: boolean;
		onMoveUp: () => void;
		onMoveDown: () => void;
		onUpdate: (updated: ConfigGroupItem) => void;
		onDelete: () => void;
		onAddChannel: () => void;
	}

	let { item, isFirst, isLast, onMoveUp, onMoveDown, onUpdate, onDelete, onAddChannel }: Props = $props();

	// Derive isExpanded from item prop
	let isExpanded: boolean = $derived(item.isExpanded);

	function toggleExpanded(): void {
		onUpdate({ ...item, isExpanded: !isExpanded });
	}

	function handleDeleteChannel(channelId: number): void {
		const updated: ConfigChannelItem[] = item.channels.filter(
			(c: ConfigChannelItem) => c.channelId !== channelId
		);
		onUpdate({ ...item, channels: updated });
	}

	function handleMoveChannelUp(index: number): void {
		if (index > 0) {
			const newChannels: ConfigChannelItem[] = [...item.channels];
			[newChannels[index - 1], newChannels[index]] = [newChannels[index], newChannels[index - 1]];
			onUpdate({ ...item, channels: newChannels });
		}
	}

	function handleMoveChannelDown(index: number): void {
		if (index < item.channels.length - 1) {
			const newChannels: ConfigChannelItem[] = [...item.channels];
			[newChannels[index], newChannels[index + 1]] = [newChannels[index + 1], newChannels[index]];
			onUpdate({ ...item, channels: newChannels });
		}
	}
</script>

<div
	role="group"
	class="
		rounded-lg border border-border/50 bg-card/80 backdrop-blur-sm
		shadow-sm transition-all
	"
>
	<!-- Group Header -->
	<div class="flex items-center gap-3 p-3">
		<!-- Reorder buttons -->
		<div class="flex flex-col gap-1 shrink-0">
			<Button
				size="icon"
				variant="ghost"
				onclick={onMoveUp}
				disabled={isFirst}
				class="h-6 w-6 p-0 hover:bg-muted"
				aria-label="Move group up"
			>
				<ChevronUp class="h-4 w-4" />
			</Button>
			<Button
				size="icon"
				variant="ghost"
				onclick={onMoveDown}
				disabled={isLast}
				class="h-6 w-6 p-0 hover:bg-muted"
				aria-label="Move group down"
			>
				<ChevronDown class="h-4 w-4" />
			</Button>
		</div>

		<!-- Expand/Collapse Button -->
		<button
			onclick={toggleExpanded}
			class="p-1 hover:bg-muted rounded transition-colors shrink-0"
			aria-label={isExpanded ? 'Collapse group' : 'Expand group'}
		>
			{#if isExpanded}
				<ExpandDown class="h-4 w-4 text-muted-foreground" />
			{:else}
				<ChevronRight class="h-4 w-4 text-muted-foreground" />
			{/if}
		</button>

		<!-- Icon -->
		<div class="p-2 rounded-md bg-accent/10 shrink-0">
			<FolderTree class="h-5 w-5 text-accent" />
		</div>

		<!-- Info -->
		<div class="flex-1 min-w-0">
			<p class="font-medium text-sm truncate">{item.name}</p>
			<p class="text-xs text-muted-foreground">
				{item.channels.length} {item.channels.length === 1 ? 'channel' : 'channels'}
			</p>
		</div>

		<!-- Delete Button -->
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10 shrink-0"
			onclick={onDelete}
			aria-label="Remove group"
		>
			<Trash2 class="h-4 w-4" />
		</Button>
	</div>

	<!-- Group Content (Channels) - Show when expanded -->
	{#if isExpanded}
		<div class="px-3 pb-3 space-y-2">
			<!-- Add Channel Button -->
			<Button
				variant="outline"
				size="sm"
				class="w-full border-dashed border-border/50 hover:border-primary/50 hover:bg-primary/5"
				onclick={onAddChannel}
			>
				<Plus class="mr-2 h-4 w-4" />
				Add Channel
			</Button>

			<!-- Channels List -->
			{#if item.channels.length > 0}
				<div class="space-y-2">
					{#each item.channels as channel, index (channel.id)}
						<ConfigChannel
							item={channel}
							isFirst={index === 0}
							isLast={index === item.channels.length - 1}
							onMoveUp={() => handleMoveChannelUp(index)}
							onMoveDown={() => handleMoveChannelDown(index)}
							onDelete={() => handleDeleteChannel(channel.channelId)}
						/>
					{/each}
				</div>
			{/if}
		</div>
	{/if}
</div>
