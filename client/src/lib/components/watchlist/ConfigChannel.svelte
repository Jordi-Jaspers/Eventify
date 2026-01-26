<script lang="ts">
	import { ChevronUp, ChevronDown, Radio, Trash2 } from '@lucide/svelte';
	import type { ConfigChannelItem } from './types';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		item: ConfigChannelItem;
		isFirst: boolean;
		isLast: boolean;
		onMoveUp: () => void;
		onMoveDown: () => void;
		onDelete: () => void;
	}

	let { item, isFirst, isLast, onMoveUp, onMoveDown, onDelete }: Props = $props();
</script>

<div
	data-channel-id={item.channelId}
	class="
		rounded-lg border border-border/50 bg-card/80 backdrop-blur-sm
		p-3 hover:border-primary/50 transition-all shadow-sm
		flex items-center gap-3
	"
>
	<!-- Reorder buttons -->
	<div class="flex flex-col gap-1 shrink-0">
		<Button
			size="icon"
			variant="ghost"
			onclick={onMoveUp}
			disabled={isFirst}
			class="h-6 w-6 p-0 hover:bg-muted"
			aria-label="Move up"
		>
			<ChevronUp class="h-4 w-4" />
		</Button>
		<Button
			size="icon"
			variant="ghost"
			onclick={onMoveDown}
			disabled={isLast}
			class="h-6 w-6 p-0 hover:bg-muted"
			aria-label="Move down"
		>
			<ChevronDown class="h-4 w-4" />
		</Button>
	</div>

	<!-- Icon -->
	<div class="p-2 rounded-md bg-primary/10 shrink-0">
		<Radio class="h-5 w-5 text-primary" />
	</div>

	<!-- Info -->
	<div class="flex-1 min-w-0">
		<p class="font-medium text-sm truncate">{item.channel.name}</p>
		{#if item.channel.description}
			<p class="text-xs text-muted-foreground truncate">{item.channel.description}</p>
		{/if}
	</div>

	<!-- Delete Button - right aligned -->
	<Button
		variant="ghost"
		size="icon"
		class="h-8 w-8 text-destructive hover:text-destructive hover:bg-destructive/10 shrink-0"
		onclick={onDelete}
		aria-label="Remove channel"
	>
		<Trash2 class="h-4 w-4" />
	</Button>
</div>
