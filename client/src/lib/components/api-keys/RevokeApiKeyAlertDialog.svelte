<script lang="ts">
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import { ShieldAlert, AlertTriangle } from '@lucide/svelte';
	import type { ApiKeyResponse } from '$lib/api/models';
	import { formatNumber } from '$lib/components/admin/utils';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		keyName: string | undefined;
		isRevoking: boolean;
		onConfirm: () => void;
		/** Optional: when provided, shows detailed key info (admin view) */
		apiKey?: ApiKeyResponse | null;
	}

	let { open, onOpenChange, keyName, isRevoking, onConfirm, apiKey = null }: Props = $props();
</script>

<AlertDialog.Root {open} onOpenChange={(isOpen) => !isOpen && onOpenChange(false)}>
	<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title class="flex items-center gap-2">
				{#if apiKey}
					<ShieldAlert class="h-5 w-5 text-destructive" />
				{/if}
				Revoke API Key
			</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to revoke <strong>{keyName ?? 'Unnamed'}</strong>? This action cannot be
				undone and any applications using this key will stop working immediately.
			</AlertDialog.Description>
		</AlertDialog.Header>

		{#if apiKey}
			<div class="my-4 space-y-3 rounded-lg border border-border/50 bg-card/50 p-4">
				<div>
					<span class="text-sm text-muted-foreground">Key:</span>
					<div class="font-mono text-sm mt-1">{apiKey.maskedKey}</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Owner:</span>
					<div class="mt-1">{apiKey.owner?.name}</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Total Requests:</span>
					<div class="font-medium mt-1">{formatNumber(apiKey.totalRequests)}</div>
				</div>
			</div>

			<Alert variant="destructive" class="bg-destructive/10 border-destructive/50">
				<AlertTriangle class="h-4 w-4" />
				<AlertDescription>
					Any systems using this key will immediately lose access.
				</AlertDescription>
			</Alert>
		{/if}

		<AlertDialog.Footer>
			<AlertDialog.Cancel
				disabled={isRevoking}
				class="bg-background/50 border-border/50"
			>Cancel</AlertDialog.Cancel>
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
