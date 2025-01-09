<script lang="ts">
	import { LoadingScreen } from '$lib/components/general';

	let { data } = $props();
</script>

{#if !data}
	<LoadingScreen />
{/if}
