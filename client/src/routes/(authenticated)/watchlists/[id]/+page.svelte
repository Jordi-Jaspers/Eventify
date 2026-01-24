<script lang="ts">
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { WatchlistBuilder } from '$lib/components/watchlist';
	import { searchChannels } from '$lib/api/channel/UserChannelController';
	import {
		getWatchlist,
		updateWatchlist
	} from '$lib/api/watchlist/UserWatchlistController';
	import type { ChannelDetailsResponse, WatchlistDetailsResponse } from '$lib/api/models';
	import type { components } from '$lib/types/api';
	import { Button } from '$lib/components/ui/button';
	import { ArrowLeft, Eye } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	type UpdateWatchlistRequest = components['schemas']['UpdateWatchlistRequest'];

	const watchlistId: number = $derived(parseInt(page.params.id ?? '0', 10));
	let watchlist: WatchlistDetailsResponse | null = $state(null);
	let allChannels: ChannelDetailsResponse[] = $state([]);
	let loading: boolean = $state(true);
	let isSaving: boolean = $state(false);
	let lastSaved: Date | null = $state(null);

	async function loadData(): Promise<void> {
		loading = true;
		try {
			const [watchlistData, channelsResponse] = await Promise.all([
				getWatchlist(watchlistId),
				searchChannels({ pageNumber: 0, pageSize: 1000 })
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
		try {
			const updated: WatchlistDetailsResponse = await updateWatchlist(watchlistId, request);
			watchlist = updated;
			lastSaved = new Date();
			toast.success('Watchlist saved');
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to save watchlist');
			toast.error(message);
		} finally {
			isSaving = false;
		}
	}

	function handleMonitor(): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}/monitor`);
	}

	onMount(() => loadData());
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
					onclick={() => window.history.back()}
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
						Make changes to your watchlist - they'll be saved automatically
					</p>
				</div>
			</div>
			{#if watchlist}
				<Button
					onclick={handleMonitor}
					class="bg-gradient-to-r from-primary to-primary/80 hover:opacity-90 transition-all shadow-lg text-primary-foreground gap-2"
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
