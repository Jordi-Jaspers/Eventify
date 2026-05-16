<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchOrganizations } from '$lib/api/admin/AdminOrganizationController';
	import type { OrganizationResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Building2, Users, Key, MoreVertical, Pencil } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { formatDate } from '$lib/utils/date';
	import {
		getOrganizationStatusBadgeVariant,
		getOwnerDisplayName
	} from '$lib/utils/organization';
	import CreateOrganizationSheet from '$lib/components/admin/CreateOrganizationSheet.svelte';
	import { EditOrganizationSheet } from '$lib/components/admin';

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
				{ value: 'ACTIVE', label: 'Active' },
				{ value: 'SUSPENDED', label: 'Suspended' }
			],
			colSpan: 2
		},
		{
			key: 'owner',
			label: 'Owner',
			colSpan: 2
		},
		{
			key: 'memberCount',
			label: 'Members',
			sortable: true,
			colSpan: 1
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 1
		},
		{
			key: 'actions',
			colSpan: 1
		}
	];

	const service = createDataTableService<OrganizationResponse>({
		fetchFn: searchOrganizations,
		pageSize: 10,
		defaultSort: [{ name: 'name', direction: 'ASC' }]
	});

	let isCreateSheetOpen: boolean = $state(false);
	let isEditSheetOpen: boolean = $state(false);
	let selectedOrganization: OrganizationResponse | null = $state(null);

	function navigateToMembers(orgId: number | undefined): void {
		if (orgId) goto(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(orgId).path);
	}

	function navigateToApiKeys(orgId: number | undefined): void {
		if (orgId) goto(CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path);
	}

	function handleOrganizationSaved(): void {
		service.load();
	}

	function openEditSheet(org: OrganizationResponse): void {
		selectedOrganization = org;
		isEditSheetOpen = true;
	}

	onMount(() => service.load());
</script>

<svelte:head>
	<title>Organizations - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8 flex items-center justify-between">
			<div>
				<h1 class="text-3xl font-bold text-primary">
					Organizations
				</h1>
				<p class="text-muted-foreground mt-2">Manage and monitor all organizations on the platform</p>
			</div>
			<Button onclick={() => (isCreateSheetOpen = true)}>
				<Building2 class="mr-2 h-4 w-4" />
				New Organization
			</Button>
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
				<div class="col-span-1 md:col-span-2 flex items-center">
					<Badge variant={getOrganizationStatusBadgeVariant(org.status)} class="min-w-[90px] justify-center">
						{org.status}
					</Badge>
				</div>

				<!-- Owner -->
				<div class="col-span-1 md:col-span-2 flex items-center">
					<span class="text-sm {org.owner ? '' : 'text-muted-foreground italic'}">
						<span class="md:hidden font-medium">Owner: </span>
						{getOwnerDisplayName(org.owner)}
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
					<div class="col-span-1 md:col-span-1 flex items-center">
						<span class="text-sm text-muted-foreground">
							<span class="md:hidden">Created: </span>
							{org.createdAt ? formatDate(org.createdAt) : 'N/A'}
						</span>
					</div>

					<!-- Actions -->
					<div class="col-span-1 md:col-span-1 flex items-center justify-center" role="none" onclick={(e) => e.stopPropagation()}>
						<DropdownMenu.Root>
							<DropdownMenu.Trigger>
								<Button variant="ghost" size="icon" class="h-8 w-8">
									<MoreVertical class="h-4 w-4" />
									<span class="sr-only">Actions</span>
								</Button>
							</DropdownMenu.Trigger>
							<DropdownMenu.Content align="end" class="w-48 bg-card/95 backdrop-blur-xl border-border/50">
								<DropdownMenu.Item onclick={() => openEditSheet(org)} class="cursor-pointer">
									<Pencil class="mr-2 h-4 w-4" />
									Edit
								</DropdownMenu.Item>
								<DropdownMenu.Separator />
								<DropdownMenu.Item onclick={() => navigateToApiKeys(org.id)} class="cursor-pointer">
									<Key class="mr-2 h-4 w-4" />
									API Keys
								</DropdownMenu.Item>
								<DropdownMenu.Item onclick={() => navigateToMembers(org.id)} class="cursor-pointer">
									<Users class="mr-2 h-4 w-4" />
									Members
								</DropdownMenu.Item>
							</DropdownMenu.Content>
						</DropdownMenu.Root>
					</div>
				</div>
			{/snippet}
		</DataTable>

		<!-- Create Organization Sheet -->
		<CreateOrganizationSheet
			open={isCreateSheetOpen}
			onOpenChange={(open: boolean) => (isCreateSheetOpen = open)}
			onSuccess={handleOrganizationSaved}
		/>

		<!-- Edit Organization Sheet -->
		<EditOrganizationSheet
			open={isEditSheetOpen}
			onOpenChange={(open: boolean) => (isEditSheetOpen = open)}
			onSuccess={handleOrganizationSaved}
			organization={selectedOrganization}
		/>
	</div>
</main>
