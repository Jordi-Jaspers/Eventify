<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { LoaderCircle, Terminal } from '@lucide/svelte';
	import type { DevCredentialsResponse } from '$lib/api/models';

	interface Props {
		devCredentials: DevCredentialsResponse | null;
		devCredentialsLoading: boolean;
		onFill: () => void;
	}

	let { devCredentials, devCredentialsLoading, onFill }: Props = $props();
</script>

<div class="mt-4 p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 backdrop-blur-sm">
	<div class="flex items-center justify-between mb-2">
		<div class="flex items-center gap-2 text-amber-500 text-sm font-medium">
			<Terminal class="w-4 h-4" />
			Dev Credentials
		</div>
		<a href="/dev-playbook" class="text-xs text-primary hover:underline">
			Component Playbook →
		</a>
	</div>
	{#if devCredentialsLoading}
		<div class="text-xs text-muted-foreground flex items-center gap-2">
			<LoaderCircle class="w-3 h-3 animate-spin" />
			Loading credentials...
		</div>
	{:else if devCredentials}
		<div class="text-xs text-muted-foreground space-y-1">
			<p><span class="font-medium">Email:</span> {devCredentials.email}</p>
			<p><span class="font-medium">Password:</span> {devCredentials.password}</p>
		</div>
		<Button variant="outline" size="sm" class="mt-2 w-full text-xs" onclick={onFill}>
			Fill Credentials
		</Button>
	{:else}
		<div class="text-xs text-muted-foreground">Failed to load dev credentials</div>
	{/if}
</div>
