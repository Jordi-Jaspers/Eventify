<script lang="ts">
	import { Lock } from '@lucide/svelte';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Label } from '$lib/components/ui/label';
	import type { AdapterConfigResponse } from '$lib/api/models';
	import type { Snippet } from 'svelte';
	import AdapterTypeIcon from './AdapterTypeIcon.svelte';

	interface Props {
		configs: AdapterConfigResponse[];
		selectedIds: Set<string>;
		onToggle: (id: string, checked: boolean) => void;
		/** HTML id prefix — must be unique per page if multiple instances coexist */
		idPrefix?: string;
		/** Rendered inside the list when no external adapters exist */
		emptyState?: Snippet;
	}

	const { configs, selectedIds, onToggle, idPrefix = 'adapter', emptyState }: Props = $props();

	const hasExternalConfigs = $derived(configs.some((c) => c.adapterType !== 'IN_APP'));
</script>

<div class="rounded-md border border-border/50 divide-y divide-border/40">
	{#each configs as config}
		{#if config.adapterType === 'IN_APP'}
			<div class="flex items-center gap-3 px-3 py-2.5">
				<Checkbox id="{idPrefix}-{config.id}" checked={true} disabled />
				<Label
					for="{idPrefix}-{config.id}"
					class="text-sm cursor-default flex items-center gap-1.5 grow"
				>
					<AdapterTypeIcon type={config.adapterType} />
					{config.label}
					<span
						class="inline-flex items-center gap-1 text-xs text-muted-foreground font-normal ml-auto"
					>
						<Lock class="h-3 w-3" />
						Always on
					</span>
				</Label>
			</div>
		{:else}
			{@const checked = selectedIds.has(String(config.id))}
			<div class="flex items-center gap-3 px-3 py-2.5">
				<Checkbox
					id="{idPrefix}-{config.id}"
					{checked}
					onCheckedChange={(v) => onToggle(String(config.id), !!v)}
				/>
				<Label
					for="{idPrefix}-{config.id}"
					class="text-sm cursor-pointer flex items-center gap-1.5"
				>
					<AdapterTypeIcon type={config.adapterType} />
					{config.label}
					<span class="text-xs text-muted-foreground font-normal">({config.adapterType})</span>
				</Label>
			</div>
		{/if}
	{/each}
	{#if !hasExternalConfigs && emptyState}
		{@render emptyState()}
	{/if}
</div>
