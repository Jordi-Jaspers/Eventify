<!-- Re-exports WatchlistSelector with orgId for backwards compatibility -->
<script lang="ts">
	import WatchlistSelector from './WatchlistSelector.svelte';

	interface Props {
		orgId: number;
		currentWatchlistId: number;
		currentWatchlistName: string;
		onSelect?: (watchlistId: number) => void;
	}

	let { orgId, currentWatchlistId, currentWatchlistName, onSelect }: Props = $props();
</script>

<WatchlistSelector {orgId} {currentWatchlistId} {currentWatchlistName} {onSelect} />
