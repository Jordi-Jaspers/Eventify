<script lang="ts">
	import { ExternalLink, Users } from '@lucide/svelte';
	import { formatDateTime } from '$lib/utils/date';
	import type { BroadcastResponse, AudienceType, PageResource, RecipientResponse } from '$lib/api/models';
	import RecipientList from '$lib/components/notification/RecipientList.svelte';

	const AUDIENCE_TYPE_LABELS: Record<AudienceType, string> = {
		ALL_USERS: 'All Users',
		ALL_ORGANIZATION_OWNERS: 'All Organization Owners',
		ORGANIZATION: 'Organization',
		USER: 'Single User',
		GLOBAL_ROLE: 'Global Role'
	};

	interface Props {
		broadcast: BroadcastResponse;
		recipientLoading: boolean;
		recipientSearch: string;
		recipientData: PageResource<RecipientResponse> | null;
		onSearchInput: (value: string) => void;
		onPageChange: (page: number) => void;
	}

	let {
		broadcast,
		recipientLoading,
		recipientSearch,
		recipientData,
		onSearchInput,
		onPageChange
	}: Props = $props();
</script>

<div class="mx-4 mb-4 rounded-lg border border-border/40 bg-muted/5">
	<div class="p-4 space-y-4">
		<div class="text-sm text-foreground whitespace-pre-wrap leading-relaxed">
			{broadcast.message}
		</div>

		<div class="border-t border-border/30 pt-3">
			<div class="grid grid-cols-2 lg:grid-cols-4 gap-y-3 gap-x-6 text-sm">
				<div>
					<span class="text-muted-foreground text-xs uppercase tracking-wide">Audience Type</span>
					<p class="mt-0.5">{AUDIENCE_TYPE_LABELS[broadcast.audienceType] ?? broadcast.audienceType}</p>
				</div>
				<div>
					<span class="text-muted-foreground text-xs uppercase tracking-wide">Recipients</span>
					<p class="mt-0.5 flex items-center gap-1.5">
						<Users class="h-3.5 w-3.5 text-muted-foreground" />
						{broadcast.recipientCount}
					</p>
				</div>
				<div>
					<span class="text-muted-foreground text-xs uppercase tracking-wide">Sent By</span>
					<p class="mt-0.5 truncate">{broadcast.sentByEmail}</p>
				</div>
				<div>
					<span class="text-muted-foreground text-xs uppercase tracking-wide">Sent At</span>
					<p class="mt-0.5 tabular-nums">{formatDateTime(broadcast.createdAt)}</p>
				</div>
			</div>
		</div>

		{#if broadcast.actionUrl}
			<div class="border-t border-border/30 pt-3">
				<a
					href={broadcast.actionUrl}
					target="_blank"
					rel="noopener noreferrer"
					class="inline-flex items-center gap-1.5 text-sm text-primary hover:underline"
				>
					<ExternalLink class="h-3.5 w-3.5" />
					{broadcast.actionLabel ?? broadcast.actionUrl}
				</a>
			</div>
		{/if}

		<RecipientList
			loading={recipientLoading}
			search={recipientSearch}
			data={recipientData}
			onSearchInput={onSearchInput}
			onPageChange={onPageChange}
		/>
	</div>
</div>
