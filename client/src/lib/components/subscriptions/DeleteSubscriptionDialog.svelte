<script lang="ts">
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { LoaderCircle } from '@lucide/svelte';

	interface Props {
		open: boolean;
		deleting: boolean;
		watchlistId?: number;
		onOpenChange: (open: boolean) => void;
		onConfirm: () => void;
	}

	const { open, deleting, watchlistId, onOpenChange, onConfirm }: Props = $props();
</script>

<AlertDialog.Root {open} {onOpenChange}>
	<AlertDialog.Content class="border-border/50 bg-card/95 backdrop-blur-xl">
		<AlertDialog.Header>
			<AlertDialog.Title>Delete Subscription</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to delete your subscription to Watchlist #{watchlistId}? You will no
				longer receive notifications for this watchlist.
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel onclick={() => onOpenChange(false)}>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={onConfirm}
				disabled={deleting}
				class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
			>
				{#if deleting}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
				{/if}
				Delete
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
