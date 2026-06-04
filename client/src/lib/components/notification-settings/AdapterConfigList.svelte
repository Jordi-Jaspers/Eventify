<script lang="ts">
	import type { Snippet } from 'svelte';
	import { MessageSquare } from '@lucide/svelte';
	import type { AdapterConfigResponse } from '$lib/api/models';
	import AdapterConfigCard from './AdapterConfigCard.svelte';

	interface Props {
		configs: AdapterConfigResponse[];
		loading?: boolean;
		canEdit?: boolean;
		editingId?: number | null;
		onEdit?: (config: AdapterConfigResponse) => void;
		onDelete?: (config: AdapterConfigResponse) => void;
		editForm?: Snippet;
	}

	let { configs, loading = false, canEdit = true, editingId = null, onEdit, onDelete, editForm }: Props = $props();
</script>

{#if loading}
	<div class="space-y-2">
		{#each [1, 2] as _}
			<div class="h-16 rounded-lg bg-muted/30 animate-pulse" aria-hidden="true"></div>
		{/each}
	</div>
{:else if configs.length === 0}
	<div class="flex flex-col items-center justify-center py-10 text-center">
		<div class="p-3 rounded-full bg-muted/30 mb-3">
			<MessageSquare class="w-6 h-6 text-muted-foreground" />
		</div>
		<p class="text-sm text-muted-foreground">No connections configured.</p>
		<p class="text-xs text-muted-foreground/60 mt-1 max-w-[240px]">
			Add Mattermost or Slack to receive notifications outside of Eventify.
		</p>
	</div>
{:else}
	<div class="space-y-2" role="list" aria-label="Additional notification connections">
		{#each configs as config (config.id)}
			<div role="listitem">
				{#if editingId === config.id && editForm}
					<div class="border border-border/50 rounded-lg p-4 bg-background/50 space-y-3">
						{@render editForm()}
					</div>
				{:else}
					<AdapterConfigCard {config} {canEdit} {onEdit} {onDelete} />
				{/if}
			</div>
		{/each}
	</div>
{/if}
