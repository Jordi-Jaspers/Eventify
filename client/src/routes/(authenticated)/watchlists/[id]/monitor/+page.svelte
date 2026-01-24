<script lang="ts">
	import { page } from '$app/state';
	import { goto, replaceState } from '$app/navigation';
	import { onMount } from 'svelte';
	import { getWatchlist } from '$lib/api/watchlist/UserWatchlistController';
	import type { WatchlistDetailsResponse } from '$lib/api/models';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { ArrowLeft, Edit, Eye, Construction } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	const watchlistId: number = $derived(parseInt(page.params.id ?? '0', 10));
	let watchlist: WatchlistDetailsResponse | null = $state(null);
	let loading: boolean = $state(true);

	async function loadWatchlist(): Promise<void> {
		loading = true;
		try {
			watchlist = await getWatchlist(watchlistId);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load watchlist');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function handleEdit(): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}`);
	}

	function handleBack(): void {
		goto(CLIENT_ROUTES.WATCHLISTS_PAGE.path);
	}

	onMount(() => {
		// Clean the URL by replacing the current state (removes any query params)
		replaceState(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}/monitor`, {});
		loadWatchlist();
	});
</script>

<svelte:head>
	<title>{watchlist?.name ?? 'Monitor Watchlist'} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between mb-8">
			<div class="flex items-center gap-4">
				<Button
					variant="ghost"
					size="icon"
					onclick={handleBack}
					aria-label="Go back to watchlists"
				>
					<ArrowLeft class="h-5 w-5" />
				</Button>
				<div>
					<h1 class="text-3xl font-bold text-foreground">
						{#if watchlist}
							{watchlist.name}
						{:else}
							Monitor Watchlist
						{/if}
					</h1>
					<p class="text-muted-foreground mt-2">
						Real-time monitoring of your configured channels
					</p>
				</div>
			</div>
			{#if watchlist}
				<Button
					variant="outline"
					onclick={handleEdit}
					class="gap-2"
				>
					<Edit class="h-4 w-4" />
					Edit Watchlist
				</Button>
			{/if}
		</div>

		<!-- Content -->
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="text-muted-foreground">Loading watchlist...</div>
			</div>
		{:else if watchlist}
			<!-- Placeholder for monitoring view -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
				<CardHeader>
					<CardTitle class="flex items-center gap-3">
						<Eye class="h-5 w-5 text-primary" />
						Monitoring View
					</CardTitle>
				</CardHeader>
				<CardContent>
					<div class="flex flex-col items-center justify-center py-16 text-center">
						<div class="h-20 w-20 rounded-full bg-primary/10 flex items-center justify-center mb-6">
							<Construction class="h-10 w-10 text-primary" />
						</div>
						<h3 class="text-xl font-semibold mb-2">Coming Soon</h3>
						<p class="text-muted-foreground max-w-md mb-6">
							The real-time monitoring dashboard for "{watchlist.name}" is currently under development.
							You'll soon be able to view live events from your {watchlist.configuration?.channelIds?.length ?? 0} configured channel{(watchlist.configuration?.channelIds?.length ?? 0) !== 1 ? 's' : ''}.
						</p>
						<div class="flex gap-3">
							<Button variant="outline" onclick={handleEdit} class="gap-2">
								<Edit class="h-4 w-4" />
								Edit Configuration
							</Button>
							<Button variant="outline" onclick={handleBack} class="gap-2">
								<ArrowLeft class="h-4 w-4" />
								Back to Watchlists
							</Button>
						</div>
					</div>
				</CardContent>
			</Card>

			<!-- Watchlist Info Summary -->
			<div class="grid grid-cols-1 md:grid-cols-3 gap-4">
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
					<CardContent class="pt-6">
						<div class="text-center">
							<div class="text-3xl font-bold text-primary">
								{watchlist.configuration?.channelIds?.length ?? 0}
							</div>
							<div class="text-sm text-muted-foreground mt-1">Channels</div>
						</div>
					</CardContent>
				</Card>
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
					<CardContent class="pt-6">
						<div class="text-center">
							<div class="text-3xl font-bold text-primary">
								{watchlist.filters?.timeRange ?? '24h'}
							</div>
							<div class="text-sm text-muted-foreground mt-1">Time Range</div>
						</div>
					</CardContent>
				</Card>
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
					<CardContent class="pt-6">
						<div class="text-center">
							<div class="text-3xl font-bold text-primary">
								{watchlist.filters?.onlyCritical ? 'Yes' : 'No'}
							</div>
							<div class="text-sm text-muted-foreground mt-1">Critical Only</div>
						</div>
					</CardContent>
				</Card>
			</div>
		{:else}
			<div class="flex flex-col items-center justify-center py-12">
				<p class="text-destructive">Watchlist not found</p>
			</div>
		{/if}
	</div>
</main>
