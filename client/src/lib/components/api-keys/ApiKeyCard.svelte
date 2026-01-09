<script lang="ts">
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { Key, Trash2 } from '@lucide/svelte';
	import type { ApiKeyResponse } from '$lib/api/models';
	import { formatDate } from '$lib/utils/date';

	interface Props {
		apiKey: ApiKeyResponse;
		onRevoke: (keyId: number) => void;
		revoking: boolean;
	}

	let { apiKey, onRevoke, revoking }: Props = $props();

	let showRevokeDialog: boolean = $state(false);

	function formatLastUsed(lastUsedAt: string | undefined): string {
		if (!lastUsedAt) return 'Never';
		return formatDate(lastUsedAt);
	}

	function formatExpiration(expiresAt: string | undefined): string {
		if (!expiresAt) return 'Never';
		return formatDate(expiresAt);
	}

	function handleRevoke(): void {
		onRevoke(apiKey.id || 0);
		showRevokeDialog = false;
	}
</script>

<div
	class="group flex items-center gap-4 p-3 rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl hover:border-primary/30 transition-all"
>
	<!-- Key Icon -->
	<div class="flex-shrink-0">
		<div class="p-2 rounded-md bg-primary/10">
			<Key class="w-4 h-4 text-primary" />
		</div>
	</div>

	<!-- Name & Masked Key -->
	<div class="flex-1 min-w-0">
		<p class="font-medium text-sm truncate">{apiKey.name}</p>
		<code class="text-xs font-mono text-muted-foreground">{apiKey.maskedKey}</code>
	</div>

	<!-- Metadata -->
	<div class="hidden sm:flex items-center gap-6 text-xs text-muted-foreground">
		<div class="text-right">
			<p class="text-muted-foreground/70">Created</p>
			<p class="font-medium text-foreground">{formatDate(apiKey.createdAt || '')}</p>
		</div>
		<div class="text-right">
			<p class="text-muted-foreground/70">Expires</p>
			<p class="font-medium text-foreground">{formatExpiration(apiKey.expiresAt)}</p>
		</div>
		<div class="text-right">
			<p class="text-muted-foreground/70">Last used</p>
			<p class="font-medium text-foreground">{formatLastUsed(apiKey.lastUsedAt)}</p>
		</div>
		<div class="text-right">
			<p class="text-muted-foreground/70">Requests</p>
			<p class="font-medium text-foreground">{apiKey.totalRequests?.toLocaleString() || 0}</p>
		</div>
	</div>

	<!-- Revoke Button -->
	<AlertDialog.Root bind:open={showRevokeDialog}>
		<AlertDialog.Trigger
			class="flex-shrink-0 p-2 opacity-0 group-hover:opacity-100 hover:bg-destructive/10 hover:text-destructive transition-all rounded-md disabled:opacity-50"
			disabled={revoking}
		>
			<Trash2 class="w-4 h-4" />
		</AlertDialog.Trigger>
		<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
			<AlertDialog.Header>
				<AlertDialog.Title>Revoke API Key</AlertDialog.Title>
				<AlertDialog.Description>
					Are you sure you want to revoke <strong>{apiKey.name}</strong>? This action cannot be
					undone and any applications using this key will stop working immediately.
				</AlertDialog.Description>
			</AlertDialog.Header>
			<AlertDialog.Footer>
				<AlertDialog.Cancel class="bg-background/50 border-border/50">Cancel</AlertDialog.Cancel>
				<AlertDialog.Action
					onclick={handleRevoke}
					class="bg-destructive hover:bg-destructive/90 text-destructive-foreground"
				>
					Revoke
				</AlertDialog.Action>
			</AlertDialog.Footer>
		</AlertDialog.Content>
	</AlertDialog.Root>
</div>
