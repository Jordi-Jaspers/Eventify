<script lang="ts">
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import { ShieldAlert, AlertTriangle } from '@lucide/svelte';
	import type { ApiKeyResponse } from '$lib/api/models';
	import { formatNumber } from './utils';

	interface Props {
		open: boolean;
		apiKey: ApiKeyResponse | null;
		revoking: boolean;
		onClose: () => void;
		onRevoke: () => Promise<void>;
	}

	const { open, apiKey, revoking, onClose, onRevoke }: Props = $props();
</script>

<AlertDialog.Root {open} onOpenChange={(isOpen) => !isOpen && onClose()}>
	<AlertDialog.Content class="bg-background border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title class="flex items-center gap-2">
				<ShieldAlert class="h-5 w-5 text-destructive" />
				Revoke API Key
			</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to revoke this API key? This action cannot be undone.
			</AlertDialog.Description>
		</AlertDialog.Header>

		{#if apiKey}
			<div class="my-4 space-y-3 rounded-lg border border-border/50 bg-card/50 p-4">
				<div>
					<span class="text-sm text-muted-foreground">Key:</span>
					<div class="font-mono text-sm mt-1">
						{apiKey.maskedKey}
					</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Name:</span>
					<div class="font-medium mt-1">{apiKey.name}</div>
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
			<AlertDialog.Cancel disabled={revoking}>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={onRevoke}
				disabled={revoking}
				class="bg-destructive hover:bg-destructive/90"
			>
				{#if revoking}
					Revoking...
				{:else}
					Revoke Key
				{/if}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
