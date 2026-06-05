<script lang="ts">
	import type { Snippet } from 'svelte';
	import { Plus, Bell, Info, LoaderCircle } from '@lucide/svelte';
	import { AdapterConfigList, AdapterConfigForm } from '$lib/components/notification-settings';
	import ConfirmDialog from '$lib/components/ui/confirm-dialog/confirm-dialog.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import type { AdapterConfigResponse, CreateAdapterConfigRequest, UpdateAdapterConfigRequest, AdapterType, SubscriptionResponse, CreateSubscriptionRequest, UpdateSubscriptionRequest, SortablePageInput, SortDirection } from '$lib/api/models';
	import { SortableTableHeader } from '$lib/components/ui/table';
	import { SubscriptionTableRow, SubscriptionDialog, DeleteSubscriptionDialog } from '$lib/components/subscriptions';
	import { searchPersonalSubscriptions, createPersonalSubscription, updatePersonalSubscription, deletePersonalSubscription } from '$lib/api/subscription/SubscriptionController';
	import { searchOrgSubscriptions, createOrgSubscription, updateOrgSubscription, deleteOrgSubscription } from '$lib/api/subscription/OrgSubscriptionController';
	import { handleError, formatValidationErrors } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';

	interface AdapterService {
		configs: AdapterConfigResponse[];
		loading: boolean;
		saving: boolean;
		addConfig(req: CreateAdapterConfigRequest): Promise<void>;
		updateConfig(id: number, req: UpdateAdapterConfigRequest): Promise<void>;
		deleteConfig(id: number): Promise<void>;
		testConfig(adapterType: string, webhookUrl: string): Promise<void>;
	}

	interface Props {
		service: AdapterService;
		configs: AdapterConfigResponse[];
		connectionsDescription: string;
		alwaysActiveChannels?: Snippet;
		scope: 'personal' | 'organization';
		orgId?: number;
	}

	let { service, configs, connectionsDescription, alwaysActiveChannels, scope, orgId }: Props = $props();

	let activeTab = $state<'connections' | 'alerts'>('connections');
	let showAddForm = $state(false);
	let editingConfig = $state<AdapterConfigResponse | null>(null);
	let deletingConfig = $state<AdapterConfigResponse | null>(null);

	// Subscription state
	let subscriptions = $state<SubscriptionResponse[]>([]);
	let subscriptionsLoading = $state(false);
	let subscriptionsSaving = $state(false);
	let subscriptionsDeleting = $state(false);
	let subscriptionsTotal = $state(0);
	let searchQuery = $state('');
	let pageNumber = $state(0);
	const pageSize = 20;

	let sortKey = $state<string | null>('createdAt');
	let sortDirection = $state<SortDirection>('DESC');

	interface TableColumn {
		key: string;
		label: string;
		sortable: boolean;
		colSpan?: number;
	}

	const tableColumns: TableColumn[] = [
		{ key: 'watchlistName', label: 'Watchlist', sortable: true, colSpan: 4 },
		{ key: 'severities', label: 'Severities', sortable: false, colSpan: 3 },
		{ key: 'adapters', label: 'Adapters', sortable: false, colSpan: 2 },
		{ key: 'createdAt', label: 'Created', sortable: true, colSpan: 2 },
		{ key: 'actions', label: '', sortable: false, colSpan: 1 }
	];

	let showSubscriptionDialog = $state(false);
	let editingSubscription = $state<SubscriptionResponse | null>(null);
	let deletingSubscription = $state<SubscriptionResponse | null>(null);

	const cardTitle = $derived(activeTab === 'connections' ? 'Connections' : 'Alerts');
	const cardDescription = $derived(
		activeTab === 'connections' ? connectionsDescription : 'Manage alert subscriptions for watchlist notifications.'
	);

	async function handleAdd(req: CreateAdapterConfigRequest | UpdateAdapterConfigRequest): Promise<void> {
		await service.addConfig(req as CreateAdapterConfigRequest);
		showAddForm = false;
	}

	async function handleUpdate(req: CreateAdapterConfigRequest | UpdateAdapterConfigRequest): Promise<void> {
		if (!editingConfig) return;
		await service.updateConfig(editingConfig.id, req as UpdateAdapterConfigRequest);
		editingConfig = null;
	}

	async function handleDeleteConfirm(): Promise<void> {
		if (!deletingConfig) return;
		await service.deleteConfig(deletingConfig.id);
		deletingConfig = null;
	}

	// Subscription helpers
	async function loadSubscriptions(): Promise<void> {
		subscriptionsLoading = true;
		try {
			const input: SortablePageInput = {
				pageNumber,
				pageSize,
				sortOrder: sortKey ? [{ name: sortKey, direction: sortDirection }] : [],
				searchInputs: searchQuery.trim()
					? [{ fieldName: 'watchlistName', textValue: searchQuery.trim() }]
					: []
			};
			const result = scope === 'organization' && orgId
				? await searchOrgSubscriptions(orgId, input)
				: await searchPersonalSubscriptions(input);
			subscriptions = result.content ?? [];
			subscriptionsTotal = result.totalElements ?? 0;
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load subscriptions');
			toast.error(message);
		} finally {
			subscriptionsLoading = false;
		}
	}

	async function handleSubscriptionSubmit(data: CreateSubscriptionRequest | UpdateSubscriptionRequest): Promise<void> {
		subscriptionsSaving = true;
		try {
			if (editingSubscription) {
				const updated = scope === 'organization' && orgId
					? await updateOrgSubscription(orgId, editingSubscription.id, data as UpdateSubscriptionRequest)
					: await updatePersonalSubscription(editingSubscription.id, data as UpdateSubscriptionRequest);
				subscriptions = subscriptions.map((s) => s.id === updated.id ? updated : s);
				toast.success('Subscription updated');
			} else {
				const created = scope === 'organization' && orgId
					? await createOrgSubscription(orgId, data as CreateSubscriptionRequest)
					: await createPersonalSubscription(data as CreateSubscriptionRequest);
				subscriptions = [created, ...subscriptions];
				subscriptionsTotal += 1;
				toast.success('Subscription created');
			}
			showSubscriptionDialog = false;
			editingSubscription = null;
		} catch (err: unknown) {
			const { message, validationErrors } = handleError(err, 'Failed to save subscription');
			toast.error(validationErrors ? formatValidationErrors(validationErrors) : message);
		} finally {
			subscriptionsSaving = false;
		}
	}

	async function handleSubscriptionDelete(): Promise<void> {
		if (!deletingSubscription) return;
		subscriptionsDeleting = true;
		try {
			if (scope === 'organization' && orgId) {
				await deleteOrgSubscription(orgId, deletingSubscription.id);
			} else {
				await deletePersonalSubscription(deletingSubscription.id);
			}
			subscriptions = subscriptions.filter((s) => s.id !== deletingSubscription!.id);
			subscriptionsTotal -= 1;
			toast.success('Subscription deleted');
			deletingSubscription = null;
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to delete subscription');
			toast.error(message);
		} finally {
			subscriptionsDeleting = false;
		}
	}

	function handleSort(key: string): void {
		if (sortKey === key) {
			sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
		} else {
			sortKey = key;
			sortDirection = 'ASC';
		}
		pageNumber = 0;
		loadSubscriptions();
	}

	$effect(() => {
		if (activeTab === 'alerts') {
			pageNumber = 0;
			loadSubscriptions();
		}
	});

	let searchTimeout: ReturnType<typeof setTimeout>;
	function handleSearchInput(): void {
		clearTimeout(searchTimeout);
		searchTimeout = setTimeout(() => {
			pageNumber = 0;
			loadSubscriptions();
		}, 300);
	}
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
	<div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>

	<CardHeader class="relative z-10 flex flex-row items-center justify-between">
		<div>
			<CardTitle>{cardTitle}</CardTitle>
			<CardDescription>{cardDescription}</CardDescription>
		</div>
		<div class="flex items-center bg-muted rounded-full p-1 gap-0.5">
			<button
				onclick={() => activeTab = 'connections'}
				class="px-3 py-1 text-xs font-medium rounded-full transition-all {activeTab === 'connections' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'}"
			>Connections</button>
			<button
				onclick={() => activeTab = 'alerts'}
				class="px-3 py-1 text-xs font-medium rounded-full transition-all {activeTab === 'alerts' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'}"
			>Alerts</button>
		</div>
	</CardHeader>

	<CardContent class="space-y-6 relative z-10">
		{#if activeTab === 'connections'}
			<div class="space-y-4">

				{#if alwaysActiveChannels}
					{@render alwaysActiveChannels()}
					<Separator />
					<p class="text-sm font-medium">Additional connections</p>
				{/if}

				{#if !editingConfig}
					{#if showAddForm}
						<div class="border border-border/50 rounded-lg p-4 bg-background/50 space-y-3">
							<p class="text-sm font-medium">New Connection</p>
						<AdapterConfigForm
							mode="add"
							saving={service.saving}
							onSubmit={handleAdd}
							onCancel={() => { showAddForm = false; }}
							onTest={(adapterType, webhookUrl) => service.testConfig(adapterType, webhookUrl)}
						/>
						</div>
					{:else}
						<button
							class="w-full flex items-center gap-2 px-4 py-3 rounded-lg border border-dashed border-border/50 hover:border-primary/50 hover:bg-accent/30 cursor-pointer transition-colors text-muted-foreground"
							onclick={() => (showAddForm = true)}
							aria-label="Add notification connection"
						>
							<Plus class="w-4 h-4" />
							<span class="text-sm">Add Connection</span>
						</button>
					{/if}
				{/if}

				<AdapterConfigList
					{configs}
					loading={service.loading}
					canEdit={true}
					editingId={editingConfig?.id ?? null}
					onEdit={(config) => { editingConfig = config; }}
					onDelete={(config) => { deletingConfig = config; }}
				>
					{#snippet editForm()}
						{#if editingConfig}
							<AdapterConfigForm
								mode="edit"
								adapterType={editingConfig.adapterType as AdapterType}
								initialData={{
									label: editingConfig.label,
									webhookUrl: typeof editingConfig.config?.['webhookUrl'] === 'string'
										? editingConfig.config['webhookUrl']
										: undefined,
									enabled: editingConfig.enabled
								}}
								saving={service.saving}
								onSubmit={handleUpdate}
								onCancel={() => (editingConfig = null)}
								onTest={(adapterType, webhookUrl) => service.testConfig(adapterType, webhookUrl)}
							/>
						{/if}
					{/snippet}
				</AdapterConfigList>

			</div>
		{:else}
			<div class="space-y-4">
				{#if scope === 'organization'}
					<div class="flex items-start gap-2 rounded-lg border border-blue-500/30 bg-blue-500/10 px-3 py-2.5 text-sm text-blue-700 dark:text-blue-400">
						<Info class="mt-0.5 h-4 w-4 shrink-0" />
						<p>Subscriptions here notify <strong>all organization members</strong> via in-app + selected org adapters.</p>
					</div>
				{/if}

				<div class="flex items-center justify-between gap-3">
					<Input
						placeholder="Search by watchlist name or ID…"
						bind:value={searchQuery}
						oninput={handleSearchInput}
						class="max-w-xs h-8 text-sm"
					/>
					<Button
						size="sm"
						onclick={() => { editingSubscription = null; showSubscriptionDialog = true; }}
						class="shrink-0"
					>
						<Plus class="mr-1.5 h-4 w-4" />
						Add Subscription
					</Button>
				</div>

				{#if subscriptionsLoading}
					<div class="flex items-center justify-center py-10 gap-2 text-muted-foreground">
						<LoaderCircle class="h-5 w-5 animate-spin" />
						<span class="text-sm">Loading subscriptions…</span>
					</div>
				{:else if subscriptions.length === 0}
					<div class="flex flex-col items-center justify-center py-12 gap-3 text-muted-foreground">
						<Bell class="w-8 h-8 opacity-40" />
						<p class="text-sm text-center">No alert subscriptions yet.<br />Subscribe to a watchlist to receive severity alerts.</p>
					</div>
				{:else}
					<div class="rounded-lg border border-border/50 overflow-hidden divide-y divide-border/40">
						<SortableTableHeader
							columns={tableColumns}
							currentSortKey={sortKey}
							currentSortDirection={sortDirection}
							onSort={handleSort}
						/>
						{#each subscriptions as subscription (subscription.id)}
							<SubscriptionTableRow
								{subscription}
								onEdit={(sub) => { editingSubscription = sub; showSubscriptionDialog = true; }}
								onDelete={(sub) => { deletingSubscription = sub; }}
							/>
						{/each}
					</div>
					{#if subscriptionsTotal > pageSize}
						<div class="flex items-center justify-between text-sm text-muted-foreground">
							<span>Showing {subscriptions.length} of {subscriptionsTotal}</span>
							<div class="flex gap-2">
								<Button variant="outline" size="sm" disabled={pageNumber === 0} onclick={() => { pageNumber -= 1; loadSubscriptions(); }}>Previous</Button>
								<Button variant="outline" size="sm" disabled={(pageNumber + 1) * pageSize >= subscriptionsTotal} onclick={() => { pageNumber += 1; loadSubscriptions(); }}>Next</Button>
							</div>
						</div>
					{/if}
				{/if}
			</div>
		{/if}
	</CardContent>
</Card>

<ConfirmDialog
	open={!!deletingConfig}
	title="Remove Connection"
	confirmLabel="Remove"
	destructive={true}
	onOpenChange={(open) => { if (!open) deletingConfig = null; }}
	onConfirm={handleDeleteConfirm}
>
	{#snippet description()}
		Are you sure you want to remove <strong>{deletingConfig?.label}</strong>? You will no longer receive notifications through this connection.
	{/snippet}
</ConfirmDialog>

<SubscriptionDialog
	open={showSubscriptionDialog}
	saving={subscriptionsSaving}
	subscription={editingSubscription}
	isOrgWatchlist={scope === 'organization'}
	preloadedAdapterConfigs={scope === 'organization' ? configs : undefined}
	onOpenChange={(open) => { if (!open) { showSubscriptionDialog = false; editingSubscription = null; } }}
	onSubmit={handleSubscriptionSubmit}
/>

<DeleteSubscriptionDialog
	open={!!deletingSubscription}
	deleting={subscriptionsDeleting}
	watchlistId={deletingSubscription?.watchlistId}
	onOpenChange={(open) => { if (!open) deletingSubscription = null; }}
	onConfirm={handleSubscriptionDelete}
/>
