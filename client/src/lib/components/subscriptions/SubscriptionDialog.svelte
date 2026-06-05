<script lang="ts">
	import { BellRing, LoaderCircle, ExternalLink, Search } from '@lucide/svelte';
	import * as Dialog from '$lib/components/ui/dialog';
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Separator } from '$lib/components/ui/separator';
	import { Input } from '$lib/components/ui/input';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { listPersonalConfigs } from '$lib/api/notification/AdapterConfigController';
	import { searchWatchlists } from '$lib/api/watchlist/UserWatchlistController';
	import AdapterChecklist from './AdapterChecklist.svelte';
	import SeverityPicker from './SeverityPicker.svelte';
	import type {
		AdapterConfigResponse,
		CreateSubscriptionRequest,
		UpdateSubscriptionRequest,
		SubscriptionResponse,
		WatchlistDetailsResponse
	} from '$lib/api/models';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		open: boolean;
		saving: boolean;
		subscription?: SubscriptionResponse | null;
		onOpenChange: (open: boolean) => void;
		onSubmit: (data: CreateSubscriptionRequest | UpdateSubscriptionRequest) => Promise<void>;
		watchlistId?: number;
		/** Set to show org-note at the bottom */
		isOrgWatchlist?: boolean;
		/** Pre-loaded adapter configs — skips auto-fetch when provided */
		preloadedAdapterConfigs?: AdapterConfigResponse[];
	}

	const { open, saving, subscription = null, onOpenChange, onSubmit, watchlistId, isOrgWatchlist = false, preloadedAdapterConfigs }: Props = $props();

	const isEdit: boolean = $derived(subscription !== null);

	// Severity state
	let selectedSeverities: string[] = $state(['CRITICAL']);

	// Adapters
	let adapterConfigs: AdapterConfigResponse[] = $state([]);
	let selectedAdapterIds: Set<string> = $state(new Set());
	let loadingAdapters: boolean = $state(false);

	// Watchlist picker (create mode, no pre-set watchlistId)
	let watchlistSearchQuery: string = $state('');
	let watchlistResults: WatchlistDetailsResponse[] = $state([]);
	let searchingWatchlists: boolean = $state(false);
	let selectedWatchlist: WatchlistDetailsResponse | null = $state(null);
	let showWatchlistDropdown: boolean = $state(false);
	let watchlistSearchTimeout: ReturnType<typeof setTimeout>;

	const hasSelection: boolean = $derived(selectedSeverities.length > 0);
	const canSubmit: boolean = $derived(
		hasSelection && (isEdit || !!watchlistId || selectedWatchlist !== null)
	);

	function populateFromSubscription(sub: SubscriptionResponse | null): void {
		if (sub) {
			selectedSeverities = [...sub.targetSeverities];
			selectedAdapterIds = new Set(sub.adapterConfigIds.map(String));
		} else {
			selectedSeverities = ['CRITICAL'];
			selectedAdapterIds = new Set();
		}
		watchlistSearchQuery = '';
		watchlistResults = [];
		selectedWatchlist = null;
		showWatchlistDropdown = false;
	}

	async function loadAdapters(): Promise<void> {
		if (preloadedAdapterConfigs !== undefined) {
			adapterConfigs = preloadedAdapterConfigs;
			return;
		}
		loadingAdapters = true;
		try {
			adapterConfigs = await listPersonalConfigs();
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load adapter configs');
			toast.error(message);
		} finally {
			loadingAdapters = false;
		}
	}

	function toggleAdapter(id: string, checked: boolean): void {
		const next = new Set(selectedAdapterIds);
		if (checked) next.add(id);
		else next.delete(id);
		selectedAdapterIds = next;
	}

	function handleWatchlistSearchInput(): void {
		clearTimeout(watchlistSearchTimeout);
		selectedWatchlist = null;
		if (!watchlistSearchQuery.trim()) {
			watchlistResults = [];
			showWatchlistDropdown = false;
			return;
		}
		watchlistSearchTimeout = setTimeout(async () => {
			searchingWatchlists = true;
			try {
				const result = await searchWatchlists({
					pageNumber: 0,
					pageSize: 8,
					sortOrder: [],
					searchInputs: [{ fieldName: 'name', textValue: watchlistSearchQuery.trim() }]
				});
				watchlistResults = result.content ?? [];
				showWatchlistDropdown = true;
			} catch {
				watchlistResults = [];
			} finally {
				searchingWatchlists = false;
			}
		}, 300);
	}

	function selectWatchlist(wl: WatchlistDetailsResponse): void {
		selectedWatchlist = wl;
		watchlistSearchQuery = wl.name ?? '';
		showWatchlistDropdown = false;
	}

	async function handleSave(): Promise<void> {
		const severities = selectedSeverities as ('CRITICAL' | 'WARNING' | 'OK' | 'NO_DATA')[];
		if (severities.length === 0) {
			toast.error('Select at least one severity level');
			return;
		}

		const inAppIds = adapterConfigs
			.filter((c) => c.adapterType === 'IN_APP')
			.map((c) => String(c.id));
		const adapterConfigIds: string[] = Array.from(new Set([...selectedAdapterIds, ...inAppIds]));

		if (isEdit) {
			const payload: UpdateSubscriptionRequest = { targetSeverities: severities, adapterConfigIds };
			await onSubmit(payload);
		} else {
			const wId: number = watchlistId ?? (selectedWatchlist?.id ?? 0);
			if (!wId) {
				toast.error('Select a watchlist first');
				return;
			}
			const payload: CreateSubscriptionRequest = { watchlistId: wId, targetSeverities: severities, adapterConfigIds };
			await onSubmit(payload);
		}
	}

	function handleOpenChange(o: boolean): void {
		onOpenChange(o);
		if (!o) {
			populateFromSubscription(null);
		}
	}

	// Sync when dialog opens
	$effect(() => {
		if (open) {
			populateFromSubscription(subscription ?? null);
			loadAdapters();
		}
	});
