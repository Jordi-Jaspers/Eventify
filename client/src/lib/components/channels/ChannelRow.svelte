<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Radio } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { truncateText } from '$lib/utils/string';
	import {
		getChannelStatusVariant,
		getChannelStatusLabel,
		copySlugToClipboard,
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
</script>

<div
	class="grid grid-cols-1 md:grid-cols-18 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all text-left w-full"
>
	<!-- Channel Name (clickable to copy slug) -->
	<div class="col-span-1 md:col-span-4">
		<button
			type="button"
			class="flex items-center gap-3 cursor-pointer hover:text-primary transition-colors text-left w-full"
			onclick={() => copySlugToClipboard(channel)}
			title="Click to copy slug: {channel.slug}"
		>
			<Radio class="h-5 w-5 text-primary shrink-0" />
			<div class="min-w-0">
				<div class="font-medium truncate">{channel.name}</div>
				<div class="text-sm text-muted-foreground truncate md:hidden">
					{truncateText(channel.description, 40)}
				</div>
			</div>
		</button>
	</div>

	<!-- Description (desktop only) -->
	<div class="hidden md:flex md:col-span-7 items-center">
		<span class="text-sm text-muted-foreground truncate">
			{truncateText(channel.description, 180)}
		</span>
	</div>

	<!-- Status with Stale Badge -->
	<div class="col-span-1 md:col-span-2 flex items-center gap-2">
		<Badge variant={getChannelStatusVariant(channel.status)}>
			{getChannelStatusLabel(channel.status)}
		</Badge>
		<StaleActivityBadge isStale={channel.isStale ?? false} />
	</div>

	<!-- Last Activity -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<span class="text-sm whitespace-nowrap" class:text-muted-foreground={isNoActivity}>
			<span class="md:hidden">Last: </span>
			{lastActivity}
		</span>
	</div>

	<!-- Created -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<span class="text-sm text-muted-foreground whitespace-nowrap">
			<span class="md:hidden">Created: </span>
			{formatDate(channel.createdAt ?? '')}
		</span>
	</div>

	<!-- Actions -->
	{#if canManage}
		<div class="col-span-1 md:col-span-1 flex justify-end">
			<ChannelActions {channel} {onEdit} {onPause} {onResume} {onDelete} />
		</div>
	{:else}
		<div class="col-span-1 md:col-span-1"></div>
	{/if}
</div>
