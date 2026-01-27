<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Key, Plus, Trash2, Clock, Activity, Info } from '@lucide/svelte';
	import type { ApiKeyResponse, SortablePageInput, PageResource, UserOrganizationResponse, OrganizationResponse } from '$lib/api/models';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn, DataTableService } from '$lib/components/data-table/types';
	import { CreateApiKeySheet, ApiKeyCreatedModal, ApiKeyList } from '$lib/components/api-keys';
	import {
		searchOrganizationApiKeys,
		createOrganizationApiKey,
		revokeOrganizationApiKey
	} from '$lib/api/organization/OrganizationApiKeyController';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { formatRelativeDate } from '$lib/utils/date';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
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
		const needsFetch: boolean = isGlobalAdmin && !organizationFromStore && currentOrgId > 0 && currentOrgId !== lastFetchedOrgId;
		
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
	let service: DataTableService<ApiKeyResponse> | undefined = $state(undefined);
	let lastLoadedOrgId: number = $state(0);

	// Recreate service when orgId changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			service = createDataTableService<ApiKeyResponse>({
				fetchFn: (input: SortablePageInput): Promise<PageResource<ApiKeyResponse>> =>
					searchOrganizationApiKeys(currentOrgId, input),
				pageSize: 10,
				defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
			});
			service.load();
			lastLoadedOrgId = currentOrgId;
		}
	});

	// Columns configuration
	const columns: DataTableColumn<ApiKeyResponse>[] = [
		{
			key: 'search',
			label: 'Search',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search by name...',
			colSpan: 0
		},
		{
			key: 'name',
			label: 'Name',
			sortable: true,
			colSpan: 4
		},
		{
			key: 'createdBy',
			label: 'Created By',
			colSpan: 2
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'expiresAt',
			label: 'Expires',
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
			key: 'actions',
			colSpan: 1
		}
	];

	// Sheet state
	let showCreateSheet: boolean = $state(false);
	let isCreating: boolean = $state(false);
	let createdKey: { id: number; name: string; key: string; suffix: string; createdAt: string; expiresAt?: string; createdBy?: any } | null = $state(null);

	// Revoke state
	let keyToRevoke: ApiKeyResponse | null = $state(null);
	let showRevokeDialog: boolean = $state(false);
	let isRevoking: boolean = $state(false);

	// Create handler
	async function handleCreate(name: string, expiresAt: string | undefined): Promise<void> {
		isCreating = true;
		try {
			const response = await createOrganizationApiKey(orgId, { name, expiresAt });
			toast.success(`API key "${name}" created successfully`);
			createdKey = {
				id: response.id ?? 0,
				name: response.name ?? name,
				key: response.key ?? '',
				suffix: response.suffix ?? '',
				createdAt: response.createdAt ?? new Date().toISOString(),
				expiresAt: response.expiresAt,
				createdBy: response.createdBy
			};
			showCreateSheet = false;
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to create API key');
			toast.error(message);
		} finally {
			isCreating = false;
		}
	}

	// Revoke handler
	async function handleRevoke(): Promise<void> {
		if (!keyToRevoke || !keyToRevoke.id) return;
		isRevoking = true;
		try {
			await revokeOrganizationApiKey(orgId, keyToRevoke.id);
			toast.success(`API key "${keyToRevoke.name ?? 'Unnamed'}" revoked successfully`);
			keyToRevoke = null;
			showRevokeDialog = false;
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to revoke API key');
			toast.error(message);
		} finally {
			isRevoking = false;
		}
	}

	function openRevokeDialog(key: ApiKeyResponse): void {
		keyToRevoke = key;
		showRevokeDialog = true;
	}

	function closeCreatedModal(): void {
		createdKey = null;
	}
</script>

