<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableService } from '$lib/components/data-table/types';
	import { searchOrganizationChannels, getOrganizationChannel } from '$lib/api/organization/OrganizationChannelController';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import type {
		ChannelDetailsResponse,
		SortablePageInput,
		PageResource,
		UserOrganizationResponse,
		OrganizationResponse
	} from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Radio, Plus } from '@lucide/svelte';
	import {
		CreateChannelSheet,
		ChannelDetailsSheet,
		ChannelRow,
		ChannelBulkActionBar,
		SendEventsHelpModal
	} from '$lib/components/channels';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { ChannelService } from '$lib/api/channel/service/ChannelService';
	import { createChannelSelectionService } from '$lib/api/channel/service/ChannelSelectionService.svelte';
	import { channelTableColumns } from '$lib/config/channel-table-columns';

	// Reactive orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	// Get organization from store - find by orgId to handle navigation
	const organizationFromStore: UserOrganizationResponse | undefined = $derived(
		organizationStore.organizations.find(
			(org: UserOrganizationResponse) => org.organizationId === orgId
		)
	);

	// Check permissions - canManage if OWNER, ADMIN, or global ADMIN
	const isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	const canManage: boolean = $derived.by((): boolean => {
		if (isGlobalAdmin) return true;
		if (!organizationFromStore) return false;
		const role: string | undefined = organizationFromStore.role;
		return role === 'OWNER' || role === 'ADMIN';
	});

	// For global admins not in the org, fetch org details from API (for display purposes)
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let lastFetchedOrgId: number = $state(0);

	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		const needsFetch: boolean =
			isGlobalAdmin &&
			!organizationFromStore &&
			currentOrgId > 0 &&
			currentOrgId !== lastFetchedOrgId;

		if (needsFetch) {
			lastFetchedOrgId = currentOrgId;
			getOrganizationById(currentOrgId)
				.then((org: OrganizationResponse | null) => {
					adminFetchedOrg = org;
				})
				.catch(() => {
					adminFetchedOrg = null;
				});
		}
	});

	// Organization name for display
	const orgName: string = $derived.by((): string => {
		if (organizationFromStore) return organizationFromStore.organizationName ?? 'Organization';
		if (adminFetchedOrg) return adminFetchedOrg.name ?? 'Organization';
		return 'Organization';
	});

	// Channel service for CRUD operations
	let channelService: ChannelService = $derived(new ChannelService(orgId));

	// DataTable service - will be recreated when orgId changes
	let dataTableService: DataTableService<ChannelDetailsResponse> | undefined = $state(undefined);
	let lastLoadedOrgId: number = $state(0);

	// Recreate service when orgId changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			dataTableService = createDataTableService<ChannelDetailsResponse>({
				fetchFn: (input: SortablePageInput): Promise<PageResource<ChannelDetailsResponse>> =>
					searchOrganizationChannels(currentOrgId, input),
				pageSize: 10,
				defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
			});
			dataTableService.load();
			lastLoadedOrgId = currentOrgId;
		}
	});

	// Selection service
	const selection = createChannelSelectionService(
		() => dataTableService?.items ?? []
	);

	// Sheet state
	let showCreateSheet: boolean = $state(false);
	let showDetailsSheet: boolean = $state(false);
	let selectedChannel: ChannelDetailsResponse | null = $state(null);
	let processing: boolean = $state(false);

	// Sheet handlers
	function openDetailsSheet(channel: ChannelDetailsResponse): void {
		selectedChannel = channel;
		showDetailsSheet = true;
	}

	function closeDetailsSheet(): void {
		showDetailsSheet = false;
		selectedChannel = null;
	}

	async function handleCreateChannel(
		name: string,
		slug: string,
		description: string | undefined
	): Promise<void> {
		processing = true;
		try {
			await channelService.createChannel(name, slug, description);
			showCreateSheet = false;
			dataTableService?.load();
		} finally {
			processing = false;
		}
	}

	async function handleUpdateChannel(
		channelId: number,
		name: string,
		description: string | undefined
	): Promise<void> {
		await channelService.updateChannel(channelId, name, description);
		if (selectedChannel?.id === channelId) {
			selectedChannel = await getOrganizationChannel(orgId, channelId);
		}
		dataTableService?.load();
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.pauseChannel(channel.id ?? 0);
		if (selectedChannel?.id === channel.id) {
			selectedChannel = await getOrganizationChannel(orgId, channel.id!);
		}
		dataTableService?.load();
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.resumeChannel(channel.id ?? 0);
		if (selectedChannel?.id === channel.id) {
			selectedChannel = await getOrganizationChannel(orgId, channel.id!);
		}
		dataTableService?.load();
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.deleteChannel(channel);
		closeDetailsSheet();
		dataTableService?.load();
	}

	// Bulk action handlers
	async function handleBulkPause(ids: number[]): Promise<void> {
		await channelService.pauseChannels(ids);
		dataTableService?.load();
	}

	async function handleBulkResume(ids: number[]): Promise<void> {
		await channelService.resumeChannels(ids);
		dataTableService?.load();
	}

	async function handleBulkDelete(ids: number[]): Promise<void> {
		await channelService.deleteChannels(ids);
		dataTableService?.load();
	}</script>

