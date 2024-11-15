<script>
import { Button } from '$lib/components/ui/button';
import { KeyRound } from 'lucide-svelte';

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
		<KeyRound />
	{/if}
	One-Time Passcode
</Button>
