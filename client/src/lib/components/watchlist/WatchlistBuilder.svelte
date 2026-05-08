<script lang="ts">
	import { onMount } from 'svelte';
	import { Radio } from '@lucide/svelte';
	import type { ChannelDetailsResponse, WatchlistDetailsResponse } from '$lib/api/models';
	import type { components } from '$lib/types/api';
	import type { ConfigItem, ConfigChannelItem, ConfigGroupItem } from './types';
	import { generateId } from './utils';
	import Configurator from './Configurator.svelte';
	import DetailsCard from './DetailsCard.svelte';
	import FiltersCard from './FiltersCard.svelte';
	import ChannelSelectSheet from './ChannelSelectSheet.svelte';
	import GroupNameSheet from './GroupNameSheet.svelte';

	type CreateWatchlistRequest = components['schemas']['CreateWatchlistRequest'];
	type UpdateWatchlistRequest = components['schemas']['UpdateWatchlistRequest'];
	type WatchlistConfigurationRequest = components['schemas']['WatchlistConfigurationRequest'];
	type WatchlistFiltersRequest = components['schemas']['WatchlistFiltersRequest'];
	type ChannelGroupRequest = components['schemas']['ChannelGroupRequest'];
	type TimeRange = NonNullable<WatchlistFiltersRequest['timeRange']>;

	interface Props {
		watchlist?: WatchlistDetailsResponse;
		allChannels: ChannelDetailsResponse[];
		onSave: (request: CreateWatchlistRequest | UpdateWatchlistRequest) => Promise<void>;
		isSaving: boolean;
		lastSaved?: Date | null;
	}

	let { watchlist, allChannels, onSave, isSaving, lastSaved }: Props = $props();

	// State
	let name: string = $state('');
	let description: string = $state('');
	let configItems: ConfigItem[] = $state([]);
	let timeRange: TimeRange = $state('24h');
	let onlyCritical: boolean = $state(false);
	let sortBySeverity: boolean = $state(false);
	let groupedView: boolean = $state(false);

	// Sheet states
	let channelSelectOpen: boolean = $state(false);
	let groupNameOpen: boolean = $state(false);
	let pendingChannelGroupId: string | undefined = $state(undefined);

	// Auto-save state
	let autoSaveTimer: ReturnType<typeof setTimeout> | null = null;
	let previousDataJson: string = $state('');
	let showSavedIndicator: boolean = $state(false);
	let savedIndicatorTimer: ReturnType<typeof setTimeout> | null = null;

	// Derived: Available channels (not already in configurator)
	const usedChannelIds: Set<number> = $derived(
		new Set(
			configItems.flatMap((item: ConfigItem) => {
				if (item.type === 'channel') {
					return [item.channelId];
				} else if (item.type === 'group') {
					return item.channels.map((c: ConfigChannelItem) => c.channelId);
				}
				return [];
			})
		)
	);

	const availableChannels: ChannelDetailsResponse[] = $derived(
		allChannels.filter((ch: ChannelDetailsResponse) => !usedChannelIds.has(ch.id ?? 0))
	);

	// Initialize from watchlist
	onMount(() => {
		if (watchlist) {
			name = watchlist.name ?? '';
			description = watchlist.description ?? '';
			timeRange = (watchlist.filters?.timeRange as TimeRange) ?? '24h';
			onlyCritical = watchlist.filters?.onlyCritical ?? false;
			sortBySeverity = watchlist.filters?.sortBySeverity ?? false;
			groupedView = watchlist.filters?.groupedView ?? false;

			// Parse configuration
			if (watchlist.configuration) {
				try {
					// Convert flat channelIds to ConfigChannelItems
					const channelItems: ConfigChannelItem[] = (watchlist.configuration.channelIds ?? [])
						.map((channelId: number) => {
							const channel: ChannelDetailsResponse | undefined = allChannels.find(
								(c: ChannelDetailsResponse) => c.id === channelId
							);
							if (channel) {
								return {
									id: generateId(),
									type: 'channel',
									channelId,
									channel
								} as ConfigChannelItem;
							}
							return null;
						})
						.filter((c): c is ConfigChannelItem => c !== null);

					// Convert groups to ConfigGroupItems
					const groupItems: ConfigGroupItem[] = (watchlist.configuration.groups ?? [])
						.map((group) => {
							const channels: ConfigChannelItem[] = (group.channelIds ?? [])
								.map((channelId: number) => {
									const channel: ChannelDetailsResponse | undefined = allChannels.find(
										(c: ChannelDetailsResponse) => c.id === channelId
									);
									if (channel) {
										return {
											id: generateId(),
											type: 'channel',
											channelId,
											channel
										} as ConfigChannelItem;
									}
									return null;
								})
								.filter((c): c is ConfigChannelItem => c !== null);

							return {
								id: group.id ?? generateId(),
								type: 'group',
								name: group.name ?? 'Unnamed Group',
								channels,
								isExpanded: true
							} as ConfigGroupItem;
						});

					configItems = [...groupItems, ...channelItems];
				} catch (error) {
					console.error('Failed to parse configuration:', error);
				}
			}

			// Set initial data JSON
			previousDataJson = buildRequestJson();
		} else {
			// For new watchlists, set initial data JSON
			previousDataJson = buildRequestJson();
		}
	});

	// Auto-save effect
	$effect(() => {
		// Dependencies: watch all state that affects the request
		const deps: unknown[] = [name, description, configItems, timeRange, onlyCritical, sortBySeverity, groupedView];
		
		// Skip if we're currently saving
		if (isSaving) return;

		// Skip if name is empty (required field)
		if (!name.trim()) return;

		// Build current data JSON
		const currentJson: string = buildRequestJson();

		// Skip if data hasn't actually changed
		if (currentJson === previousDataJson) return;

		// Clear existing timer
		if (autoSaveTimer) {
			clearTimeout(autoSaveTimer);
		}

		// Debounce: save after 1000ms of no changes
		autoSaveTimer = setTimeout(() => {
			triggerSave();
			previousDataJson = currentJson;
		}, 1000);
	});

	// Show "Saved" indicator when lastSaved changes
	$effect(() => {
		if (lastSaved) {
			showSavedIndicator = true;
			
			// Clear existing timer
			if (savedIndicatorTimer) {
				clearTimeout(savedIndicatorTimer);
			}

			// Hide indicator after 2.5s
			savedIndicatorTimer = setTimeout(() => {
				showSavedIndicator = false;
			}, 2500);
		}
	});

	function buildRequestJson(): string {
		// Extract standalone channel IDs (not in groups)
		const standaloneChannelIds: number[] = configItems
			.filter((item: ConfigItem) => item.type === 'channel')
			.map((item: ConfigChannelItem) => item.channelId);

		// Extract groups
		const groups: ChannelGroupRequest[] = configItems
			.filter((item: ConfigItem) => item.type === 'group')
			.map((item: ConfigGroupItem) => ({
				id: item.id,
				name: item.name,
				channelIds: item.channels.map((c: ConfigChannelItem) => c.channelId)
			}));

		const configuration: WatchlistConfigurationRequest = {
			channelIds: standaloneChannelIds,
			groups
		};

		const filters: WatchlistFiltersRequest = {
			timeRange,
			onlyCritical,
			sortBySeverity,
			groupedView
		};

		const request: CreateWatchlistRequest | UpdateWatchlistRequest = {
			name,
			description: description || undefined,
			configuration,
			filters
		};

		return JSON.stringify(request);
	}

	async function triggerSave(): Promise<void> {
		const requestJson: string = buildRequestJson();
		const request: CreateWatchlistRequest | UpdateWatchlistRequest = JSON.parse(requestJson);
		await onSave(request);
	}

	// Handlers
	function handleConfigUpdate(items: ConfigItem[]): void {
		configItems = items;
	}

	function openChannelSheet(groupId?: string): void {
		pendingChannelGroupId = groupId;
		channelSelectOpen = true;
	}

	function openGroupSheet(): void {
		groupNameOpen = true;
	}

	function handleChannelSelect(channel: ChannelDetailsResponse): void {
		const newChannel: ConfigChannelItem = {
			id: generateId(),
			type: 'channel',
			channelId: channel.id ?? 0,
			channel
		};
		
		// If a groupId is pending, add channel to that group
		if (pendingChannelGroupId) {
			const groupId: string = pendingChannelGroupId;
			pendingChannelGroupId = undefined;
			
			configItems = configItems.map((item: ConfigItem) => {
				if (item.type === 'group' && item.id === groupId) {
					return {
						...item,
						channels: [...item.channels, newChannel]
					};
				}
				return item;
			});
		} else {
			// Add to main list
			configItems = [...configItems, newChannel];
		}
	}

	function handleGroupCreate(groupName: string): void {
		const newGroup: ConfigGroupItem = {
			id: generateId(),
			type: 'group',
			name: groupName,
			channels: [],
			isExpanded: true
		};
		configItems = [...configItems, newGroup];
	}
