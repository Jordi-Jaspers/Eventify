<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn, DataTableService } from '$lib/components/data-table/types';
	import {
		searchOrganizationChannels,
		createOrganizationChannel,
		updateOrganizationChannel,
		pauseOrganizationChannel,
		resumeOrganizationChannel,
		deleteOrganizationChannel
	} from '$lib/api/organization/OrganizationChannelController';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import type {
		ChannelDetailsResponse,
		SortablePageInput,
		PageResource,
		UserOrganizationResponse,
		OrganizationResponse
	} from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Radio, Edit, Pause, Play, Trash2, Plus } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { toast } from 'svelte-sonner';
	import { CreateChannelSheet, EditChannelSheet } from '$lib/components/channels';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';

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

	// DataTable service - will be recreated when orgId changes
	let service: DataTableService<ChannelDetailsResponse> | undefined = $state(undefined);
	let lastLoadedOrgId: number = $state(0);

	// Recreate service when orgId changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			service = createDataTableService<ChannelDetailsResponse>({
				fetchFn: (input: SortablePageInput): Promise<PageResource<ChannelDetailsResponse>> =>
					searchOrganizationChannels(currentOrgId, input),
				pageSize: 10,
				defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
			});
			service.load();
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

	// Sheet state
	let showCreateSheet: boolean = $state(false);
	let showEditSheet: boolean = $state(false);
	let editingChannel: ChannelDetailsResponse | null = $state(null);
	let processing: boolean = $state(false);

	// Helper functions
	function getStatusBadgeVariant(status: string | undefined): 'success' | 'secondary' {
		return status === 'ACTIVE' ? 'success' : 'secondary';
	}

	function getStatusLabel(status: string | undefined): string {
		return status === 'ACTIVE' ? 'Active' : 'Paused';
	}

	function truncateDescription(
		description: string | undefined | null,
		maxLength: number = 80
	): string {
		if (!description) return 'No description';
		if (description.length <= maxLength) return description;
		return `${description.substring(0, maxLength)}...`;
	}

	// Sheet handlers
	function openEditSheet(channel: ChannelDetailsResponse): void {
		editingChannel = channel;
		showEditSheet = true;
	}

	function closeEditSheet(): void {
		showEditSheet = false;
		editingChannel = null;
	}

	async function handleCreateChannel(
		name: string,
		description: string | undefined
	): Promise<void> {
		processing = true;
		try {
			await createOrganizationChannel(orgId, { name, description });
			toast.success('Channel created successfully');
			showCreateSheet = false;
			service?.load();
		} catch (error) {
			toast.error('Failed to create channel');
		} finally {
			processing = false;
		}
	}

	async function handleUpdateChannel(
		channelId: number,
		name: string,
		description: string | undefined
	): Promise<void> {
		processing = true;
		try {
			await updateOrganizationChannel(orgId, channelId, { name, description });
			toast.success('Channel updated successfully');
			showEditSheet = false;
			editingChannel = null;
			service?.load();
		} catch (error) {
			toast.error('Failed to update channel');
		} finally {
			processing = false;
		}
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		try {
			await pauseOrganizationChannel(orgId, channel.id ?? 0);
			toast.success('Channel paused');
			service?.load();
		} catch (error) {
			toast.error('Failed to pause channel');
		}
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		try {
			await resumeOrganizationChannel(orgId, channel.id ?? 0);
			toast.success('Channel resumed');
			service?.load();
		} catch (error) {
			toast.error('Failed to resume channel');
		}
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		if (
			!confirm(
				`Are you sure you want to delete "${channel.name}"? This action cannot be undone.`
			)
		) {
			return;
		}

		try {
			await deleteOrganizationChannel(orgId, channel.id ?? 0);
			toast.success('Channel deleted');
			service?.load();
		} catch (error) {
			toast.error('Failed to delete channel');
		}
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
				<h1
					class="text-3xl font-bold text-primary"
				>
					Organization Channels
				</h1>
				<p class="text-muted-foreground mt-2">
					Manage channels for {orgName}
				</p>
			</div>
			{#if canManage}
				<Button
					onclick={() => (showCreateSheet = true)}
					class="bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all shadow-lg shadow-primary/20"
				>
					<Plus class="mr-2 h-4 w-4" />
					New Channel
				</Button>
			{/if}
		</div>

		<!-- DataTable -->
		{#if service}
			<DataTable {columns} {service} title="All Channels" icon={Radio}>
				{#snippet row(channel: ChannelDetailsResponse)}
					<div
						class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/50 backdrop-blur-sm hover:bg-card/70 hover:border-border transition-all text-left w-full"
					>
						<!-- Channel Name -->
						<div class="col-span-1 md:col-span-3">
							<div class="flex items-center gap-3">
								<div
									class="h-10 w-10 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center flex-shrink-0"
								>
									<Radio class="h-5 w-5 text-primary" />
								</div>
								<div class="min-w-0">
									<div class="font-medium truncate">{channel.name}</div>
									<div class="text-sm text-muted-foreground truncate md:hidden">
										{truncateDescription(channel.description, 40)}
									</div>
								</div>
							</div>
						</div>

						<!-- Description (desktop only) -->
						<div class="hidden md:flex md:col-span-5 items-center">
							<span class="text-sm text-muted-foreground truncate">
								{truncateDescription(channel.description, 240)}
							</span>
						</div>

						<!-- Status -->
						<div class="col-span-1 md:col-span-1 flex items-center">
							<Badge variant={getStatusBadgeVariant(channel.status)}>
								{getStatusLabel(channel.status)}
							</Badge>
						</div>

						<!-- Created -->
						<div class="col-span-1 md:col-span-2 flex items-center">
							<span class="text-sm text-muted-foreground whitespace-nowrap">
								<span class="md:hidden">Created: </span>
								{formatDate(channel.createdAt ?? '')}
							</span>
						</div>

						<!-- Actions -->
						{#if canManage}
							<div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
								<Button
									variant="ghost"
									size="icon"
									class="h-8 w-8 hover:bg-primary/10 hover:text-primary"
									onclick={() => openEditSheet(channel)}
									aria-label="Edit channel"
								>
									<Edit class="h-4 w-4" />
								</Button>
								{#if channel.status === 'ACTIVE'}
									<Button
										variant="ghost"
										size="icon"
										class="h-8 w-8 hover:bg-yellow-500/10 hover:text-yellow-500"
										onclick={() => handlePauseChannel(channel)}
										aria-label="Pause channel"
									>
										<Pause class="h-4 w-4" />
									</Button>
								{:else}
									<Button
										variant="ghost"
										size="icon"
										class="h-8 w-8 hover:bg-green-500/10 hover:text-green-500"
										onclick={() => handleResumeChannel(channel)}
										aria-label="Resume channel"
									>
										<Play class="h-4 w-4" />
									</Button>
								{/if}
								<Button
									variant="ghost"
									size="icon"
									class="h-8 w-8 hover:bg-destructive/10 hover:text-destructive"
									onclick={() => handleDeleteChannel(channel)}
									aria-label="Delete channel"
								>
									<Trash2 class="h-4 w-4" />
								</Button>
							</div>
						{:else}
							<div class="col-span-1 md:col-span-1"></div>
						{/if}
					</div>
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
		onOpenChange={(o) => (showCreateSheet = o)}
		onSubmit={handleCreateChannel}
	/>
	<EditChannelSheet
		open={showEditSheet}
		channel={editingChannel}
		updating={processing}
		onOpenChange={(o) => {
			showEditSheet = o;
			if (!o) closeEditSheet();
		}}
		onSubmit={handleUpdateChannel}
	/>
{/if}
