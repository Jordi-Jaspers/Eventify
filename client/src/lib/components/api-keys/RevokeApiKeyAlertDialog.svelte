<script lang="ts">
	import * as AlertDialog from '$lib/components/ui/alert-dialog';

	let {
		open,
		onOpenChange,
		keyName,
		isRevoking,
		onConfirm
	}: {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		keyName: string | undefined;
		isRevoking: boolean;
		onConfirm: () => void;
	} = $props();
</script>

<AlertDialog.Root {open} {onOpenChange}>
	<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title>Revoke API Key</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to revoke <strong>{keyName ?? 'Unnamed'}</strong>? This action cannot be
				undone and any applications using this key will stop working immediately.
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel class="bg-background/50 border-border/50">Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={onConfirm}
				disabled={isRevoking}
				class="bg-destructive hover:bg-destructive/90 text-destructive-foreground"
			>
				{isRevoking ? 'Revoking...' : 'Revoke'}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
