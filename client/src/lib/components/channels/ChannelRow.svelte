<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Radio } from '@lucide/svelte';
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
		selected?: boolean;
		onEdit: (channel: ChannelDetailsResponse) => void;
		onPause: (channel: ChannelDetailsResponse) => void;
		onResume: (channel: ChannelDetailsResponse) => void;
		onDelete: (channel: ChannelDetailsResponse) => void;
		onToggleSelect?: (channel: ChannelDetailsResponse) => void;
	}

	let { channel, canManage, selected = false, onEdit, onPause, onResume, onDelete, onToggleSelect }: Props = $props();

	const lastActivity: string = $derived(getRelativeActivityTime(channel.lastEventAt));
	const isNoActivity: boolean = $derived(!channel.lastEventAt);
	const isSelectable: boolean = $derived(!!onToggleSelect);

	const rowClass: string = $derived(
		`grid grid-cols-1 md:grid-cols-18 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all w-full${selected ? ' bg-primary/5' : ''}`
	);

	function handleRowClick(): void {
		onEdit(channel);
	}

	function handleActionsClick(event: MouseEvent): void {
		event.stopPropagation();
	}

	function handleCheckboxClick(event: MouseEvent): void {
		event.stopPropagation();
		onToggleSelect?.(channel);
	}
</script>

<div class={rowClass}>
	<!-- Checkbox (desktop, always col-span-1 for alignment) -->
	<!-- svelte-ignore a11y_click_events_have_key_events -->
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div class="hidden md:flex md:col-span-1 items-center justify-center">
		{#if isSelectable}
			<Checkbox
				checked={selected}
				onCheckedChange={() => onToggleSelect?.(channel)}
				class="cursor-pointer"
			/>
		{/if}
	</div>

	<!-- Clickable row content -->
	<button
		type="button"
		class="col-span-1 md:col-span-16 grid grid-cols-1 md:grid-cols-16 items-center gap-4 text-left w-full cursor-pointer"
		onclick={handleRowClick}
	>
		<!-- Channel Name with Stale Badge -->
		<div class="col-span-1 md:col-span-4">
			<div class="flex items-center gap-3">
				{#if isSelectable}
					<!-- Mobile checkbox -->
					<!-- svelte-ignore a11y_click_events_have_key_events -->
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div class="md:hidden" onclick={handleCheckboxClick}>
						<Checkbox
							checked={selected}
							class="cursor-pointer"
						/>
					</div>
				{/if}
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
		<div class="hidden md:flex md:col-span-8 items-center">
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
	</button>

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
</div>
