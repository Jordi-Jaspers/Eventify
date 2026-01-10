<script lang="ts">
	import ApiKeyCard from './ApiKeyCard.svelte';
	import type { ApiKeyResponse } from '$lib/api/models';

	interface Props {
		keys: ApiKeyResponse[];
		onRevoke: (keyId: number) => void;
		revoking: boolean;
	}

	let { keys, onRevoke, revoking }: Props = $props();
</script>

<div class="space-y-3">
	{#each keys as apiKey (apiKey.id)}
		<ApiKeyCard {apiKey} {onRevoke} {revoking} />
	{/each}
</div>
