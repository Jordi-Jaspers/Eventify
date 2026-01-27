<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchOrganizations } from '$lib/api/organization/OrganizationController';
	import type { OrganizationResponse, OrganizationStatus } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Building2, Users, Key } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	const columns: DataTableColumn<OrganizationResponse>[] = [
		{
			key: 'name',
			label: 'Name',
			sortable: true,
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search by name...',
			colSpan: 2
		},
		{
			key: 'slug',
			label: 'Slug',
			colSpan: 2
		},
		{
			key: 'status',
			label: 'Status',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ value: 'TRIAL', label: 'Trial' },
				{ value: 'ACTIVE', label: 'Active' },
				{ value: 'SUSPENDED', label: 'Suspended' }
			]
		},
		{
			key: 'owner',
			label: 'Owner',
			colSpan: 2
		},
		{
			key: 'memberCount',
			label: 'Members',
			sortable: true
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'actions'
		}
	];

	// Service
	const service = createDataTableService<OrganizationResponse>({
		fetchFn: searchOrganizations,
		pageSize: 10,
		defaultSort: [{name: 'name', direction: 'ASC'}]
	});

	// Helper functions
	function getStatusBadgeVariant(
		status: OrganizationStatus | undefined
	): 'default' | 'success' | 'destructive' {
		switch (status) {
			case 'ACTIVE':
				return 'success';
			case 'SUSPENDED':
				return 'destructive';
			case 'TRIAL':
			default:
				return 'default';
		}
	}

	function getOwnerName(owner: { firstName?: string; lastName?: string } | undefined): string {
		if (!owner || (!owner.firstName && !owner.lastName)) {
			return 'No owner';
		}
		return `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim();
	}

	function formatDate(dateString: string | undefined): string {
		if (!dateString) return 'N/A';
		const date: Date = new Date(dateString);
		return date.toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		});
	}

	function navigateToMembers(orgId: number | undefined): void {
		if (orgId) {
			goto(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(orgId).path);
		}
	}

	function navigateToApiKeys(orgId: number | undefined): void {
		if (orgId) {
			goto(CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path);
		}
	}

	onMount(() => service.load());
</script>

<svelte:head>
	<title>Organizations - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1
				class="text-3xl font-bold text-primary"
			>
				Organizations
			</h1>
			<p class="text-muted-foreground mt-2">Manage and monitor all organizations on the platform</p>
		</div>

		<!-- DataTable -->
		<DataTable {columns} {service} title="All Organizations" icon={Building2}>
			{#snippet row(org: OrganizationResponse)}
				<div
					class="grid grid-cols-1 md:grid-cols-11 items-center gap-2 md:gap-4 p-4 hover:bg-muted/30 transition-all"
				>
					<!-- Name -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<div class="flex items-center gap-2">
							<Building2 class="h-4 w-4 text-primary md:hidden" />
							<div>
								<div class="font-medium">{org.name}</div>
								<div class="text-sm text-muted-foreground md:hidden">{org.slug}</div>
							</div>
						</div>
					</div>

					<!-- Slug (desktop only) -->
					<div class="hidden md:flex md:col-span-2 items-center">
						<span class="text-sm text-muted-foreground">{org.slug}</span>
					</div>

					<!-- Status -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<Badge variant={getStatusBadgeVariant(org.status)} class="min-w-[90px] justify-center">
							{org.status}
						</Badge>
					</div>

					<!-- Owner -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm {org.owner ? '' : 'text-muted-foreground italic'}">
							<span class="md:hidden font-medium">Owner: </span>
							{getOwnerName(org.owner)}
						</span>
					</div>

					<!-- Members -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<span class="text-sm">
							<span class="md:hidden text-muted-foreground">Members: </span>
							{org.memberCount}
						</span>
					</div>

					<!-- Created -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm text-muted-foreground">
							<span class="md:hidden">Created: </span>
							{formatDate(org.createdAt)}
						</span>
					</div>

					<!-- Actions -->
					<div class="col-span-1 md:col-span-1 flex items-center justify-center gap-1">
						<Button
							variant="ghost"
							size="sm"
							onclick={() => navigateToApiKeys(org.id)}
							class="gap-1 text-primary hover:text-primary hover:bg-primary/10 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 transition-all"
							title="Manage API Keys"
							aria-label="Manage API Keys for {org.name}"
						>
							<Key class="h-4 w-4" />
							<span class="md:hidden">API Keys</span>
						</Button>
						<Button
							variant="ghost"
							size="sm"
							onclick={() => navigateToMembers(org.id)}
							class="gap-1 text-primary hover:text-primary hover:bg-primary/10 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 transition-all"
							title="Manage Members"
							aria-label="Manage Members for {org.name}"
						>
							<Users class="h-4 w-4" />
							<span class="md:hidden">Members</span>
						</Button>
					</div>
				</div>
			{/snippet}
		</DataTable>
	</div>
</main>