</script>

<div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
	<!-- Left Column: Details + Filters -->
	<div class="space-y-6">
		<DetailsCard
			bind:name
			bind:description
			onNameChange={(n) => (name = n)}
			onDescriptionChange={(d) => (description = d)}
			{isSaving}
			{showSavedIndicator}
		/>
		<FiltersCard
			bind:timeRange
			bind:onlyCritical
			bind:sortBySeverity
			bind:groupedView
			onTimeRangeChange={(r) => (timeRange = r)}
			onOnlyCriticalChange={(v) => (onlyCritical = v)}
			onSortBySeverityChange={(v) => (sortBySeverity = v)}
			onGroupedViewChange={(v) => (groupedView = v)}
		/>
	</div>

	<!-- Right Column: Configurator (spans 2 columns) -->
	<div class="lg:col-span-2">
		<Configurator
			bind:items={configItems}
			onUpdate={handleConfigUpdate}
			onAddChannel={openChannelSheet}
			onAddGroup={openGroupSheet}
		/>
	</div>
</div>

<!-- Sheets -->
<ChannelSelectSheet
	bind:open={channelSelectOpen}
	onOpenChange={(open) => (channelSelectOpen = open)}
	{availableChannels}
	onSelect={handleChannelSelect}
/>

<GroupNameSheet
	bind:open={groupNameOpen}
	onOpenChange={(open) => (groupNameOpen = open)}
	onSubmit={handleGroupCreate}
/>
