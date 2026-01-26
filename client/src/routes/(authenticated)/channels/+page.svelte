<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchChannels, pauseChannel, resumeChannel, deleteChannel, createChannel, updateChannel } from '$lib/api/channel/UserChannelController';
	import type { ChannelDetailsResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Radio, Edit, Pause, Play, Trash2, Plus } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { toast } from 'svelte-sonner';
	import { CreateChannelSheet, EditChannelSheet } from '$lib/components/channels';

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
			colSpan: 2
		}
	];

	// DataTable service
	const dataTableService = createDataTableService<ChannelDetailsResponse>({
		fetchFn: searchChannels,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

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

	function truncateDescription(description: string | undefined | null, maxLength: number = 80): string {
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

	async function handleCreateChannel(name: string, description: string | undefined): Promise<void> {
		processing = true;
		try {
			await createChannel({ name, description });
			toast.success('Channel created successfully');
			showCreateSheet = false;
			dataTableService.load();
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
			await updateChannel(channelId, { name, description });
			toast.success('Channel updated successfully');
			showEditSheet = false;
			editingChannel = null;
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to update channel');
		} finally {
			processing = false;
		}
	}

	async function handlePauseChannel(channel: ChannelDetailsResponse): Promise<void> {
		try {
			await pauseChannel(channel.id ?? 0);
			toast.success('Channel paused');
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to pause channel');
		}
	}

	async function handleResumeChannel(channel: ChannelDetailsResponse): Promise<void> {
		try {
			await resumeChannel(channel.id ?? 0);
			toast.success('Channel resumed');
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to resume channel');
		}
	}

	async function handleDeleteChannel(channel: ChannelDetailsResponse): Promise<void> {
		if (!confirm(`Are you sure you want to delete "${channel.name}"? This action cannot be undone.`)) {
			return;
		}

		try {
			await deleteChannel(channel.id ?? 0);
			toast.success('Channel deleted');
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to delete channel');
		}
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
			<Button onclick={() => (showCreateSheet = true)}>
				<Plus class="mr-2 h-4 w-4" />
				New Channel
			</Button>
		</div>

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Channels" icon={Radio}>
			{#snippet row(channel: ChannelDetailsResponse)}
				<div
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/50 backdrop-blur-sm hover:bg-card/70 hover:border-border transition-all text-left w-full"
				>
					<!-- Channel Name -->
					<div class="col-span-1 md:col-span-3">
						<div class="flex items-center gap-3">
							<div class="h-10 w-10 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center flex-shrink-0">
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
				</div>
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