</script>

<Dialog.Root {open} onOpenChange={handleOpenChange}>
	<Dialog.Content class="sm:max-w-[500px] border-border/50 bg-card/95 backdrop-blur-xl">
		<Dialog.Header>
			<Dialog.Title class="flex items-center gap-2 text-primary">
				<BellRing class="h-4 w-4" />
				{isEdit ? 'Edit Subscription' : 'New Subscription'}
			</Dialog.Title>
			<Dialog.Description>
				{isEdit
					? 'Update severity levels and delivery adapters for this subscription.'
					: 'Subscribe to severity changes on a watchlist.'}
			</Dialog.Description>
		</Dialog.Header>

		<div class="space-y-5 py-2">
			<!-- Watchlist Section -->
			<div class="space-y-2">
				<Label class="text-sm font-medium">Watchlist</Label>
				{#if isEdit}
					<div class="flex items-center gap-2 px-3 py-2 rounded-md border border-border/50 bg-muted/30 text-sm text-muted-foreground">
						Watchlist #{subscription?.watchlistId}
					</div>
				{:else if watchlistId}
					<div class="flex items-center gap-2 px-3 py-2 rounded-md border border-border/50 bg-muted/30 text-sm">
						Watchlist #{watchlistId}
					</div>
				{:else}
					<div class="space-y-1">
						<div class="relative">
							<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
							<Input
								type="text"
								placeholder="Search watchlists…"
								bind:value={watchlistSearchQuery}
								oninput={handleWatchlistSearchInput}
								class="pl-9 pr-9"
							/>
							{#if searchingWatchlists}
								<LoaderCircle class="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 animate-spin text-muted-foreground" />
							{/if}
						</div>
						{#if showWatchlistDropdown}
							<div class="max-h-48 overflow-y-auto rounded-md border border-border bg-card divide-y divide-border/40">
								{#if watchlistResults.length > 0}
									{#each watchlistResults as wl (wl.id)}
										<button
											type="button"
											class="flex w-full items-center justify-between px-3 py-2 text-sm hover:bg-accent transition-colors text-left"
											onclick={() => selectWatchlist(wl)}
										>
											<span class="font-medium truncate">{wl.name}</span>
											<span class="ml-2 shrink-0 text-xs text-muted-foreground">ID: {wl.id}</span>
										</button>
									{/each}
								{:else}
									<p class="px-3 py-2 text-sm text-muted-foreground">No watchlists found</p>
								{/if}
							</div>
						{/if}
					</div>
				{/if}
			</div>

			<Separator class="bg-border/30" />

			<!-- Severity Section -->
			<div class="space-y-3">
				<p class="text-sm font-medium">Severity Levels</p>
				<SeverityPicker
					selected={selectedSeverities}
					onChange={(s) => (selectedSeverities = s)}
				/>
			</div>

			<Separator class="bg-border/30" />

			<!-- Adapters Section -->
			<div class="space-y-3">
				<p class="text-sm font-medium">Deliver via</p>
				{#if loadingAdapters}
					<div class="flex items-center gap-2 text-sm text-muted-foreground">
						<LoaderCircle class="h-4 w-4 animate-spin" />
						Loading adapters…
					</div>
				{:else if adapterConfigs.length === 0}
					<p class="text-sm text-muted-foreground">
						No adapter configs found.
						<a
							href={CLIENT_ROUTES.PROFILE_NOTIFICATIONS_PAGE.path}
							class="underline text-primary inline-flex items-center gap-1"
						>
							Link adapters in Settings <ExternalLink class="h-3 w-3" />
						</a>
					</p>
				{:else}
					<AdapterChecklist
						configs={adapterConfigs}
						selectedIds={selectedAdapterIds}
						onToggle={toggleAdapter}
						idPrefix="sub-adapter"
					/>
				{/if}
			</div>

			{#if isOrgWatchlist}
				<p class="text-xs text-muted-foreground border border-border/40 rounded-md px-3 py-2 bg-muted/20">
					For org-wide notifications, configure in Org Settings → Subscriptions.
				</p>
			{/if}
		</div>

		<Dialog.Footer class="flex-col gap-2 sm:flex-col">
			<Button onclick={handleSave} disabled={saving || !canSubmit} class="w-full">
				{#if saving}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
				{/if}
				{isEdit ? 'Save Changes' : 'Subscribe'}
			</Button>
		</Dialog.Footer>
	</Dialog.Content>
</Dialog.Root>
