<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Edit, Pause, Play, Trash2, Terminal } from '@lucide/svelte';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import { toast } from 'svelte-sonner';

	interface Props {
		channel: ChannelDetailsResponse;
		onEdit: (channel: ChannelDetailsResponse) => void;
		onPause: (channel: ChannelDetailsResponse) => void;
		onResume: (channel: ChannelDetailsResponse) => void;
		onDelete: (channel: ChannelDetailsResponse) => void;
	}

	let { channel, onEdit, onPause, onResume, onDelete }: Props = $props();

	function copyCurlCommand(): void {
		const curlCommand: string = `curl -X POST https://api.eventify.dev/v1/events \\
  -H "Authorization: Bearer <YOUR_API_KEY>" \\
  -H "Content-Type: application/json" \\
  -d '{
    "channelSlug": "${channel.slug}",
    "severity": "INFO",
    "title": "Event Title",
    "message": "Event message here"
  }'`;

		navigator.clipboard
			.writeText(curlCommand)
			.then(() => {
				toast.success('Curl command copied to clipboard');
			})
			.catch(() => {
				toast.error('Failed to copy to clipboard');
			});
	}
</script>

<div class="flex items-center justify-end gap-1">
	<Button
		variant="ghost"
		size="icon"
		class="h-8 w-8 text-muted-foreground hover:text-primary"
		onclick={copyCurlCommand}
		aria-label="Copy curl command"
	>
		<Terminal class="h-4 w-4" />
	</Button>
	<Button
		variant="ghost"
		size="icon"
		class="h-8 w-8 text-muted-foreground hover:text-primary"
		onclick={() => onEdit(channel)}
		aria-label="Edit channel"
	>
		<Edit class="h-4 w-4" />
	</Button>
	{#if channel.status === 'ACTIVE'}
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-yellow-500"
			onclick={() => onPause(channel)}
			aria-label="Pause channel"
		>
			<Pause class="h-4 w-4" />
		</Button>
	{:else}
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-green-500"
			onclick={() => onResume(channel)}
			aria-label="Resume channel"
		>
			<Play class="h-4 w-4" />
		</Button>
	{/if}
	<Button
		variant="ghost"
		size="icon"
		class="h-8 w-8 text-muted-foreground hover:text-destructive"
		onclick={() => onDelete(channel)}
		aria-label="Delete channel"
	>
		<Trash2 class="h-4 w-4" />
	</Button>
</div>
