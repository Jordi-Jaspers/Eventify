<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { Pause, Play, Trash2, X, LoaderCircle } from '@lucide/svelte';
	import type { ChannelDetailsResponse } from '$lib/api/models';

	interface Props {
		selectedChannels: ChannelDetailsResponse[];
		onPause: (ids: number[]) => Promise<void>;
		onResume: (ids: number[]) => Promise<void>;
		onDelete: (ids: number[]) => Promise<void>;
		onClearSelection: () => void;
	}

	let { selectedChannels, onPause, onResume, onDelete, onClearSelection }: Props = $props();

	let processing: boolean = $state(false);
	let showDeleteConfirm: boolean = $state(false);

	const selectedIds: number[] = $derived(
		selectedChannels.map((c: ChannelDetailsResponse) => c.id ?? 0).filter((id: number) => id > 0)
	);

	const hasActive: boolean = $derived(
		selectedChannels.some((c: ChannelDetailsResponse) => c.status === 'ACTIVE')
	);

	const hasPaused: boolean = $derived(
		selectedChannels.some((c: ChannelDetailsResponse) => c.status === 'PAUSED')
	);

	const count: number = $derived(selectedChannels.length);

	async function handlePause(): Promise<void> {
		processing = true;
		try {
			const activeIds: number[] = selectedChannels
				.filter((c: ChannelDetailsResponse) => c.status === 'ACTIVE')
				.map((c: ChannelDetailsResponse) => c.id ?? 0)
				.filter((id: number) => id > 0);
			await onPause(activeIds);
			onClearSelection();
		} finally {
			processing = false;
		}
	}

	async function handleResume(): Promise<void> {
		processing = true;
		try {
			const pausedIds: number[] = selectedChannels
				.filter((c: ChannelDetailsResponse) => c.status === 'PAUSED')
				.map((c: ChannelDetailsResponse) => c.id ?? 0)
				.filter((id: number) => id > 0);
			await onResume(pausedIds);
			onClearSelection();
		} finally {
			processing = false;
		}
	}

	async function handleDelete(): Promise<void> {
		processing = true;
		showDeleteConfirm = false;
		try {
			await onDelete(selectedIds);
			onClearSelection();
		} finally {
			processing = false;
		}
	}
</script>

{#if count > 0}
	<div
		class="flex items-center gap-3 px-4 py-2.5 rounded-xl border border-primary/30 bg-primary/5 backdrop-blur-xl shadow-sm animate-fade-in"
	>
		<!-- Selection count -->
		<span class="text-sm font-medium text-primary shrink-0">
			{count} selected
		</span>

		<div class="h-4 w-px bg-border/50"></div>

		<!-- Actions -->
		<div class="flex items-center gap-2 flex-1">
			{#if hasActive}
				<Button
					variant="outline"
					size="sm"
					disabled={processing}
					onclick={handlePause}
					class="h-8 border-border/50 bg-background/50"
				>
					{#if processing}
						<LoaderCircle class="mr-1.5 h-3.5 w-3.5 animate-spin" />
					{:else}
						<Pause class="mr-1.5 h-3.5 w-3.5" />
					{/if}
					Pause
				</Button>
			{/if}

			{#if hasPaused}
				<Button
					variant="outline"
					size="sm"
					disabled={processing}
					onclick={handleResume}
					class="h-8 border-border/50 bg-background/50"
				>
					{#if processing}
						<LoaderCircle class="mr-1.5 h-3.5 w-3.5 animate-spin" />
					{:else}
						<Play class="mr-1.5 h-3.5 w-3.5" />
					{/if}
					Resume
				</Button>
			{/if}

			<Button
				variant="outline"
				size="sm"
				disabled={processing}
				onclick={() => (showDeleteConfirm = true)}
				class="h-8 border-destructive/50 bg-destructive/5 text-destructive hover:bg-destructive/10 hover:text-destructive"
			>
				<Trash2 class="mr-1.5 h-3.5 w-3.5" />
				Delete
			</Button>
		</div>

		<!-- Clear selection -->
		<Button
			variant="ghost"
			size="icon"
			class="h-7 w-7 text-muted-foreground hover:text-foreground shrink-0"
			onclick={onClearSelection}
			aria-label="Clear selection"
		>
			<X class="h-3.5 w-3.5" />
		</Button>
	</div>
{/if}

<!-- Delete confirmation dialog -->
<AlertDialog.Root bind:open={showDeleteConfirm}>
	<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title>Delete {count} channel{count > 1 ? 's' : ''}?</AlertDialog.Title>
			<AlertDialog.Description>
				This will permanently delete {count} selected channel{count > 1 ? 's' : ''}. This action
				cannot be undone.
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={handleDelete}
				class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
			>
				Delete {count} channel{count > 1 ? 's' : ''}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
