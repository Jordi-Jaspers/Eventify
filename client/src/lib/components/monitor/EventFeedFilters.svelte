<script lang="ts">
	import { ChevronDown, Radio, X } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { MultiEnumFilter } from '$lib/components/data-table/filters';
	import type { EventFeedChannel, EventFeedSeverity } from '$lib/api/event/service/EventFeedService.svelte';

	interface Props {
		channels: EventFeedChannel[];
		selectedChannelIds: number[];
		selectedSeverities: EventFeedSeverity[];
		isLive: boolean;
		hasFilters: boolean;
		onToggleChannel: (id: number) => void;
		onSeverityChange: (values: string[]) => void;
		onClearFilters: () => void;
		onToggleLive: () => void;
	}

	const SEVERITY_OPTIONS: { value: string; label: string }[] = [
		{ value: 'CRITICAL', label: 'Critical' },
		{ value: 'WARNING', label: 'Warning' },
		{ value: 'OK', label: 'Ok' },
		{ value: 'NO_DATA', label: 'No Data' }
	];

	let {
		channels,
		selectedChannelIds,
		selectedSeverities,
		isLive,
		hasFilters,
		onToggleChannel,
		onSeverityChange,
		onClearFilters,
		onToggleLive
	}: Props = $props();

	let openFilter: string | null = $state(null);

	const selectedChannelCount: number = $derived(selectedChannelIds.length);
	const selectedSeverityCount: number = $derived(selectedSeverities.length);

	function getChannelName(id: number): string {
		return channels.find((c: EventFeedChannel) => c.id === id)?.name ?? String(id);
	}

	function getSeverityLabel(sev: string): string {
		return SEVERITY_OPTIONS.find((o) => o.value === sev)?.label ?? sev;
	}
</script>

<!-- Click-outside overlay -->
{#if openFilter}
	<button
		type="button"
		class="fixed inset-0 z-[99] cursor-default"
		aria-label="Close filter"
		onclick={() => { openFilter = null; }}
	></button>
{/if}

<div class="relative z-[100] rounded-xl border border-border/50 bg-card/50 backdrop-blur-xl shadow-lg mb-4 px-4 py-3">
	<div class="space-y-2">
		<div class="flex items-center gap-2 flex-wrap">
			<!-- Channels filter -->
			{#if channels.length > 0}
				<div class="relative z-50">
					<button
						onclick={() => { openFilter = openFilter === 'channels' ? null : 'channels'; }}
						class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap
							{selectedChannelCount > 0
								? 'bg-primary/10 border-primary/30 text-primary'
								: openFilter === 'channels'
									? 'bg-background/50 border-primary text-primary'
									: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
					>
						Channels
						{#if selectedChannelCount > 0}
							<span class="bg-primary text-primary-foreground rounded-full px-1.5 py-0 text-[10px] font-medium">
								{selectedChannelCount}
							</span>
						{/if}
						<ChevronDown class="h-3 w-3 transition-transform {openFilter === 'channels' ? 'rotate-180' : ''}" />
					</button>

					{#if openFilter === 'channels'}
						<!-- svelte-ignore a11y_no_static_element_interactions -->
						<div
							class="absolute top-full left-0 mt-1 z-50 min-w-[180px] rounded-lg border border-border/50 bg-card shadow-xl p-2"
							onmousedown={(e: MouseEvent) => e.stopPropagation()}
						>
							<div class="flex flex-col gap-1">
								{#each channels as ch (ch.id)}
									<label class="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-muted/40 cursor-pointer text-sm">
										<Checkbox
											checked={selectedChannelIds.includes(ch.id)}
											onCheckedChange={() => onToggleChannel(ch.id)}
										/>
										<span class="truncate">{ch.name}</span>
									</label>
								{/each}
							</div>
						</div>
					{/if}
				</div>
			{/if}

			<!-- Severity filter -->
			<div class="relative z-50">
				<button
					onclick={() => { openFilter = openFilter === 'severity' ? null : 'severity'; }}
					class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap
						{selectedSeverityCount > 0
							? 'bg-primary/10 border-primary/30 text-primary'
							: openFilter === 'severity'
								? 'bg-background/50 border-primary text-primary'
								: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
				>
					Severity
					{#if selectedSeverityCount > 0}
						<span class="bg-primary text-primary-foreground rounded-full px-1.5 py-0 text-[10px] font-medium">
							{selectedSeverityCount}
						</span>
					{/if}
					<ChevronDown class="h-3 w-3 transition-transform {openFilter === 'severity' ? 'rotate-180' : ''}" />
				</button>

				{#if openFilter === 'severity'}
					<!-- svelte-ignore a11y_no_static_element_interactions -->
					<div
						class="absolute top-full left-0 mt-1 z-50 min-w-[220px] rounded-lg border border-border/50 bg-card shadow-xl p-3"
						onmousedown={(e: MouseEvent) => e.stopPropagation()}
					>
						<MultiEnumFilter
							value={selectedSeverities}
							options={SEVERITY_OPTIONS}
							onChange={onSeverityChange}
						/>
					</div>
				{/if}
			</div>

			<!-- Live toggle -->
			<button
				onclick={onToggleLive}
				class="inline-flex items-center gap-1.5 h-9 px-3 text-xs font-medium rounded-lg border transition-colors ml-auto
					{isLive
						? 'bg-green-500/10 text-green-500 border-green-500/30'
						: 'bg-background/50 border-border/50 text-muted-foreground hover:text-foreground hover:border-border'}"
				title="Toggle live mode"
			>
				{#if isLive}
					<span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
				{:else}
					<Radio class="h-3 w-3" />
				{/if}
				Live
			</button>

			<!-- Clear all -->
			{#if hasFilters}
				<Button
					variant="ghost"
					size="sm"
					onclick={onClearFilters}
					class="text-xs text-muted-foreground hover:text-destructive h-9 px-2"
				>
					<X class="h-3 w-3 mr-1" />
					Clear
				</Button>
			{/if}
		</div>

		<!-- Active filter chips -->
		{#if hasFilters}
			<div class="flex items-center gap-1.5 flex-wrap">
				{#each selectedChannelIds as id (id)}
					<span class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md bg-primary/10 text-primary border border-primary/20">
						{getChannelName(id)}
						<button
							onclick={() => onToggleChannel(id)}
							class="hover:bg-primary/20 rounded-full p-0.5"
							aria-label="Remove channel filter"
						>
							<X class="h-2.5 w-2.5" />
						</button>
					</span>
				{/each}
				{#each selectedSeverities as sev (sev)}
					<span class="inline-flex items-center gap-1 h-6 px-2 text-xs rounded-md bg-primary/10 text-primary border border-primary/20">
						{getSeverityLabel(sev)}
						<button
							onclick={() => onSeverityChange(selectedSeverities.filter((s: EventFeedSeverity) => s !== sev))}
							class="hover:bg-primary/20 rounded-full p-0.5"
							aria-label="Remove severity filter"
						>
							<X class="h-2.5 w-2.5" />
						</button>
					</span>
				{/each}
			</div>
		{/if}
	</div>
</div>
