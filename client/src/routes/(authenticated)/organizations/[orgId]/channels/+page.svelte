<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn, DataTableService } from '$lib/components/data-table/types';
	import { searchOrganizationChannels } from '$lib/api/organization/OrganizationChannelController';
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
	import { CreateChannelSheet, EditChannelSheet, ChannelRow } from '$lib/components/channels';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { ChannelService } from '$lib/services/channel-service';

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
		// Global admin can always manage
		if (isGlobalAdmin) return true;
		// Check if user is OWNER or ADMIN of this specific org
		if (!organizationFromStore) return false;
		const role: string | undefined = organizationFromStore.role;
		return role === 'OWNER' || role === 'ADMIN';
	});

	// For global admins not in the org, fetch org details from API (for display purposes)
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let lastFetchedOrgId: number = $state(0);

	// Fetch org for global admin if not in store
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

	// Columns configuration
	const columns: DataTableColumn<ChannelDetailsResponse>[] = [
		{
			key: 'search',
			label: 'Channel',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search channels...',
			colSpan: 3
		},
		{
			key: 'description',
			label: 'Description',
			colSpan: 5
		},
		{
			key: 'status',
			label: 'Status',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ value: 'ACTIVE', label: 'Active' },
				{ value: 'PAUSED', label: 'Paused' }
			],
			colSpan: 1
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'actions',
			colSpan: 1
		}
	];

	// Sheet state - consolidated into single object
	let sheetState: {
		showCreate: boolean;
		showEdit: boolean;
		editingChannel: ChannelDetailsResponse | null;
		processing: boolean;
	} = $state({
		showCreate: false,
		showEdit: false,
		editingChannel: null,
		processing: false
	});

	// Sheet handlers
	function openEditSheet(channel: ChannelDetailsResponse): void {
		sheetState.editingChannel = channel;
		sheetState.showEdit = true;
	}

	function closeEditSheet(): void {
		sheetState.showEdit = false;
		sheetState.editingChannel = null;
	}

	async function handleCreateChannel(
		name: string,
		description: string | undefined
	): Promise<void> {
		sheetState.processing = true;
		try {
			await channelService.createChannel(name, description);
			sheetState.showCreate = false;
			dataTableService?.load();
		} finally {
			sheetState.processing = false;
		}
	}

	async function handleUpdateChannel(
		channelId: number,
		name: string,
		description: string | undefined
	): Promise<void> {
		sheetState.processing = true;
		try {
			await channelService.updateChannel(channelId, name, description);
			closeEditSheet();
			dataTableService?.load();
		} finally {
			sheetState.processing = false;
		}
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.pauseChannel(channel.id ?? 0);
		dataTableService?.load();
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.resumeChannel(channel.id ?? 0);
		dataTableService?.load();
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.deleteChannel(channel);
		dataTableService?.load();
	}
</script>

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
				<Button onclick={() => (sheetState.showCreate = true)}>
					<Plus class="mr-2 h-4 w-4" />
					New Channel
				</Button>
			{/if}
		</div>

		<!-- DataTable -->
		{#if dataTableService}
			<DataTable {columns} service={dataTableService} title="All Channels" icon={Radio}>
				{#snippet row(channel: ChannelDetailsResponse)}
					<ChannelRow
						{channel}
						{canManage}
						onEdit={openEditSheet}
						onPause={handlePauseChannel}
						onResume={handleResumeChannel}
						onDelete={handleDeleteChannel}
					/>
				{/snippet}
			</DataTable>
		{/if}
	</div>
</main>

<!-- Modals/Sheets -->
{#if canManage}
	<CreateChannelSheet
		open={sheetState.showCreate}
		creating={sheetState.processing}
		onOpenChange={(o) => (sheetState.showCreate = o)}
		onSubmit={handleCreateChannel}
	/>
	<EditChannelSheet
		open={sheetState.showEdit}
		channel={sheetState.editingChannel}
		updating={sheetState.processing}
		onOpenChange={(o) => {
			sheetState.showEdit = o;
			if (!o) closeEditSheet();
		}}
		onSubmit={handleUpdateChannel}
	/>
{/if}
