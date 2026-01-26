<script lang="ts">
	import { onMount } from 'svelte';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
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
		Users,
		Building2,
		TrendingUp,
		CircleAlert,
		Activity,
		Clock,
		ShieldAlert,
		MoreVertical,
		Trash2,
		BarChart3,
		CalendarDays,
		AlertTriangle
	} from '@lucide/svelte';

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
			dataTableService.load(); // Refresh table
			loadStats(); // Refresh stats
			loadRecentRevocations(); // Refresh audit log
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to revoke API key');
			toast.error(message);
		} finally {
			revoking = false;
		}
	}

	function getScopeBadgeClass(scope: string | undefined): string {
		if (scope === 'USER') return 'bg-blue-500/10 text-blue-500 border-blue-500/20 backdrop-blur-md';
		if (scope === 'ORGANIZATION')
			return 'bg-purple-500/10 text-purple-500 border-purple-500/20 backdrop-blur-md';
		return '';
	}

	function formatNumber(num: number | undefined): string {
		if (num === undefined || num === null) return '0';
		return num.toLocaleString();
	}

	function formatLastUsed(date: string | null | undefined): string {
		if (!date) return 'Never';
		return formatDate(date);
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
		<div class="mb-8 space-y-2">
			<h1 class="text-3xl font-bold text-primary">API Keys</h1>
			<p class="text-muted-foreground">Monitor and manage API keys across the platform</p>
		</div>

		<!-- Error Alert -->
		{#if error && !loadingStats}
			<Alert
				variant="destructive"
				class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
			>
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{error}
					<Button variant="outline" size="sm" class="ml-4" onclick={loadStats}> Retry</Button>
				</AlertDescription>
			</Alert>
		{/if}

		<!-- Statistics Cards (2 rows of 4) -->
		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
			<!-- Total Keys Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-primary/20 hover:border-primary/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-primary/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground">Total Keys</CardTitle>
						<Key class="h-5 w-5 text-primary" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent"
						>
							{formatNumber(stats?.totalKeys)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- User Keys Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-blue-500/20 hover:border-blue-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-blue-500/10 via-transparent to-blue-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground">User Keys</CardTitle>
						<Users class="h-5 w-5 text-blue-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-blue-500 to-blue-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.userKeys)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Org Keys Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-purple-500/20 hover:border-purple-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-purple-500/10 via-transparent to-purple-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground">Org Keys</CardTitle>
						<Building2 class="h-5 w-5 text-purple-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-purple-500 to-purple-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.organizationKeys)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Created This Month Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-green-500/20 hover:border-green-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-green-500/10 via-transparent to-green-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground"
							>Created This Month</CardTitle
						>
						<TrendingUp class="h-5 w-5 text-green-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-green-500 to-green-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.createdThisMonth)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Never Used Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-yellow-500/20 hover:border-yellow-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-yellow-500/10 via-transparent to-yellow-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground">Never Used</CardTitle>
						<Clock class="h-5 w-5 text-yellow-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-yellow-500 to-yellow-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.neverUsedKeys)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Expiring Soon Card -->
			<Card
				class="border-orange-500/30 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-orange-500/20 hover:border-orange-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-orange-500/10 via-transparent to-orange-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground"
							>Expiring (30d)</CardTitle
						>
						<AlertTriangle class="h-5 w-5 text-orange-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-orange-500 to-orange-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.expiringNext30Days)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Revoked This Month Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-red-500/20 hover:border-red-500/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-red-500/10 via-transparent to-red-500/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground"
							>Revoked This Month</CardTitle
						>
						<ShieldAlert class="h-5 w-5 text-red-500" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else}
						<div
							class="text-3xl font-bold bg-gradient-to-r from-red-500 to-red-400 bg-clip-text text-transparent"
						>
							{formatNumber(stats?.revokedThisMonth)}
						</div>
					{/if}
				</CardContent>
			</Card>

			<!-- Top Key Card -->
			<Card
				class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden hover:shadow-accent/20 hover:border-accent/50 transition-all duration-300"
			>
				<div
					class="absolute inset-0 bg-gradient-to-br from-accent/10 via-transparent to-accent/5 opacity-50"
				></div>
				<CardHeader class="relative z-10">
					<div class="flex items-center justify-between">
						<CardTitle class="text-sm font-medium text-muted-foreground">Top Key</CardTitle>
						<BarChart3 class="h-5 w-5 text-accent" />
					</div>
				</CardHeader>
				<CardContent class="relative z-10">
					{#if loadingStats}
						<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
					{:else if stats?.topKeysByUsage && stats.topKeysByUsage.length > 0}
						<div class="space-y-1">
							<div
								class="text-3xl font-bold bg-gradient-to-r from-accent to-primary bg-clip-text text-transparent"
							>
								{formatNumber(stats.topKeysByUsage[0].totalRequests)}
							</div>
							<div class="text-xs text-muted-foreground truncate">
								{stats.topKeysByUsage[0].name}
							</div>
						</div>
					{:else}
						<div class="text-sm text-muted-foreground">No usage data</div>
					{/if}
				</CardContent>
			</Card>
		</div>

		<!-- All API Keys DataTable -->
		<DataTable {columns} service={dataTableService} title="All API Keys" icon={Key}>
		{#snippet row(key: ApiKeyResponse)}
			<div
				class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 px-4 py-4 rounded-lg border border-border/50 bg-card/40 hover:bg-accent/5 transition-colors group"
			>
					<!-- Key + Name -->
					<div class="col-span-1 md:col-span-3">
						<div class="space-y-1">
							<div class="font-mono text-xs text-muted-foreground group-hover:text-primary/80 transition-colors">
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
		<Card
			class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden ring-1 ring-white/5"
		>
			<div
				class="absolute inset-0 bg-gradient-to-br from-destructive/5 via-transparent to-destructive/5 opacity-30"
			></div>
			<CardHeader class="relative z-10">
				<div class="flex items-center gap-2">
					<ShieldAlert class="w-5 h-5 text-destructive" />
					<CardTitle class="text-xl">Recent Revocations</CardTitle>
				</div>
				<CardDescription>Recently revoked API keys and audit information</CardDescription>
			</CardHeader>
			<CardContent class="relative z-10">
				{#if loadingRevocations}
					<div class="space-y-4">
						{#each Array(3) as _}
							<div class="rounded-lg border border-border/50 bg-card/50 p-4">
								<div class="h-4 bg-muted/50 rounded animate-pulse w-3/4 mb-2"></div>
								<div class="h-3 bg-muted/50 rounded animate-pulse w-1/2"></div>
							</div>
						{/each}
					</div>
				{:else if recentRevocations.length > 0}
					<div class="space-y-3">
						{#each recentRevocations as audit}
							<div
								class="rounded-lg border border-border/50 bg-card/30 p-4 hover:bg-accent/5 transition-colors group"
							>
								<div class="flex items-start justify-between gap-4">
									<div class="flex-1 min-w-0">
										<div class="flex items-center gap-2 mb-2">
											<span class="font-medium truncate">{audit.keyName}</span>
											<Badge
												class="font-mono text-xs group-hover:border-primary/30 transition-colors"
												variant="outline">{audit.keyPrefix}</Badge
											>
											<Badge class={getScopeBadgeClass(audit.scope)}>{audit.scope}</Badge>
										</div>
										<div class="text-sm text-muted-foreground space-y-1">
											<div>
												Owner: {audit.ownerName}
												{#if audit.ownerEmail}
													<span class="text-xs">({audit.ownerEmail})</span>
												{/if}
											</div>
											<div class="flex items-center gap-4 flex-wrap">
												<span>
													Revoked by: {audit.revokedBy?.firstName} {audit.revokedBy?.lastName}
													{#if audit.revokedBy?.email}
														<span class="text-xs">({audit.revokedBy.email})</span>
													{/if}
												</span>
												<span>•</span>
												<span>{formatDate(audit.revokedAt ?? '')}</span>
												<span>•</span>
												<span>{formatNumber(audit.totalRequestsAtRevocation)} lifetime requests</span>
											</div>
										</div>
									</div>
								</div>
							</div>
						{/each}
					</div>
				{:else}
					<div class="text-center py-12">
						<div class="inline-flex p-4 rounded-full bg-muted/50 mb-4">
							<ShieldAlert class="h-12 w-12 text-muted-foreground/50" />
						</div>
						<p class="text-muted-foreground font-medium">No recent revocations</p>
						<p class="text-sm text-muted-foreground/70 mt-1">
							Revoked API keys will appear here
						</p>
					</div>
				{/if}
			</CardContent>
		</Card>
	</div>
</main>

<!-- Revoke Confirmation Dialog -->
<AlertDialog.Root open={showRevokeDialog} onOpenChange={(open) => !open && closeRevokeDialog()}>
	<AlertDialog.Content class="bg-background border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title class="flex items-center gap-2">
				<ShieldAlert class="h-5 w-5 text-destructive" />
				Revoke API Key
			</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to revoke this API key? This action cannot be undone.
			</AlertDialog.Description>
		</AlertDialog.Header>

		{#if keyToRevoke}
			<div class="my-4 space-y-3 rounded-lg border border-border/50 bg-card/50 p-4">
				<div>
					<span class="text-sm text-muted-foreground">Key:</span>
					<div class="font-mono text-sm mt-1">
						{keyToRevoke.maskedKey}
					</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Name:</span>
					<div class="font-medium mt-1">{keyToRevoke.name}</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Owner:</span>
					<div class="mt-1">{keyToRevoke.owner?.name}</div>
				</div>
				<div>
					<span class="text-sm text-muted-foreground">Total Requests:</span>
					<div class="font-medium mt-1">{formatNumber(keyToRevoke.totalRequests)}</div>
				</div>
			</div>

			<Alert variant="destructive" class="bg-destructive/10 border-destructive/50">
				<AlertTriangle class="h-4 w-4" />
				<AlertDescription>
					Any systems using this key will immediately lose access.
				</AlertDescription>
			</Alert>
		{/if}

		<AlertDialog.Footer>
			<AlertDialog.Cancel disabled={revoking}>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={handleRevoke}
				disabled={revoking}
				class="bg-destructive hover:bg-destructive/90"
			>
				{#if revoking}
					Revoking...
				{:else}
					Revoke Key
				{/if}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