<svelte:head>
	<title>Channels - {orgName} - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
	<!-- Header -->
	<div class="flex items-center justify-between mb-8">
		<div>
			<h1 class="text-3xl font-bold text-primary">Organization Channels</h1>
			<p class="text-muted-foreground mt-2">
				Manage channels for {orgName}
			</p>
		</div>
		{#if canManage}
			<div class="flex items-center gap-3">
				<Button onclick={() => (showCreateSheet = true)}>
					<Plus class="mr-2 h-4 w-4" />
					New Channel
				</Button>
			</div>
		{/if}
	</div>

		<!-- DataTable -->
		{#if dataTableService}
		<DataTable
				columns={channelTableColumns}
				service={dataTableService}
				title="All Channels"
				icon={Radio}
				selectable={canManage}
				allSelected={selection.isAllSelected}
				indeterminate={selection.isIndeterminate}
				onToggleSelectAll={selection.toggleSelectAll}
			>
				{#snippet headerActions()}
					{#if canManage && selection.selectedChannels.length > 0}
						<ChannelBulkActionBar
							selectedChannels={selection.selectedChannels}
							onPause={handleBulkPause}
							onResume={handleBulkResume}
							onDelete={handleBulkDelete}
							onClearSelection={selection.clearSelection}
						/>
					{:else}
						<SendEventsHelpModal
							apiKeySettingsUrl="/organizations/{orgId}/settings/api-keys"
						/>
					{/if}
				{/snippet}
				{#snippet row(channel: ChannelDetailsResponse)}
					<ChannelRow
						{channel}
						{canManage}
						selected={selection.selectedIds.has(channel.id ?? 0)}
						onEdit={openDetailsSheet}
						onPause={handlePauseChannel}
						onResume={handleResumeChannel}
						onDelete={handleDeleteChannel}
						onToggleSelect={canManage ? selection.toggleSelectChannel : undefined}
					/>
				{/snippet}
			</DataTable>
		{/if}
	</div>
</main>

<!-- Modals/Sheets -->
{#if canManage}
	<CreateChannelSheet
		open={showCreateSheet}
		creating={processing}
		onOpenChange={(o: boolean) => (showCreateSheet = o)}
		onSubmit={handleCreateChannel}
	/>
	<ChannelDetailsSheet
		open={showDetailsSheet}
		channel={selectedChannel}
		{orgId}
		{canManage}
		onOpenChange={(o: boolean) => {
			showDetailsSheet = o;
			if (!o) closeDetailsSheet();
		}}
		onUpdate={handleUpdateChannel}
		onPause={handlePauseChannel}
		onResume={handleResumeChannel}
		onDelete={handleDeleteChannel}
	/>
{/if}
