<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '$lib/components/ui/collapsible';
	import { ChevronDown, FolderTree } from '@lucide/svelte';
	import TimelineBar from './TimelineBar.svelte';
	import ChannelCard from './ChannelCard.svelte';
	import type { ChannelGroupResponse } from '$lib/api/models';

	interface Props {
		group: ChannelGroupResponse;
		rangeStart: Date;
		rangeEnd: Date;
	}

	let { group, rangeStart, rangeEnd }: Props = $props();

	let isExpanded: boolean = $state(false);
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-sm">
	<CardContent class="p-4">
		<Collapsible bind:open={isExpanded}>
			<div class="space-y-3">
				<!-- Group header -->
				<div class="flex items-center justify-between">
					<div class="flex items-center gap-3">
						<FolderTree class="h-4 w-4 text-primary" />
						<h3 class="font-medium text-foreground">{group.name}</h3>
						<span class="text-xs text-muted-foreground">
							({group.channels?.length ?? 0} channels)
						</span>
					</div>

					<CollapsibleTrigger class="p-1 rounded hover:bg-muted transition-colors">
						<ChevronDown
							class="h-4 w-4 transition-transform {isExpanded ? 'rotate-180' : ''}"
						/>
					</CollapsibleTrigger>
				</div>

				<!-- Group consolidated timeline -->
				{#if group.timeline}
					<TimelineBar
						timeline={group.timeline}
						{rangeStart}
						{rangeEnd}
					/>
				{/if}

				<!-- Member channels -->
				<CollapsibleContent>
					{#if group.channels && group.channels.length > 0}
						<div class="mt-4 space-y-2 pl-6 border-l-2 border-border/30">
							{#each group.channels as channel, idx (`group-channel-${idx}-${channel.channelId}`)}
								<ChannelCard
									{channel}
									{rangeStart}
									{rangeEnd}
								/>
							{/each}
						</div>
					{:else}
						<div class="mt-4 pl-6 text-sm text-muted-foreground">
							No channels in this group
						</div>
					{/if}
				</CollapsibleContent>
			</div>
		</Collapsible>
	</CardContent>
</Card>
