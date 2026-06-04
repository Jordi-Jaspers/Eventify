<script lang="ts">
	import { Pencil, Trash2 } from '@lucide/svelte';
	import { MattermostIcon, SlackIcon } from '$lib/components/icons';
	import { Button } from '$lib/components/ui/button';
	import type { AdapterConfigResponse } from '$lib/api/models';

	interface Props {
		config: AdapterConfigResponse;
		canEdit?: boolean;
		onEdit?: (config: AdapterConfigResponse) => void;
		onDelete?: (config: AdapterConfigResponse) => void;
	}

	let { config, canEdit = true, onEdit, onDelete }: Props = $props();

	const webhookUrl = $derived((): string | null => {
		const url = config.config?.['webhookUrl'];
		return typeof url === 'string' ? url : null;
	});

	const maskedUrl = $derived((): string => {
		const url = webhookUrl();
		if (!url) return '';
		try {
			const u = new URL(url);
			return `${u.host}/••••••`;
		} catch {
			return url.length > 30 ? url.slice(0, 12) + '/••••••' : url;
		}
	});
</script>

<div class="flex items-center gap-3 px-4 py-3 rounded-lg border border-border/50 bg-card/40 hover:bg-card/60 transition-colors">
	<!-- Icon -->
	<div class="shrink-0 p-1.5 rounded-md bg-muted/50">
		{#if config.adapterType === 'MATTERMOST'}
			<MattermostIcon class="w-4 h-4 text-muted-foreground" />
		{:else}
			<SlackIcon class="w-4 h-4 text-muted-foreground" />
		{/if}
	</div>

	<!-- Info -->
	<div class="flex-1 min-w-0">
		<div class="flex items-center gap-2 flex-wrap">
			<span class="text-sm font-medium">
				{config.adapterType === 'MATTERMOST' ? 'Mattermost' : 'Slack'}
			</span>
			{#if config.label}
				<span class="text-sm text-muted-foreground">· "{config.label}"</span>
			{/if}
		</div>
		{#if maskedUrl()}
			<p class="text-xs text-muted-foreground/70 mt-0.5">{maskedUrl()}</p>
		{/if}
	</div>

	<!-- Status -->
	{#if config.enabled}
		<span class="text-xs text-emerald-500 font-medium shrink-0">Enabled</span>
	{:else}
		<span class="text-xs text-muted-foreground font-medium shrink-0">Disabled</span>
	{/if}

	<!-- Actions -->
	{#if canEdit}
		<div class="flex items-center gap-1 shrink-0">
			{#if onEdit}
				<Button
					variant="ghost"
					size="sm"
					class="h-8 w-8 p-0"
					onclick={() => onEdit!(config)}
					aria-label="Edit {config.label}"
				>
					<Pencil class="w-3.5 h-3.5" />
				</Button>
			{/if}
			{#if onDelete}
				<Button
					variant="ghost"
					size="sm"
					class="h-8 w-8 p-0 text-destructive hover:text-destructive"
					onclick={() => onDelete!(config)}
					aria-label="Delete {config.label}"
				>
					<Trash2 class="w-3.5 h-3.5" />
				</Button>
			{/if}
		</div>
	{/if}
</div>
