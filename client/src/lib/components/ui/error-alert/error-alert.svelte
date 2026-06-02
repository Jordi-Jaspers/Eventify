<script lang="ts">
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Button } from '$lib/components/ui/button';
	import { AlertCircle } from '@lucide/svelte';

	interface Props {
		message: string;
		onRetry?: () => void;
	}
	let { message, onRetry }: Props = $props();
</script>

<Alert variant="destructive">
	<AlertCircle class="h-4 w-4" />
	<AlertDescription class="flex items-center justify-between">
		<span>{message}</span>
		{#if onRetry}
			<Button variant="outline" size="sm" onclick={onRetry}>Retry</Button>
		{/if}
	</AlertDescription>
</Alert>
