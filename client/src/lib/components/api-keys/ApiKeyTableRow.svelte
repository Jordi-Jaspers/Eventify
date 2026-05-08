<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Trash2, Clock, Activity } from '@lucide/svelte';
	import type { ApiKeyResponse } from '$lib/api/models';
	import { formatRelativeDate } from '$lib/utils/date';

	interface Props {
		apiKey: ApiKeyResponse;
		canManage: boolean;
		onRevoke: (key: ApiKeyResponse) => void;
	}

	const { apiKey, canManage, onRevoke }: Props = $props();
</script>

<div
	class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 hover:bg-muted/30 transition-all"
>
	<!-- Name & Masked Key -->
	<div class="col-span-1 md:col-span-4 flex flex-col gap-1">
		<p class="font-medium">{apiKey.name ?? 'Unnamed'}</p>
		<code class="text-xs text-muted-foreground font-mono">{apiKey.maskedKey ?? ''}</code>
	</div>

	<!-- Created By -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<p class="text-sm text-muted-foreground">
			{#if apiKey.createdBy}
				{apiKey.createdBy.firstName ?? ''} {apiKey.createdBy.lastName ?? ''}
			{:else}
				N/A
			{/if}
		</p>
	</div>

	<!-- Created At -->
	<div class="col-span-1 md:col-span-2 flex items-center gap-1">
		<Clock class="w-3 h-3 text-muted-foreground" />
		<p class="text-sm text-muted-foreground">
			{formatRelativeDate(apiKey.createdAt ?? '')}
		</p>
	</div>

	<!-- Expires At -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<p class="text-sm text-muted-foreground">
			{#if apiKey.expiresAt}
				{formatRelativeDate(apiKey.expiresAt)}
			{:else}
				<span class="text-green-500">Never</span>
			{/if}
		</p>
	</div>

	<!-- Last Used -->
	<div class="hidden md:flex md:col-span-1 items-center">
		<div class="flex flex-col gap-0.5">
			<div class="flex items-center gap-1">
				<Activity class="w-3 h-3 text-muted-foreground" />
				<p class="text-xs text-muted-foreground">
					{#if apiKey.lastUsedAt}
						{formatRelativeDate(apiKey.lastUsedAt)}
					{:else}
						Never
					{/if}
				</p>
			</div>
			<p class="text-xs text-muted-foreground">
				{apiKey.totalRequests ?? 0} req
			</p>
		</div>
	</div>

	<!-- Actions -->
	<div class="col-span-1 md:col-span-1 flex items-center justify-center">
		{#if canManage}
			<Button
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-muted-foreground hover:text-destructive"
				onclick={() => onRevoke(apiKey)}
				aria-label="Revoke API key"
			>
				<Trash2 class="h-4 w-4" />
			</Button>
		{/if}
	</div>
</div>
