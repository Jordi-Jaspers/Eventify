<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Badge } from '$lib/components/ui/badge';
	import { Textarea } from '$lib/components/ui/textarea';
	import * as Sheet from '$lib/components/ui/sheet';
	import { InlineEditableText } from '$lib/components/ui/inline-editable-text';
	import { InfoCard } from '$lib/components/ui/info-card';
	import {
		Radio,
		LoaderCircle,
		Copy,
		Terminal,
		Pause,
		Play,
		Trash2,
		Eye
	} from '@lucide/svelte';
	import type { ChannelDetailsResponse, TimelineDuration, Severity } from '$lib/api/models';
	import {
		getChannelStatusVariant,
		getChannelStatusLabel,
		copyCurlToClipboard,
		copySlugToClipboard,
		getRelativeActivityTime
	} from '$lib/utils/channel';
	import { formatDate } from '$lib/utils/date';
	import StaleActivityBadge from './StaleActivityBadge.svelte';
	import { DurationDetailsModal } from '$lib/components/monitor';
	import { createDurationService } from '$lib/api/monitor/service/DurationService.svelte';
	import { createChannelDetailsSheetService } from './ChannelDetailsSheetService.svelte';

	interface Props {
		open: boolean;
		channel: ChannelDetailsResponse | null;
		orgId?: number;
		canManage?: boolean;
		onOpenChange: (open: boolean) => void;
		onUpdate: (channelId: number, name: string, description: string | undefined) => Promise<void>;
		onPause: (channel: ChannelDetailsResponse) => Promise<void>;
		onResume: (channel: ChannelDetailsResponse) => Promise<void>;
		onDelete: (channel: ChannelDetailsResponse) => Promise<void>;
	}

	let {
		open,
		channel,
		orgId,
		canManage = true,
		onOpenChange,
		onUpdate,
		onPause,
		onResume,
		onDelete
	}: Props = $props();

	// Services
	const service = createChannelDetailsSheetService();
	const durationService = createDurationService();

	// Duration modal state
	let showDurationModal: boolean = $state(false);
	let selectedDuration: TimelineDuration | null = $state(null);
	let loadingViewEvents: boolean = $state(false);

	// Derived values
	const lastActivity = $derived(channel ? getRelativeActivityTime(channel.lastEventAt) : 'No activity');
	const hasActivity = $derived(!!channel?.lastEventAt);

	// Reset edit states when sheet closes or channel changes
	$effect(() => {
		if (!open || !channel) {
			service.resetEditStates();
		}
	});

	// Save handlers that call the onUpdate callback
	async function saveName(newName: string): Promise<void> {
		if (!channel) return;
		if (newName === channel.name) return;
		await onUpdate(channel.id!, newName, channel.description);
	}

	async function saveDescription(newDescription: string): Promise<void> {
		if (!channel) return;
		const desc = newDescription || undefined;
		if (desc === (channel.description || undefined)) return;
		await onUpdate(channel.id!, channel.name!, desc);
	}

	async function handleViewEvents(): Promise<void> {
		if (!channel?.id || !channel.lastEventAt) return;

		loadingViewEvents = true;
		try {
			await durationService.load(channel.id, orgId, channel.lastEventAt);
			selectedDuration = durationService.selectedDuration;
			showDurationModal = true;
		} finally {
			loadingViewEvents = false;
		}
	}
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="w-full sm:max-w-md bg-background/95 backdrop-blur-xl border-border/50 overflow-y-auto p-0">
		{#if channel}
			<!-- Hero Header with Gradient -->
			<div class="relative bg-gradient-to-br from-primary/20 via-accent/15 to-background/50 pt-8 pb-12 px-6 backdrop-blur-sm">
				<div class="absolute inset-0 bg-grid-white/5"></div>
				<div class="relative flex flex-col items-center text-center">
					<!-- Channel Icon -->
					<div class="h-20 w-20 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/25 ring-4 ring-background">
						<Radio class="h-10 w-10 text-white" />
					</div>
					
					<!-- Name (Editable) -->
					<div class="mt-4 w-full max-w-xs">
						<InlineEditableText
							value={channel.name ?? ''}
							editing={service.nameEdit.state.editing}
							saving={service.nameEdit.state.saving}
							tempValue={service.nameEdit.state.tempValue}
							canEdit={canManage}
							onStartEdit={() => service.nameEdit.startEdit(channel.name ?? '')}
							onSave={() => service.nameEdit.save(saveName)}
							onCancel={() => service.nameEdit.cancel()}
							onTempValueChange={(v) => service.nameEdit.updateTempValue(v)}
							onKeydown={(e) => service.nameEdit.handleKeydown(e, saveName)}
							class="justify-center"
							inputClass="text-center font-semibold bg-background/80 border-border"
						>
							<h2 class="text-xl font-semibold">{channel.name}</h2>
						</InlineEditableText>
					</div>

					<!-- Slug (Click to Copy) -->
					<button
						onclick={() => copySlugToClipboard(channel)}
						class="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-primary transition-colors group"
					>
						<span class="font-mono">{channel.slug}</span>
						<Copy class="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
					</button>

					<!-- Status Badges -->
					<div class="mt-3 flex items-center gap-2">
						<Badge variant={getChannelStatusVariant(channel.status)} class="px-3 py-1">
							{getChannelStatusLabel(channel.status)}
						</Badge>
						<StaleActivityBadge isStale={channel.isStale ?? false} />
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 py-4 -mt-6 space-y-4">
				<!-- Description Card -->
				<div class="rounded-xl border border-border/50 bg-card/50 backdrop-blur-sm p-4 shadow-sm">
					<div class="flex items-center justify-between mb-2">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Description</p>
					</div>
					{#if service.descriptionEdit.state.editing}
						<div class="space-y-3">
							<Textarea
								bind:value={service.descriptionEdit.state.tempValue}
								onkeydown={(e) => {
									if (e.key === 'Escape') service.descriptionEdit.cancel();
								}}
								disabled={service.descriptionEdit.state.saving}
								class="bg-background/50 border-border resize-none min-h-[80px]"
								rows={3}
								placeholder="Add a description..."
								autofocus
							/>
							<div class="flex justify-end gap-2">
								<Button
									variant="ghost"
									size="sm"
									onclick={() => service.descriptionEdit.cancel()}
									disabled={service.descriptionEdit.state.saving}
								>
									Cancel
								</Button>
								<Button
									size="sm"
									onclick={() => service.descriptionEdit.save(saveDescription)}
									disabled={service.descriptionEdit.state.saving}
								>
									{#if service.descriptionEdit.state.saving}
										<LoaderCircle class="mr-2 h-3 w-3 animate-spin" />
									{/if}
									Save
								</Button>
							</div>
						</div>
					{:else}
						<button
							class="w-full text-left group flex items-center justify-between"
							onclick={() => service.descriptionEdit.startEdit(channel.description ?? '')}
							disabled={!canManage}
						>
							<p class="text-sm {!channel.description ? 'text-muted-foreground italic' : ''}">
								{channel.description || 'No description'}
							</p>
							{#if canManage}
								<span class="text-muted-foreground hover:text-primary transition-colors p-1 hover:bg-muted/50 rounded opacity-0 group-hover:opacity-100">
									✏️
								</span>
							{/if}
						</button>
					{/if}
				</div>

				<!-- Info Grid -->
				<div class="grid grid-cols-2 gap-3">
					<InfoCard label="Created" value={formatDate(channel.createdAt ?? '')} />
					<InfoCard label="Updated" value={formatDate(channel.updatedAt ?? '')} />
				</div>

				<!-- Last Activity Card -->
				<div class="rounded-xl border border-border/50 bg-card/50 backdrop-blur-sm p-4 shadow-sm">
					<div class="flex items-center justify-between">
						<div>
							<p class="text-xs text-muted-foreground uppercase tracking-wide">Last Activity</p>
							<p class="text-sm font-medium mt-1 flex items-center gap-1.5">
								{#if hasActivity}
									<span class="h-2 w-2 rounded-full bg-green-500"></span>
								{:else}
									<span class="h-2 w-2 rounded-full bg-muted-foreground"></span>
								{/if}
								{lastActivity}
							</p>
						</div>
						{#if hasActivity}
							<Button
								variant="outline"
								size="sm"
								onclick={handleViewEvents}
								disabled={loadingViewEvents}
							>
								{#if loadingViewEvents}
									<LoaderCircle class="mr-2 h-3.5 w-3.5 animate-spin" />
								{:else}
									<Eye class="mr-2 h-3.5 w-3.5" />
								{/if}
								View Events
							</Button>
						{/if}
					</div>
				</div>

				<!-- Quick Actions -->
				{#if canManage}
					<div>
						<p class="text-xs text-muted-foreground uppercase tracking-wide mb-3">Quick Actions</p>
						<div class="grid grid-cols-2 gap-2">
							<Button
								variant="outline"
								size="sm"
								class="justify-start"
								onclick={() => copySlugToClipboard(channel)}
							>
								<Copy class="mr-2 h-4 w-4" />
								Copy Slug
							</Button>
							<Button
								variant="outline"
								size="sm"
								class="justify-start"
								onclick={() => copyCurlToClipboard(channel.slug)}
							>
								<Terminal class="mr-2 h-4 w-4" />
								Copy cURL
							</Button>
						</div>
					</div>
				{/if}
			</div>

			<!-- Footer Actions -->
			{#if canManage}
				<div class="sticky bottom-0 mt-4 p-4 border-t border-border/50 bg-background/98 backdrop-blur-xl shadow-lg">
					<div class="flex gap-2">
						{#if channel.status === 'ACTIVE'}
							<Button
								variant="outline"
								class="flex-1"
								onclick={() => service.handlePause(channel, onPause)}
								disabled={service.pauseLoading}
							>
								{#if service.pauseLoading}
									<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
								{:else}
									<Pause class="mr-2 h-4 w-4" />
								{/if}
								Pause
							</Button>
						{:else}
							<Button
								variant="default"
								class="flex-1"
								onclick={() => service.handleResume(channel, onResume)}
								disabled={service.resumeLoading}
							>
								{#if service.resumeLoading}
									<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
								{:else}
									<Play class="mr-2 h-4 w-4" />
								{/if}
								Resume
							</Button>
						{/if}
						<Button
							variant="destructive"
							onclick={() => service.handleDelete(channel, onDelete)}
							disabled={service.deleteLoading}
						>
							{#if service.deleteLoading}
								<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
							{:else}
								<Trash2 class="mr-2 h-4 w-4" />
							{/if}
							Delete
						</Button>
						<Button variant="outline" onclick={() => onOpenChange(false)}>
							Close
						</Button>
					</div>
				</div>
			{/if}
		{/if}
	</Sheet.Content>
</Sheet.Root>

<!-- Duration Details Modal -->
{#if channel}
	<DurationDetailsModal
		bind:open={showDurationModal}
		onOpenChange={(o: boolean) => (showDurationModal = o)}
		channelName={channel.name ?? ''}
		currentSeverity={selectedDuration?.severity as Severity | null}
		bind:selectedDuration
		{orgId}
		channelId={channel.id}
	/>
{/if}
