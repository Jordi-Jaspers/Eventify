<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Edit, Pause, Play, Trash2, Terminal, MoreVertical } from '@lucide/svelte';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import { copyCurlToClipboard } from '$lib/utils/channel';

	interface Props {
		channel: ChannelDetailsResponse;
		onEdit: (channel: ChannelDetailsResponse) => void;
		onPause: (channel: ChannelDetailsResponse) => void;
		onResume: (channel: ChannelDetailsResponse) => void;
		onDelete: (channel: ChannelDetailsResponse) => void;
	}

	let { channel, onEdit, onPause, onResume, onDelete }: Props = $props();
</script>

<div class="flex items-center justify-end">
	<DropdownMenu.Root>
		<DropdownMenu.Trigger>
			<Button
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-muted-foreground hover:text-primary"
				aria-label="Channel actions"
			>
				<MoreVertical class="h-4 w-4" />
			</Button>
		</DropdownMenu.Trigger>
		<DropdownMenu.Content align="end" class="w-48 bg-card/95 backdrop-blur-xl border-border/50">
			<DropdownMenu.Item onclick={() => copyCurlToClipboard(channel.slug)} class="cursor-pointer">
				<Terminal class="mr-2 h-4 w-4" />
				Copy Curl
			</DropdownMenu.Item>
			<DropdownMenu.Separator />
			<DropdownMenu.Item onclick={() => onEdit(channel)} class="cursor-pointer">
				<Edit class="mr-2 h-4 w-4" />
				Edit
			</DropdownMenu.Item>
			{#if channel.status === 'ACTIVE'}
				<DropdownMenu.Item onclick={() => onPause(channel)} class="cursor-pointer">
					<Pause class="mr-2 h-4 w-4" />
					Pause
				</DropdownMenu.Item>
			{:else}
				<DropdownMenu.Item onclick={() => onResume(channel)} class="cursor-pointer">
					<Play class="mr-2 h-4 w-4" />
					Resume
				</DropdownMenu.Item>
			{/if}
			<DropdownMenu.Separator />
			<DropdownMenu.Item onclick={() => onDelete(channel)} class="cursor-pointer text-destructive">
				<Trash2 class="mr-2 h-4 w-4" />
				Delete
			</DropdownMenu.Item>
		</DropdownMenu.Content>
	</DropdownMenu.Root>
</div>
