<script lang="ts">
	import { untrack } from 'svelte';
	import { dndzone } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';
	import type {
		ChannelDetailsResponse,
		WatchlistDetailsResponse
	} from '$lib/api/models';
	import type { components } from '$lib/types/api';
	import { searchChannels } from '$lib/api/channel/UserChannelController';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { Textarea } from '$lib/components/ui/textarea';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import * as Sheet from '$lib/components/ui/sheet';
	import { Search, Check, Save, Radio, GripVertical, X, Plus } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';

	type CreateWatchlistRequest = components['schemas']['CreateWatchlistRequest'];
	type UpdateWatchlistRequest = components['schemas']['UpdateWatchlistRequest'];

	interface Props {
		watchlist?: WatchlistDetailsResponse | null;
		allChannels: ChannelDetailsResponse[];
		onSave: (request: CreateWatchlistRequest | UpdateWatchlistRequest) => Promise<void>;
		isSaving?: boolean;
		lastSaved?: Date | null;
	}

	let {
		watchlist = null,
		allChannels,
		onSave,
		isSaving = false,
		lastSaved = null
	}: Props = $props();

	// Form state - intentionally capture initial values only (form controls own state after mount)
	let name: string = $state(untrack(() => watchlist?.name ?? ''));
	let description: string = $state(untrack(() => watchlist?.description ?? ''));
	let timeRange: string = $state(untrack(() => watchlist?.filters?.timeRange ?? '24h'));
	let onlyCritical: boolean = $state(untrack(() => watchlist?.filters?.onlyCritical ?? false));
	let sortBySeverity: boolean = $state(untrack(() => watchlist?.filters?.sortBySeverity ?? true));

	// Selected channels (ordered list of channel objects for display)
	interface SelectedChannel {
		id: number;
		channel: ChannelDetailsResponse;
	}

	// Build initial selected channels - intentionally capture initial value only
	function buildInitialChannels(): SelectedChannel[] {
		return (watchlist?.configuration?.channelIds ?? [])
			.map((id: number) => {
				const channel: ChannelDetailsResponse | undefined = allChannels.find(
					(c: ChannelDetailsResponse) => c.id === id
				);
				return channel ? { id: channel.id ?? 0, channel } : null;
			})
			.filter((item): item is SelectedChannel => item !== null);
	}

	let selectedChannels: SelectedChannel[] = $state(untrack(() => buildInitialChannels()));

	// Channel search modal state
	let showChannelModal: boolean = $state(false);
	let channelSearch: string = $state('');
	let searchResults: ChannelDetailsResponse[] = $state([]);
	let isSearching: boolean = $state(false);

	// Building blocks (static list)
	interface BuildingBlock {
		id: string;
		type: 'channel';
		label: string;
		icon: typeof Radio;
	}

	const buildingBlocks: BuildingBlock[] = [
		{ id: 'channel-block', type: 'channel', label: 'Channel', icon: Radio }
	];

	// Drop zone placeholder for detecting drops
	let dropZoneItems: { id: string }[] = $state([]);

	// Validation
	let nameError: string = $state('');

	function validate(): boolean {
		nameError = '';
		if (!name.trim()) {
			nameError = 'Name is required';
			return false;
		}
		return true;
	}

	// Auto-save tracking
	let isDirty: boolean = $state(false);
	let saveTimeout: ReturnType<typeof setTimeout> | null = null;

	// Track changes for auto-save (edit mode only)
	$effect(() => {
		// Watch all form fields
		void name;
		void description;
		void timeRange;
		void onlyCritical;
		void sortBySeverity;
		void selectedChannels.length;

		if (watchlist?.id) {
			isDirty = true;
			if (saveTimeout) clearTimeout(saveTimeout);
			saveTimeout = setTimeout(() => {
				if (isDirty && watchlist?.id) {
					handleAutoSave();
				}
			}, 1000);
		}
	});

	async function handleAutoSave(): Promise<void> {
		if (!watchlist?.id || !validate()) return;

		const request: UpdateWatchlistRequest = {
			name,
			description,
			configuration: { channelIds: selectedChannels.map((sc: SelectedChannel) => sc.id) },
			filters: { timeRange, onlyCritical, sortBySeverity }
		};

		await onSave(request);
		isDirty = false;
	}

	async function handleManualSave(): Promise<void> {
		if (!validate()) return;

		const request: CreateWatchlistRequest | UpdateWatchlistRequest = {
			name,
			description,
			configuration: { channelIds: selectedChannels.map((sc: SelectedChannel) => sc.id) },
			filters: { timeRange, onlyCritical, sortBySeverity }
		};

		await onSave(request);
		isDirty = false;
	}

	// Handle drop zone - when a building block is dropped, open modal
	function handleDropZoneConsider(e: CustomEvent<{ items: { id: string }[] }>): void {
		const items: { id: string }[] = e.detail.items;
		// Check if a building block was added
		const newBlock = items.find((item: { id: string }) => item.id === 'channel-block');
		if (newBlock) {
			// Remove the placeholder immediately and open modal
			dropZoneItems = [];
			showChannelModal = true;
			channelSearch = '';
			searchResults = [];
		} else {
			dropZoneItems = items;
		}
	}

	function handleDropZoneFinalize(e: CustomEvent<{ items: { id: string }[] }>): void {
		const items: { id: string }[] = e.detail.items;
		const newBlock = items.find((item: { id: string }) => item.id === 'channel-block');
		if (newBlock) {
			dropZoneItems = [];
			showChannelModal = true;
			channelSearch = '';
			searchResults = [];
		} else {
			dropZoneItems = items;
		}
	}

	// Handle reordering selected channels
	function handleSelectedConsider(e: CustomEvent<{ items: SelectedChannel[] }>): void {
		selectedChannels = e.detail.items;
	}

	function handleSelectedFinalize(e: CustomEvent<{ items: SelectedChannel[] }>): void {
		selectedChannels = e.detail.items;
	}

	// Channel search
	async function searchForChannels(): Promise<void> {
		if (!channelSearch.trim()) {
			searchResults = [];
			return;
		}

		isSearching = true;
		try {
			const response = await searchChannels({
				pageNumber: 0,
				pageSize: 20,
				searchInputs: [
					{
						fieldName: 'search',
						textValue: channelSearch
					}
				]
			});
			// Filter out already selected channels
			const selectedIds: number[] = selectedChannels.map((sc: SelectedChannel) => sc.id);
			searchResults = (response.content ?? []).filter(
				(c: ChannelDetailsResponse) => !selectedIds.includes(c.id ?? 0)
			);
		} catch {
			toast.error('Failed to search channels');
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	// Debounced search
	let searchTimeout: ReturnType<typeof setTimeout> | null = null;
	function handleSearchInput(): void {
		if (searchTimeout) clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => searchForChannels(), 300);
	}

	// Add channel from modal
	function addChannel(channel: ChannelDetailsResponse): void {
		if (!channel.id) return;
		selectedChannels = [...selectedChannels, { id: channel.id, channel }];
		showChannelModal = false;
		channelSearch = '';
		searchResults = [];
	}

	// Remove channel
	function removeChannel(channelId: number): void {
		selectedChannels = selectedChannels.filter((sc: SelectedChannel) => sc.id !== channelId);
	}

	// Save status indicator
	let showSaved: boolean = $state(false);
	$effect(() => {
		if (lastSaved) {
			showSaved = true;
			const timeout: ReturnType<typeof setTimeout> = setTimeout(() => {
				showSaved = false;
			}, 2000);
			return () => clearTimeout(timeout);
		}
	});
</script>

<div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
	<!-- Left Column: Details + Building Blocks -->
	<div class="lg:col-span-4 space-y-6">
		<!-- Watchlist Details -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
			<CardHeader>
				<CardTitle class="flex items-center justify-between">
					<span>Details</span>
					{#if watchlist?.id}
						<span class="text-sm font-normal">
							{#if isSaving}
								<span class="text-muted-foreground">Saving...</span>
							{:else if showSaved}
								<span class="text-primary flex items-center gap-1">
									<Check class="h-4 w-4" />
									Saved
								</span>
							{/if}
						</span>
					{/if}
				</CardTitle>
			</CardHeader>
			<CardContent class="space-y-4">
				<!-- Name -->
				<div class="space-y-2">
					<Label for="name">
						Name <span class="text-destructive" aria-label="required">*</span>
					</Label>
					<Input
						id="name"
						bind:value={name}
						placeholder="My Watchlist"
						class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
						aria-invalid={!!nameError}
						aria-describedby={nameError ? 'name-error' : undefined}
					/>
					{#if nameError}
						<p id="name-error" class="text-sm text-destructive">{nameError}</p>
					{/if}
				</div>

				<!-- Description -->
				<div class="space-y-2">
					<Label for="description">Description</Label>
					<Textarea
						id="description"
						bind:value={description}
						placeholder="Optional description..."
						rows={2}
						class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
					/>
				</div>

				<!-- Default Filters -->
				<div class="space-y-4 pt-4 border-t border-border/50">
					<h3 class="font-medium text-sm">Default Filters</h3>

					<!-- Time Range -->
					<div class="space-y-2">
						<Label class="text-xs text-muted-foreground">Time Range</Label>
						<div class="flex gap-2">
							<Button
								type="button"
								variant={timeRange === '24h' ? 'default' : 'outline'}
								size="sm"
								onclick={() => (timeRange = '24h')}
								class={timeRange === '24h' ? 'bg-gradient-to-r from-primary to-primary/80 text-primary-foreground' : ''}
							>
								24h
							</Button>
							<Button
								type="button"
								variant={timeRange === '7d' ? 'default' : 'outline'}
								size="sm"
								onclick={() => (timeRange = '7d')}
								class={timeRange === '7d' ? 'bg-gradient-to-r from-primary to-primary/80 text-primary-foreground' : ''}
							>
								7d
							</Button>
							<Button
								type="button"
								variant={timeRange === '30d' ? 'default' : 'outline'}
								size="sm"
								onclick={() => (timeRange = '30d')}
								class={timeRange === '30d' ? 'bg-gradient-to-r from-primary to-primary/80 text-primary-foreground' : ''}
							>
								30d
							</Button>
						</div>
					</div>

					<!-- Checkboxes -->
					<div class="space-y-2">
						<div class="flex items-center space-x-2">
							<Checkbox id="onlyCritical" bind:checked={onlyCritical} />
							<Label for="onlyCritical" class="cursor-pointer font-normal text-sm">
								Only critical events
							</Label>
						</div>

						<div class="flex items-center space-x-2">
							<Checkbox id="sortBySeverity" bind:checked={sortBySeverity} />
							<Label for="sortBySeverity" class="cursor-pointer font-normal text-sm">
								Sort by severity
							</Label>
						</div>
					</div>
				</div>
			</CardContent>
		</Card>

		<!-- Building Blocks -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
			<CardHeader>
				<CardTitle class="text-lg">Building Blocks</CardTitle>
			</CardHeader>
			<CardContent>
				<p class="text-sm text-muted-foreground mb-4">
					Drag blocks to the configuration panel
				</p>
				<div class="space-y-2">
					{#each buildingBlocks as block (block.id)}
						<button
							type="button"
							draggable="true"
							ondragstart={(e) => {
								e.dataTransfer?.setData('text/plain', block.id);
							}}
							onclick={() => {
								if (block.id === 'channel-block') {
									showChannelModal = true;
									channelSearch = '';
									searchResults = [];
								}
							}}
							class="w-full flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-card/80 hover:bg-card hover:border-primary/50 cursor-grab active:cursor-grabbing transition-all text-left group"
						>
							<div class="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center group-hover:scale-105 transition-transform">
								<block.icon class="h-5 w-5 text-primary" />
							</div>
							<div>
								<div class="font-medium">{block.label}</div>
								<div class="text-xs text-muted-foreground">Drag or click to add</div>
							</div>
						</button>
					{/each}
				</div>
			</CardContent>
		</Card>

		<!-- Action Buttons (create mode) -->
		{#if !watchlist?.id}
			<div class="flex flex-col gap-3">
				<Button
					onclick={handleManualSave}
					disabled={isSaving}
					class="w-full bg-gradient-to-r from-primary to-primary/80 hover:opacity-90 transition-all shadow-lg text-primary-foreground"
				>
					{#if isSaving}
						<Save class="mr-2 h-4 w-4 animate-spin" />
						Creating...
					{:else}
						Create Watchlist
					{/if}
				</Button>
				<Button variant="outline" class="w-full" onclick={() => window.history.back()}>
					Cancel
				</Button>
			</div>
		{/if}
	</div>

	<!-- Right Column: Configuration (Channels) -->
	<div class="lg:col-span-8">
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg h-full">
			<CardHeader>
				<CardTitle class="flex items-center justify-between">
					<span>Configuration</span>
					<Button
						variant="outline"
						size="sm"
						onclick={() => {
							showChannelModal = true;
							channelSearch = '';
							searchResults = [];
						}}
						class="gap-2"
					>
						<Plus class="h-4 w-4" />
						Add Channel
					</Button>
				</CardTitle>
			</CardHeader>
			<CardContent>
				{#if selectedChannels.length === 0}
					<div
						role="region"
						aria-label="Drop zone for channels"
						class="py-16 border-2 border-dashed border-border/50 rounded-lg text-center min-h-[400px] flex flex-col items-center justify-center"
						ondragover={(e) => e.preventDefault()}
						ondrop={(e) => {
							e.preventDefault();
							const blockId = e.dataTransfer?.getData('text/plain');
							if (blockId === 'channel-block') {
								showChannelModal = true;
								channelSearch = '';
								searchResults = [];
							}
						}}
					>
						<Radio class="h-16 w-16 text-muted-foreground/30 mb-4" />
						<p class="text-muted-foreground text-lg mb-2">No channels configured</p>
						<p class="text-muted-foreground/70 text-sm">
							Drag a Channel block here or click "Add Channel"
						</p>
					</div>
				{:else}
					<div
						role="list"
						aria-label="Selected channels"
						use:dndzone={{
							items: selectedChannels,
							flipDurationMs: 200,
							dropTargetStyle: { outline: '2px dashed hsl(var(--primary))' }
						}}
						onconsider={handleSelectedConsider}
						onfinalize={handleSelectedFinalize}
						class="space-y-2 min-h-[400px]"
						ondragover={(e) => e.preventDefault()}
						ondrop={(e) => {
							e.preventDefault();
							const blockId = e.dataTransfer?.getData('text/plain');
							if (blockId === 'channel-block') {
								showChannelModal = true;
								channelSearch = '';
								searchResults = [];
							}
						}}
					>
						{#each selectedChannels as item (item.id)}
							<div animate:flip={{ duration: 200 }}>
								<div class="flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-card/80 hover:bg-card transition-all group">
									<div class="cursor-grab active:cursor-grabbing touch-none">
										<GripVertical class="h-5 w-5 text-muted-foreground" />
									</div>

									<div class="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
										<Radio class="h-5 w-5 text-primary" />
									</div>

									<div class="flex-1 min-w-0">
										<div class="font-medium truncate">{item.channel.name}</div>
										<div class="text-sm text-muted-foreground truncate">
											{item.channel.description || 'No description'}
										</div>
									</div>

									<Button
										variant="ghost"
										size="icon"
										class="h-8 w-8 opacity-0 group-hover:opacity-100 focus:opacity-100 hover:bg-destructive/10 hover:text-destructive transition-all"
										onclick={() => removeChannel(item.id)}
										aria-label="Remove channel"
									>
										<X class="h-4 w-4" />
									</Button>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</CardContent>
		</Card>
	</div>
</div>

<!-- Channel Search Sheet -->
<Sheet.Root bind:open={showChannelModal}>
	<Sheet.Content side="right" class="sm:max-w-lg">
		<Sheet.Header>
			<Sheet.Title>Add Channel</Sheet.Title>
			<Sheet.Description>
				Search for a channel to add to your watchlist
			</Sheet.Description>
		</Sheet.Header>

		<div class="space-y-4 py-4">
			<!-- Search Input -->
			<div class="relative">
				<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
				<Input
					bind:value={channelSearch}
					oninput={handleSearchInput}
					placeholder="Search channels..."
					class="pl-10"
				/>
			</div>

			<!-- Search Results -->
			<div class="max-h-[calc(100vh-250px)] overflow-y-auto space-y-2">
				{#if isSearching}
					<div class="py-8 text-center text-muted-foreground">
						Searching...
					</div>
				{:else if channelSearch && searchResults.length === 0}
					<div class="py-8 text-center text-muted-foreground">
						No channels found matching "{channelSearch}"
					</div>
				{:else if searchResults.length > 0}
					{#each searchResults as channel (channel.id)}
						<button
							type="button"
							onclick={() => addChannel(channel)}
							class="w-full flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-card/50 hover:bg-card hover:border-primary/50 transition-all text-left"
						>
							<div class="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
								<Radio class="h-5 w-5 text-primary" />
							</div>
							<div class="flex-1 min-w-0">
								<div class="font-medium truncate">{channel.name}</div>
								<div class="text-sm text-muted-foreground truncate">
									{channel.description || 'No description'}
								</div>
							</div>
							<Plus class="h-5 w-5 text-muted-foreground" />
						</button>
					{/each}
				{:else}
					<div class="py-8 text-center text-muted-foreground">
						Start typing to search for channels
					</div>
				{/if}
			</div>
		</div>

		<Sheet.Footer>
			<Button variant="outline" onclick={() => (showChannelModal = false)}>
				Cancel
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
