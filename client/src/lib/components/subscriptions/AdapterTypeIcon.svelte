<script lang="ts">
	import { Bell, Mail, MessageSquare, Hash } from '@lucide/svelte';

	interface Props {
		type: string;
	}

	const { type }: Props = $props();
</script>

{#if type === 'IN_APP'}
	<Bell class="h-3.5 w-3.5 shrink-0" />
{:else if type === 'EMAIL'}
	<Mail class="h-3.5 w-3.5 shrink-0" />
{:else if type === 'MATTERMOST'}
	<MessageSquare class="h-3.5 w-3.5 shrink-0" />
{:else if type === 'SLACK'}
	<Hash class="h-3.5 w-3.5 shrink-0" />
{/if}
