<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { Loader2 } from '@lucide/svelte';

	interface Props {
		adapterType: string;
		webhookUrl: string;
		onTest: (adapterType: string, webhookUrl: string) => Promise<void>;
	}

	let { adapterType, webhookUrl, onTest }: Props = $props();

	let testing = $state(false);

	async function handleTest(): Promise<void> {
		testing = true;
		try {
			await onTest(adapterType, webhookUrl);
		} catch {
			// toast handled in service
		} finally {
			testing = false;
		}
	}
</script>

<Button
	variant="outline"
	size="sm"
	onclick={handleTest}
	disabled={testing || !webhookUrl}
	aria-label="Send test notification"
>
	{#if testing}
		<Loader2 class="w-3 h-3 animate-spin mr-1" />
		Testing…
	{:else}
		Test
	{/if}
</Button>
