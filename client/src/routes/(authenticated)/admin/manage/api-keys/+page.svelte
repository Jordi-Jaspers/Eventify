<script lang="ts">
	import { onMount } from 'svelte';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import {
		getAdminApiKeyStats,
		searchAdminApiKeys,
		revokeAdminApiKey,
		searchAdminApiKeyAudit
	} from '$lib/api/admin/AdminApiKeyController';
	import type {
		AdminApiKeyStatsResponse,
		ApiKeyResponse,
		AdminApiKeyAuditResponse
	} from '$lib/api/models';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { formatDate } from '$lib/utils/date';
	import {
		Key,
		MoreVertical,
		Trash2
	} from '@lucide/svelte';
	import { PageHeader } from '$lib/components/ui/page-header';
	import {
		RecentRevocations,
		AdminApiKeyStats,
		getScopeBadgeClass,
		formatNumber,
		formatLastUsed
	} from '$lib/components/admin';
	import { RevokeApiKeyAlertDialog } from '$lib/components/api-keys';

	let stats: AdminApiKeyStatsResponse | null = $state(null);
	let recentRevocations: AdminApiKeyAuditResponse[] = $state([]);
	let loadingStats: boolean = $state(true);
	let loadingRevocations: boolean = $state(true);
	let error: string | null = $state(null);

	// Revoke modal state
	let showRevokeDialog: boolean = $state(false);
	let keyToRevoke: ApiKeyResponse | null = $state(null);
	let revoking: boolean = $state(false);

	// DataTable columns configuration
	const columns: DataTableColumn<ApiKeyResponse>[] = [
		{
			key: 'searchTerm',
			label: 'Name',
			sortable: true,
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search by name, key, owner...',
			colSpan: 3
		},
		{
			key: 'owner',
			label: 'Owner',
			colSpan: 2
		},
		{
			key: 'scope',
			label: 'Scope',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ value: 'USER', label: 'User' },
				{ value: 'ORGANIZATION', label: 'Organization' }
			],
			colSpan: 2
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'lastUsedAt',
			label: 'Last Used',
			sortable: true,
			colSpan: 1
		},
		{
			key: 'totalRequests',
			label: 'Requests',
			sortable: true,
			colSpan: 1
		},
		{
			key: 'actions',
			colSpan: 1
		}
	];

	// DataTable service
	const dataTableService = createDataTableService<ApiKeyResponse>({
		fetchFn: searchAdminApiKeys,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

	async function loadStats(): Promise<void> {
		loadingStats = true;
		error = null;

		try {
			stats = await getAdminApiKeyStats();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to load API key statistics'
			);
			error = message;
			toast.error(message);
		} finally {
			loadingStats = false;
		}
	}

	async function loadRecentRevocations(): Promise<void> {
		loadingRevocations = true;

		try {
			const result = await searchAdminApiKeyAudit({
				pageNumber: 0,
				pageSize: 5,
				sortOrder: [{ name: 'revokedAt', direction: 'DESC' }]
			});
			recentRevocations = result.content || [];
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to load recent revocations'
			);
			console.error(message);
		} finally {
			loadingRevocations = false;
		}
	}

	function openRevokeDialog(key: ApiKeyResponse): void {
		keyToRevoke = key;
		showRevokeDialog = true;
	}

	function closeRevokeDialog(): void {
		showRevokeDialog = false;
		keyToRevoke = null;
	}

	async function handleRevoke(): Promise<void> {
		if (!keyToRevoke) return;

		revoking = true;
		try {
			await revokeAdminApiKey(keyToRevoke.id);
			toast.success(`API key "${keyToRevoke.name}" has been revoked`);
			closeRevokeDialog();
			dataTableService.load();
			loadStats();
			loadRecentRevocations();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to revoke API key');
			toast.error(message);
		} finally {
			revoking = false;
		}
	}

	onMount(() => {
		loadStats();
		loadRecentRevocations();
		dataTableService.load();
	});
</script>

<svelte:head>
	<title>API Keys - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<PageHeader title="API Keys" description="Monitor and manage API keys across the platform" />

		<!-- Statistics Cards -->
		<AdminApiKeyStats {stats} loading={loadingStats} {error} onRetry={loadStats} />

		<!-- All API Keys DataTable -->
		<DataTable {columns} service={dataTableService} title="All API Keys" icon={Key}>
			{#snippet row(key: ApiKeyResponse)}
				<div
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 px-4 py-4 hover:bg-muted/30 transition-all group"
				>
					<!-- Key + Name -->
					<div class="col-span-1 md:col-span-3">
						<div class="space-y-1">
							<div
								class="font-mono text-xs text-muted-foreground group-hover:text-primary/80 transition-colors"
							>
								{key.maskedKey}
							</div>
							<div class="font-medium truncate">{key.name}</div>
						</div>
					</div>

					<!-- Owner -->
					<div class="hidden md:flex md:col-span-2 items-center">
						<span class="text-sm truncate" title={key.owner?.email ?? key.owner?.name}>
							{key.owner?.name ?? 'Unknown'}
						</span>
					</div>

					<!-- Scope -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<Badge class={getScopeBadgeClass(key.scope)}>
							{key.scope}
						</Badge>
					</div>

					<!-- Created -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm text-muted-foreground">
							<span class="md:hidden">Created: </span>
							{formatDate(key.createdAt ?? '')}
						</span>
					</div>

					<!-- Last Used -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<span class="text-sm text-muted-foreground truncate">
							<span class="md:hidden">Last Used: </span>
							{formatLastUsed(key.lastUsedAt)}
						</span>
					</div>

					<!-- Total Requests -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<span class="text-sm font-medium">{formatNumber(key.totalRequests)}</span>
					</div>

					<!-- Actions -->
					<div class="col-span-1 md:col-span-1 flex items-center justify-center">
						<DropdownMenu.Root>
							<DropdownMenu.Trigger>
								<Button variant="ghost" size="sm" class="h-8 w-8 p-0">
									<MoreVertical class="h-4 w-4" />
									<span class="sr-only">Actions</span>
								</Button>
							</DropdownMenu.Trigger>
							<DropdownMenu.Content
								align="end"
								class="w-48 bg-card/95 backdrop-blur-xl border-border/50"
							>
								<DropdownMenu.Item
									onclick={() => openRevokeDialog(key)}
									class="cursor-pointer text-destructive focus:text-destructive"
								>
									<Trash2 class="mr-2 h-4 w-4" />
									Revoke Key
								</DropdownMenu.Item>
							</DropdownMenu.Content>
						</DropdownMenu.Root>
					</div>
				</div>
			{/snippet}
		</DataTable>

		<!-- Recent Revocations -->
		<RecentRevocations revocations={recentRevocations} loading={loadingRevocations} />
	</div>
</main>

<!-- Revoke Confirmation Dialog -->
<RevokeApiKeyAlertDialog
	open={showRevokeDialog}
	onOpenChange={(o) => { if (!o) closeRevokeDialog(); }}
	keyName={keyToRevoke?.name}
	isRevoking={revoking}
	onConfirm={handleRevoke}
	apiKey={keyToRevoke}
/>
