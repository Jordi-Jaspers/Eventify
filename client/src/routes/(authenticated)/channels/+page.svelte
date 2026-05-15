<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import { searchChannels, getChannel } from '$lib/api/channel/UserChannelController';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Radio, Plus } from '@lucide/svelte';
	import {
		CreateChannelSheet,
		ChannelDetailsSheet,
		ChannelRow,
		ChannelBulkActionBar,
		SendEventsHelpModal
	} from '$lib/components/channels';
	import { UserChannelService } from '$lib/api/channel/service/UserChannelService';
	import { createChannelSelectionService } from '$lib/api/channel/service/ChannelSelectionService.svelte';
	import { channelTableColumns } from '$lib/config/channel-table-columns';

	// Services
	const dataTableService = createDataTableService<ChannelDetailsResponse>({
		fetchFn: searchChannels,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});
	const channelService: UserChannelService = new UserChannelService();
	const selection = createChannelSelectionService(() => dataTableService.items);

	// Sheet state
	let showCreateSheet: boolean = $state(false);
	let showDetailsSheet: boolean = $state(false);
	let selectedChannel: ChannelDetailsResponse | null = $state(null);
	let processing: boolean = $state(false);

	// Open details sheet for a channel
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
		await channelService.updateChannel(channelId, name, description);
		if (selectedChannel?.id === channelId) {
			selectedChannel = await getChannel(channelId);
		}
		dataTableService.load();
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.pauseChannel(channel.id ?? 0);
		if (selectedChannel?.id === channel.id) {
			selectedChannel = await getChannel(channel.id!);
		}
		dataTableService.load();
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.resumeChannel(channel.id ?? 0);
		if (selectedChannel?.id === channel.id) {
			selectedChannel = await getChannel(channel.id!);
		}
		dataTableService.load();
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		await channelService.deleteChannel(channel);
		closeDetailsSheet();
		dataTableService.load();
	}

	async function handleBulkPause(ids: number[]): Promise<void> {
		await channelService.pauseChannels(ids);
		dataTableService.load();
	}

	async function handleBulkResume(ids: number[]): Promise<void> {
		await channelService.resumeChannels(ids);
		dataTableService.load();
	}

	async function handleBulkDelete(ids: number[]): Promise<void> {
		await channelService.deleteChannels(ids);
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
		<DataTable
			columns={channelTableColumns}
			service={dataTableService}
			title="All Channels"
			icon={Radio}
			selectable={true}
			allSelected={selection.isAllSelected}
			indeterminate={selection.isIndeterminate}
			onToggleSelectAll={selection.toggleSelectAll}
		>
			{#snippet headerActions()}
				{#if selection.selectedChannels.length > 0}
					<ChannelBulkActionBar
						selectedChannels={selection.selectedChannels}
						onPause={handleBulkPause}
						onResume={handleBulkResume}
						onDelete={handleBulkDelete}
						onClearSelection={selection.clearSelection}
					/>
				{:else}
					<SendEventsHelpModal apiKeySettingsUrl="/developer" />
				{/if}
			{/snippet}
			{#snippet row(channel: ChannelDetailsResponse)}
				<ChannelRow
					{channel}
					canManage={true}
					selected={selection.selectedIds.has(channel.id ?? 0)}
					onEdit={openDetailsSheet}
					onPause={handlePauseChannel}
					onResume={handleResumeChannel}
					onDelete={handleDeleteChannel}
					onToggleSelect={selection.toggleSelectChannel}
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
<ChannelDetailsSheet
	open={showDetailsSheet}
	channel={selectedChannel}
	canManage={true}
	onOpenChange={(o) => {
		showDetailsSheet = o;
		if (!o) closeDetailsSheet();
	}}
	onUpdate={handleUpdateChannel}
	onPause={handlePauseChannel}
	onResume={handleResumeChannel}
	onDelete={handleDeleteChannel}
/>
