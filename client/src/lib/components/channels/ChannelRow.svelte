<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Radio } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { truncateText } from '$lib/utils/string';
	import {
		getChannelStatusVariant,
		getChannelStatusLabel,
		getRelativeActivityTime
	} from '$lib/utils/channel';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import ChannelActions from './ChannelActions.svelte';
	import StaleActivityBadge from './StaleActivityBadge.svelte';

	interface Props {
		channel: ChannelDetailsResponse;
		canManage: boolean;
		onEdit: (channel: ChannelDetailsResponse) => void;
		onPause: (channel: ChannelDetailsResponse) => void;
		onResume: (channel: ChannelDetailsResponse) => void;
		onDelete: (channel: ChannelDetailsResponse) => void;
	}

	let { channel, canManage, onEdit, onPause, onResume, onDelete }: Props = $props();
	
	const lastActivity: string = $derived(getRelativeActivityTime(channel.lastEventAt));
	const isNoActivity: boolean = $derived(!channel.lastEventAt);

	function handleRowClick(): void {
		onEdit(channel);
	}

	function handleActionsClick(event: MouseEvent): void {
		// Stop propagation to prevent row click from firing when clicking actions
		event.stopPropagation();
	}
</script>

<button
	type="button"
	class="grid grid-cols-1 md:grid-cols-16 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all text-left w-full cursor-pointer"
	onclick={handleRowClick}
>
	<!-- Channel Name with Stale Badge -->
	<div class="col-span-1 md:col-span-4">
		<div class="flex items-center gap-3">
			<Radio class="h-5 w-5 text-primary shrink-0" />
			<div class="min-w-0 flex items-center gap-2">
				<span class="font-medium truncate">{channel.name}</span>
				<StaleActivityBadge isStale={channel.isStale ?? false} />
			</div>
		</div>
		<div class="text-sm text-muted-foreground truncate md:hidden pl-8">
			{truncateText(channel.description, 40)}
		</div>
	</div>

	<!-- Description (desktop only) -->
	<div class="hidden md:flex md:col-span-7 items-center">
		<span class="text-sm text-muted-foreground truncate">
			{truncateText(channel.description, 180)}
		</span>
	</div>

	<!-- Status -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<Badge variant={getChannelStatusVariant(channel.status)}>
			{getChannelStatusLabel(channel.status)}
		</Badge>
	</div>

	<!-- Last Activity -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<span class="text-sm whitespace-nowrap" class:text-muted-foreground={isNoActivity}>
			<span class="md:hidden">Last: </span>
			{lastActivity}
		</span>
	</div>

	<!-- Actions -->
	{#if canManage}
		<!-- svelte-ignore a11y_click_events_have_key_events -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div class="col-span-1 md:col-span-1 flex justify-end" onclick={handleActionsClick}>
			<ChannelActions {channel} {onEdit} {onPause} {onResume} {onDelete} />
		</div>
	{:else}
		<div class="col-span-1 md:col-span-1"></div>
	{/if}
</button>
