<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchChannels } from '$lib/api/channel/UserChannelController';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Radio, Plus } from '@lucide/svelte';
	import {
		CreateChannelSheet,
		EditChannelSheet,
		ChannelRow,
		SendEventsHelpModal
	} from '$lib/components/channels';
	import { UserChannelService } from '$lib/services/user-channel-service';

	// Column definitions (inline like other tables)
	const columns: DataTableColumn<ChannelDetailsResponse>[] = [
		{
			key: 'name',
			label: 'Channel',
			sortable: true,
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search channels...',
			colSpan: 4
		},
		{
			key: 'description',
			label: 'Description',
			colSpan: 7
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
			colSpan: 2
		},
		{
			key: 'lastEventAt',
			label: 'Last Activity',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'isStale',
			label: 'Stale',
			filterable: true,
			filterType: 'BOOLEAN',
			filterOptions: [
				{ value: 'true', label: 'Stale only' },
				{ value: 'false', label: 'Active only' }
			],
			colSpan: 0
		},
		{
			key: 'actions',
			colSpan: 1
		}
	];



	// Services
	const dataTableService = createDataTableService<ChannelDetailsResponse>({
		fetchFn: searchChannels,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});
	const channelService: UserChannelService = new UserChannelService();

	// Sheet state
	let showCreateSheet: boolean = $state(false);
	let showEditSheet: boolean = $state(false);
	let editingChannel: ChannelDetailsResponse | null = $state(null);
	let processing: boolean = $state(false);

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
		slug: string,
		description: string | undefined
	): Promise<void> {
		processing = true;
		try {
			await channelService.createChannel(name, slug, description);
			showCreateSheet = false;
			dataTableService.load();
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
			await channelService.updateChannel(channelId, name, description);
			closeEditSheet();
			dataTableService.load();
		} finally {
			processing = false;
		}
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.pauseChannel(channel.id ?? 0);
		dataTableService.load();
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.resumeChannel(channel.id ?? 0);
		dataTableService.load();
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.deleteChannel(channel);
		dataTableService.load();
	}

	onMount(() => dataTableService.load());
</script>

<svelte:head>
	<title>My Channels - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
	<!-- Header -->
	<div class="flex items-center justify-between mb-8">
		<div>
			<h1 class="text-3xl font-bold text-primary">
				My Channels
			</h1>
			<p class="text-muted-foreground mt-2">
				Manage your personal channels for organizing events
			</p>
		</div>
		<div class="flex items-center gap-3">
			<Button onclick={() => (showCreateSheet = true)}>
				<Plus class="mr-2 h-4 w-4" />
				New Channel
			</Button>
		</div>
	</div>

		<!-- DataTable -->
		<DataTable columns={columns} service={dataTableService} title="All Channels" icon={Radio}>
			{#snippet headerActions()}
				<SendEventsHelpModal apiKeySettingsUrl="/developer" />
			{/snippet}
			{#snippet row(channel: ChannelDetailsResponse)}
				<ChannelRow
					{channel}
					canManage={true}
					onEdit={openEditSheet}
					onPause={handlePauseChannel}
					onResume={handleResumeChannel}
					onDelete={handleDeleteChannel}
				/>
			{/snippet}
		</DataTable>
	</div>
</main>

<!-- Modals/Sheets -->
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
