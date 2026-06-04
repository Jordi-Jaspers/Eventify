<script lang="ts">
	import type { Snippet } from 'svelte';
	import { Plus, AlertTriangle } from '@lucide/svelte';
	import { AdapterConfigList, AdapterConfigForm } from '$lib/components/notification-settings';
	import ConfirmDialog from '$lib/components/ui/confirm-dialog/confirm-dialog.svelte';
	import { Separator } from '$lib/components/ui/separator';
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import type { AdapterConfigResponse, CreateAdapterConfigRequest, UpdateAdapterConfigRequest, AdapterType } from '$lib/api/models';

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
	}

	let { service, configs, connectionsDescription, alwaysActiveChannels }: Props = $props();

	let activeTab = $state<'connections' | 'alerts'>('connections');
	let showAddForm = $state(false);
	let editingConfig = $state<AdapterConfigResponse | null>(null);
	let deletingConfig = $state<AdapterConfigResponse | null>(null);

	const cardTitle = $derived(activeTab === 'connections' ? 'Connections' : 'Alerts');
	const cardDescription = $derived(
		activeTab === 'connections' ? connectionsDescription : 'Configure alert rules for your notifications.'
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
			<div class="flex flex-col items-center justify-center py-12 gap-3 text-muted-foreground">
				<AlertTriangle class="w-8 h-8 opacity-40" />
				<p class="text-sm">Alert rules coming soon.</p>
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
