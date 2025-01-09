<script>
	import { GitHub } from '$lib/icons';
	import { Button } from '$lib/components/ui/button';
	import { LoaderCircle } from 'lucide-svelte';

	let { isLoading } = $props();
</script>

<Button class="w-full" variant="outline" type="button" disabled={isLoading}>
	{#if isLoading}
		<LoaderCircle class="h-4 w-4 animate-spin" />
	{/if}
	<GitHub />
	GitHub
</Button>
