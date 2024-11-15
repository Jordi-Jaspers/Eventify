<script>
	import { GitHub } from '$lib/icons';
	import { Button } from '$lib/components/ui/button';

	let { isLoading } = $props();
</script>

<Button class="w-full" variant="outline" type="button" disabled={isLoading}>
	{#if isLoading}
		<div
			class="inline-block h-8 w-8 loading"
			role="status"
		>
		</div>
	{:else}
		<GitHub />
	{/if}
	GitHub
</Button>
