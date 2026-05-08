<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { WatchlistBuilder } from '$lib/components/watchlist';
	import { searchOrganizationChannels } from '$lib/api/organization/OrganizationChannelController';
	import {
		getWatchlist,
		updateWatchlist
	} from '$lib/api/watchlist/OrganizationWatchlistController';
	import type { ChannelDetailsResponse, WatchlistDetailsResponse } from '$lib/api/models';
	import type { components } from '$lib/types/api';
	import { Button } from '$lib/components/ui/button';
	import { ArrowLeft, Eye } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { organizationStore } from '$lib/stores/organization.svelte';

	type UpdateWatchlistRequest = components['schemas']['UpdateWatchlistRequest'];

	const orgId: number = $derived(Number($page.params.orgId));
	const watchlistId: number = $derived(Number($page.params.id));

	// Role-based access check
	const canManage: boolean = $derived(
		organizationStore.currentRole === 'OWNER' || organizationStore.currentRole === 'ADMIN'
	);

	let watchlist: WatchlistDetailsResponse | null = $state(null);
	let allChannels: ChannelDetailsResponse[] = $state([]);
	let loading: boolean = $state(true);
	let isSaving: boolean = $state(false);
	let lastSaved: Date | null = $state(null);

	async function loadData(): Promise<void> {
		loading = true;
		try {
			const [watchlistData, channelsResponse] = await Promise.all([
				getWatchlist(orgId, watchlistId),
				searchOrganizationChannels(orgId, { pageNumber: 0, pageSize: 1000 })
			]);

			watchlist = watchlistData;
			allChannels = channelsResponse.content ?? [];
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load watchlist');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function handleSave(request: UpdateWatchlistRequest): Promise<void> {
		isSaving = true;
		const startTime = Date.now();
		const MIN_SAVE_DURATION = 600; // Minimum ms to show saving indicator

		try {
			const updated: WatchlistDetailsResponse = await updateWatchlist(orgId, watchlistId, request);
			watchlist = updated;
			lastSaved = new Date();
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to save watchlist');
			toast.error(message);
		} finally {
			// Ensure spinner shows for at least MIN_SAVE_DURATION
			const elapsed = Date.now() - startTime;
			const remaining = MIN_SAVE_DURATION - elapsed;
			if (remaining > 0) {
				await new Promise((resolve) => setTimeout(resolve, remaining));
			}
			isSaving = false;
		}
	}

	function handleMonitor(): void {
		goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/monitor?id=${watchlistId}`);
	}

	onMount(() => {
		if (!canManage) {
			toast.error("You don't have permission to edit watchlists");
			goto(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path);
			return;
		}
		loadData();
	});
</script>

<svelte:head>
	<title>{watchlist?.name ?? 'Edit Watchlist'} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between mb-8">
			<div class="flex items-center gap-4">
				<Button
					variant="ghost"
					size="icon"
					onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path)}
					aria-label="Go back"
				>
					<ArrowLeft class="h-5 w-5" />
				</Button>
				<div>
					<h1 class="text-3xl font-bold text-foreground">
						{#if watchlist}
							Edit "{watchlist.name}"
						{:else}
							Edit Watchlist
						{/if}
					</h1>
					<p class="text-muted-foreground mt-2">
						Make changes to your organization watchlist - they'll be saved automatically
					</p>
				</div>
			</div>
			{#if watchlist}
				<Button
					onclick={handleMonitor}
					class="gap-2"
				>
					<Eye class="h-4 w-4" />
					Monitor
				</Button>
			{/if}
		</div>

		<!-- Builder -->
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="text-muted-foreground">Loading watchlist...</div>
			</div>
		{:else if watchlist}
			<WatchlistBuilder {watchlist} {allChannels} onSave={handleSave} {isSaving} {lastSaved} />
		{:else}
			<div class="flex flex-col items-center justify-center py-12">
				<p class="text-destructive">Watchlist not found</p>
			</div>
		{/if}
	</div>
</main>
