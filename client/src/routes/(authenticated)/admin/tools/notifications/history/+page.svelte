<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { Badge } from '$lib/components/ui/badge';
	import { History, Megaphone, Monitor, TriangleAlert, ChevronDown, Users, ExternalLink, Sparkles } from '@lucide/svelte';
	import { formatDateTime } from '$lib/utils/date';
	import {
		searchBroadcasts,
		searchBroadcastRecipients
	} from '$lib/api/admin/AdminNotificationController';
	import type {
		SortablePageInput,
		PageResource,
		BroadcastResponse,
		BroadcastCategory,
		AudienceType,
		RecipientResponse
	} from '$lib/api/models';
	import { handleError } from '$lib/utils/error-handler';
	import RecipientList from '$lib/components/notification/RecipientList.svelte';

	interface RecipientState {
		loading: boolean;
		page: number;
		search: string;
		data: PageResource<RecipientResponse> | null;
		debounceTimer: ReturnType<typeof setTimeout> | null;
	}

	const AUDIENCE_TYPE_LABELS: Record<AudienceType, string> = {
		ALL_USERS: 'All Users',
		ALL_ORGANIZATION_OWNERS: 'All Organization Owners',
		ORGANIZATION: 'Organization',
		USER: 'Single User',
		GLOBAL_ROLE: 'Global Role'
	};

	const categoryConfig: Record<
		BroadcastCategory,
		{ label: string; variant: 'default' | 'destructive' | 'secondary'; icon: typeof Megaphone }
	> = {
		ANNOUNCEMENT: { label: 'Announce', variant: 'default', icon: Megaphone },
		SYSTEM: { label: 'System', variant: 'secondary', icon: Monitor },
		ALERT: { label: 'Alert', variant: 'destructive', icon: TriangleAlert },
		UPDATE: { label: 'Update', variant: 'default', icon: Sparkles }
	};

	const columns: DataTableColumn<BroadcastResponse>[] = [
		{ key: 'createdAt', label: 'Sent At', sortable: true, filterable: true, filterType: 'DATE', colSpan: 2 },
		{ key: 'title', label: 'Title', sortable: false, filterable: true, filterType: 'FUZZY_TEXT', filterPlaceholder: 'Search title...', colSpan: 3 },
		{ key: 'category', label: 'Category', sortable: true, filterable: true, filterType: 'ENUM', filterOptions: [{ value: 'ANNOUNCEMENT', label: 'Announcement' }, { value: 'SYSTEM', label: 'System' }, { value: 'ALERT', label: 'Alert' }, { value: 'UPDATE', label: 'Update' }], colSpan: 2 },
		{ key: 'audienceType', label: 'Audience', sortable: false, colSpan: 3 },
		{ key: 'sentByEmail', label: 'Sent By', sortable: false, filterable: true, filterType: 'FUZZY_TEXT', filterPlaceholder: 'Search email...', colSpan: 2 }
	];

	const service = createDataTableService<BroadcastResponse>({
		fetchFn: (input: SortablePageInput) => searchBroadcasts(input),
		pageSize: 20,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

	let expandedId: number | null = $state(null);
	let recipientStates: Map<number, RecipientState> = $state(new Map());

	onMount(() => service.load());

	function getRecipientState(id: number): RecipientState {
		if (!recipientStates.has(id)) {
			recipientStates = new Map(recipientStates).set(id, {
				loading: false,
				page: 0,
				search: '',
				data: null,
				debounceTimer: null
			});
		}
		return recipientStates.get(id)!;
	}

	function updateRecipientState(id: number, patch: Partial<RecipientState>): void {
		const current: RecipientState = getRecipientState(id);
		recipientStates = new Map(recipientStates).set(id, { ...current, ...patch });
	}

	async function loadRecipients(id: number, page: number, search: string): Promise<void> {
		updateRecipientState(id, { loading: true });
		try {
			const input: SortablePageInput = {
				pageNumber: page,
				pageSize: 20,
				searchInputs: search.trim() ? [{ fieldName: 'search', textValue: search.trim() }] : []
			};
			const data: PageResource<RecipientResponse> = await searchBroadcastRecipients(id, input);
			updateRecipientState(id, { loading: false, data, page });
		} catch (err: unknown) {
			handleError(err, 'Failed to load recipients');
			updateRecipientState(id, { loading: false });
		}
	}

	function toggleExpand(id: number): void {
		const wasExpanded: boolean = expandedId === id;
		expandedId = wasExpanded ? null : id;
		if (!wasExpanded) {
			const state: RecipientState = getRecipientState(id);
			if (!state.data) loadRecipients(id, 0, '');
		}
	}

	function handleSearchInput(id: number, value: string): void {
		const state: RecipientState = getRecipientState(id);
		if (state.debounceTimer) clearTimeout(state.debounceTimer);
		const timer: ReturnType<typeof setTimeout> = setTimeout(() => loadRecipients(id, 0, value), 300);
		updateRecipientState(id, { search: value, debounceTimer: timer });
	}

	function formatAudienceLabel(type: AudienceType, broadcast: BroadcastResponse): string {
		switch (type) {
			case 'ALL_USERS':
				return 'All Users';
			case 'ALL_ORGANIZATION_OWNERS':
				return 'All Org Owners';
			case 'ORGANIZATION':
				return broadcast.audienceTargetName
					? broadcast.audienceTargetName
					: broadcast.audienceTargetId
						? `Organization #${broadcast.audienceTargetId}`
						: 'Organization';
			case 'USER':
				return broadcast.audienceTargetName
					? broadcast.audienceTargetName
					: broadcast.audienceTargetId
						? `User #${broadcast.audienceTargetId}`
						: 'Single User';
			case 'GLOBAL_ROLE':
				return broadcast.audienceRole ?? 'Role';
			default:
				return type;
		}
	}
</script>

<DataTable {columns} {service} title="Broadcast History" icon={History}>
	{#snippet row(broadcast: BroadcastResponse)}
		{@const cat = categoryConfig[broadcast.category]}
		{@const CategoryIcon = cat.icon}
		<div class="border-b border-border/30 last:border-0">
			<button
				type="button"
				onclick={() => toggleExpand(broadcast.id)}
				class="flex items-center gap-2 px-4 py-3 w-full text-left hover:bg-muted/30 transition-colors"
			>
				<ChevronDown
					class="h-4 w-4 shrink-0 text-muted-foreground transition-transform {expandedId === broadcast.id
						? ''
						: '-rotate-90'}"
				/>
				<div class="grid grid-cols-12 items-center gap-4 flex-1 min-w-0">
					<div class="col-span-2 text-sm text-muted-foreground tabular-nums">
						{formatDateTime(broadcast.createdAt)}
					</div>
					<div class="col-span-3 text-sm font-medium truncate">
						{broadcast.title}
					</div>
					<div class="col-span-2">
						<Badge variant={cat.variant} class="gap-1">
							<CategoryIcon class="h-3 w-3" />
							{cat.label}
						</Badge>
					</div>
					<div class="col-span-3 text-sm text-muted-foreground truncate">
						{formatAudienceLabel(broadcast.audienceType, broadcast)}
						<span class="text-xs">({broadcast.recipientCount})</span>
					</div>
					<div class="col-span-2 text-sm text-muted-foreground truncate text-right">
						{broadcast.sentByEmail}
					</div>
				</div>
			</button>

			{#if expandedId === broadcast.id}
				{@const rs = getRecipientState(broadcast.id)}
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
							loading={rs.loading}
							search={rs.search}
							data={rs.data}
							onSearchInput={(value: string) => handleSearchInput(broadcast.id, value)}
							onPageChange={(page: number) => loadRecipients(broadcast.id, page, rs.search)}
						/>
					</div>
				</div>
			{/if}
		</div>
	{/snippet}
</DataTable>