<svelte:head>
	<title>API Keys - {orgName} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
		<!-- Header Card -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
			<!-- Gradient overlay -->
			<div
				class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"
			></div>

			<CardHeader class="relative z-10">
				<div class="flex items-start justify-between gap-4">
					<div>
						<CardTitle class="text-2xl flex items-center gap-2">
							<Key class="w-6 h-6 text-primary" />
							API Keys
						</CardTitle>
						<CardDescription>Manage API keys for programmatic access to {orgName}</CardDescription>
					</div>
					{#if canManage}
						<Button onclick={() => (showCreateSheet = true)}>
							<Plus class="mr-2 h-4 w-4" />
							Create API Key
						</Button>
					{/if}
				</div>
			</CardHeader>

			<CardContent class="space-y-6 relative z-10">
				<!-- DataTable or Empty State -->
				{#if service}
					{#if service.items.length === 0 && !service.loading}
						<!-- Empty State -->
						<div
							class="flex flex-col items-center justify-center py-12 px-6 text-center border-2 border-dashed border-border/50 rounded-lg bg-background/30"
						>
							<Key class="w-16 h-16 text-muted-foreground/30 mb-4" />
							<h3 class="text-lg font-semibold mb-2">No API Keys Yet</h3>
							<p class="text-sm text-muted-foreground max-w-md mb-4">
								API keys allow you to authenticate requests to the Eventify API. Create your first key
								to start sending events programmatically.
							</p>
							{#if canManage}
								<Button onclick={() => (showCreateSheet = true)}>
									<Plus class="mr-2 h-4 w-4" />
									Create Key
								</Button>
							{/if}
						</div>
					{:else if service.loading}
						<div class="flex items-center justify-center py-12">
							<div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
						</div>
					{:else}
						<DataTable {columns} {service} title="API Keys" icon={Key}>
							{#snippet row(apiKey: ApiKeyResponse)}
								<div
									class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 hover:bg-muted/30 transition-all"
								>
									<!-- Name & Masked Key -->
									<div class="col-span-1 md:col-span-4 flex flex-col gap-1">
										<p class="font-medium">{apiKey.name ?? 'Unnamed'}</p>
										<code class="text-xs text-muted-foreground font-mono">{apiKey.maskedKey ?? ''}</code>
									</div>

									<!-- Created By -->
									<div class="col-span-1 md:col-span-2 flex items-center">
										<p class="text-sm text-muted-foreground">
											{#if apiKey.createdBy}
												{apiKey.createdBy.firstName ?? ''} {apiKey.createdBy.lastName ?? ''}
											{:else}
												N/A
											{/if}
										</p>
									</div>

									<!-- Created At -->
									<div class="col-span-1 md:col-span-2 flex items-center gap-1">
										<Clock class="w-3 h-3 text-muted-foreground" />
										<p class="text-sm text-muted-foreground">
											{formatRelativeDate(apiKey.createdAt ?? '')}
										</p>
									</div>

									<!-- Expires At -->
									<div class="col-span-1 md:col-span-2 flex items-center">
										<p class="text-sm text-muted-foreground">
											{#if apiKey.expiresAt}
												{formatRelativeDate(apiKey.expiresAt)}
											{:else}
												<span class="text-green-500">Never</span>
											{/if}
										</p>
									</div>

									<!-- Last Used -->
									<div class="hidden md:flex md:col-span-1 items-center">
										<div class="flex flex-col gap-0.5">
											<div class="flex items-center gap-1">
												<Activity class="w-3 h-3 text-muted-foreground" />
												<p class="text-xs text-muted-foreground">
													{#if apiKey.lastUsedAt}
														{formatRelativeDate(apiKey.lastUsedAt)}
													{:else}
														Never
													{/if}
												</p>
											</div>
											<p class="text-xs text-muted-foreground">
												{apiKey.totalRequests ?? 0} req
											</p>
										</div>
									</div>

									<!-- Actions -->
									<div class="col-span-1 md:col-span-1 flex items-center justify-center">
										{#if canManage}
											<Button
												variant="ghost"
												size="icon"
												class="h-8 w-8 text-muted-foreground hover:text-destructive"
												onclick={() => openRevokeDialog(apiKey)}
												aria-label="Revoke API key"
											>
												<Trash2 class="h-4 w-4" />
											</Button>
										{/if}
									</div>
								</div>
							{/snippet}
						</DataTable>

						<!-- Key count indicator -->
						<div class="flex items-center gap-2 px-1">
							<Info class="w-3.5 h-3.5 text-muted-foreground/60" />
							<span class="text-xs text-muted-foreground">
								{service.items.length} API key{service.items.length === 1 ? '' : 's'}
							</span>
						</div>
					{/if}
				{/if}
			</CardContent>
		</Card>
	</div>
</main>

<!-- Create Sheet -->
<CreateApiKeySheet
	open={showCreateSheet}
	creating={isCreating}
	onOpenChange={(o) => (showCreateSheet = o)}
	onSubmit={handleCreate}
/>

<!-- Created Modal -->
<ApiKeyCreatedModal open={createdKey !== null} apiKey={createdKey} onClose={closeCreatedModal} />

<!-- Revoke Dialog -->
<AlertDialog.Root open={showRevokeDialog} onOpenChange={(o) => (showRevokeDialog = o)}>
	<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<AlertDialog.Header>
			<AlertDialog.Title>Revoke API Key</AlertDialog.Title>
			<AlertDialog.Description>
				Are you sure you want to revoke the API key "{keyToRevoke?.name ?? 'Unnamed'}"? This action
				cannot be undone and any applications using this key will lose access immediately.
			</AlertDialog.Description>
		</AlertDialog.Header>
		<AlertDialog.Footer>
			<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
			<AlertDialog.Action
				onclick={handleRevoke}
				disabled={isRevoking}
				class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
			>
				{isRevoking ? 'Revoking...' : 'Revoke Key'}
			</AlertDialog.Action>
		</AlertDialog.Footer>
	</AlertDialog.Content>
</AlertDialog.Root>
