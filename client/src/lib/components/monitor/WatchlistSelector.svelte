<script lang="ts">
	import { onMount } from 'svelte';
	import { ChevronDown, Search } from '@lucide/svelte';
	import {
		DropdownMenu,
		DropdownMenuContent,
		DropdownMenuGroup,
		DropdownMenuItem,
		DropdownMenuTrigger
	} from '$lib/components/ui/dropdown-menu';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { searchWatchlists as searchUserWatchlists } from '$lib/api/watchlist/UserWatchlistController';
	import { searchWatchlists as searchOrgWatchlists } from '$lib/api/watchlist/OrganizationWatchlistController';
	import type { WatchlistDetailsResponse, SearchInput } from '$lib/api/models';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';

	interface Props {
		currentWatchlistId: number;
		currentWatchlistName: string;
		orgId?: number;
		onSelect?: (watchlistId: number) => void;
	}

	let { currentWatchlistId, currentWatchlistName, orgId, onSelect }: Props = $props();

	let watchlists: WatchlistDetailsResponse[] = $state([]);
	let loading: boolean = $state(true);
	let searchQuery: string = $state('');
	let open: boolean = $state(false);

	const filteredWatchlists: WatchlistDetailsResponse[] = $derived(
		searchQuery.trim() === ''
			? watchlists
			: watchlists.filter((w: WatchlistDetailsResponse) =>
					(w.name ?? '').toLowerCase().includes(searchQuery.toLowerCase())
				)
	);

	async function loadWatchlists(): Promise<void> {
		loading = true;
		try {
			const params = {
				pageNumber: 0,
				pageSize: 100,
				sortOrder: [{ name: 'name', direction: 'ASC' }],
				searchInputs: [] as SearchInput[]
			};
			const result = orgId
				? await searchOrgWatchlists(orgId, params)
				: await searchUserWatchlists(params);
			watchlists = result.content ?? [];
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load watchlists');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function handleWatchlistSelect(watchlistId: number): void {
		if (watchlistId !== currentWatchlistId) {
			onSelect?.(watchlistId);
		}
		open = false;
	}

	onMount(() => {
		loadWatchlists();
	});
</script>

<DropdownMenu bind:open>
	<DropdownMenuTrigger>
		{#snippet child({ props })}
			<Button
				{...props}
				variant="outline"
				class="min-w-[300px] justify-between bg-background/50 border-border"
				disabled={loading}
			>
				<span class="truncate">{currentWatchlistName}</span>
				<ChevronDown class="h-4 w-4 opacity-50" />
			</Button>
		{/snippet}
	</DropdownMenuTrigger>
	<DropdownMenuContent class="w-[300px] max-h-[400px] overflow-y-auto" align="start">
		<div class="px-2 py-1.5 sticky top-0 bg-background z-10">
			<div class="relative">
				<Search class="absolute left-2 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
				<Input
					bind:value={searchQuery}
					placeholder="Search watchlists..."
					class="pl-8 h-8 bg-background/50"
					onclick={(e: MouseEvent) => e.stopPropagation()}
				/>
			</div>
		</div>
		<DropdownMenuGroup>
			{#if loading}
				<div class="py-6 text-center text-sm text-muted-foreground">Loading...</div>
			{:else if filteredWatchlists.length === 0}
				<div class="py-6 text-center text-sm text-muted-foreground">
					{searchQuery ? 'No watchlists found' : 'No watchlists available'}
				</div>
			{:else}
				{#each filteredWatchlists as watchlist (watchlist.id)}
					{@const isSelected = watchlist.id === currentWatchlistId}
					<DropdownMenuItem
						onclick={() => handleWatchlistSelect(watchlist.id ?? 0)}
						class={isSelected ? 'bg-primary/10' : ''}
					>
						<span class="truncate">{watchlist.name}</span>
					</DropdownMenuItem>
				{/each}
			{/if}
		</DropdownMenuGroup>
	</DropdownMenuContent>
</DropdownMenu>
