<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import { WatchlistBuilder } from '$lib/components/watchlist';
	import { searchOrganizationChannels } from '$lib/api/organization/OrganizationChannelController';
	import { createWatchlist } from '$lib/api/watchlist/OrganizationWatchlistController';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import type { components } from '$lib/types/api';
	import { Button } from '$lib/components/ui/button';
	import { ArrowLeft } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { handleError } from '$lib/utils/error-handler';
	import { organizationStore } from '$lib/stores/organization.svelte';

	type CreateWatchlistRequest = components['schemas']['CreateWatchlistRequest'];

	const orgId: number = $derived(Number($page.params.orgId));
	
	// Role-based access check
	const canManage: boolean = $derived(
		organizationStore.currentRole === 'OWNER' || organizationStore.currentRole === 'ADMIN'
	);

	let allChannels: ChannelDetailsResponse[] = $state([]);
	let loading: boolean = $state(true);
	let isSaving: boolean = $state(false);

	async function loadChannels(): Promise<void> {
		loading = true;
		try {
			const response = await searchOrganizationChannels(orgId, {
				pageNumber: 0,
				pageSize: 1000
			});
			allChannels = response.content ?? [];
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load channels');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function handleSave(request: CreateWatchlistRequest): Promise<void> {
		isSaving = true;
		try {
			const watchlist: components['schemas']['WatchlistDetailsResponse'] = await createWatchlist(
				orgId,
				request
			);
			toast.success('Watchlist created');
			goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/${watchlist.id}`);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to create watchlist');
			toast.error(message);
		} finally {
			isSaving = false;
		}
	}

	onMount(() => {
		if (!canManage) {
			toast.error("You don't have permission to create watchlists");
			goto(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path);
			return;
		}
		loadChannels();
	});
</script>

<svelte:head>
	<title>Create Organization Watchlist - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center gap-4 mb-8">
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
					Create Organization Watchlist
				</h1>
				<p class="text-muted-foreground mt-2">
					Build a custom watchlist to monitor your organization's channels
				</p>
			</div>
		</div>

		<!-- Builder -->
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="text-muted-foreground">Loading channels...</div>
			</div>
		{:else}
			<WatchlistBuilder {allChannels} onSave={handleSave} {isSaving} />
		{/if}
	</div>
</main>
