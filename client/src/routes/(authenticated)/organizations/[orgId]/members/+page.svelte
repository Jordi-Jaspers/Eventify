<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { onMount } from 'svelte';
	import Button from '$lib/components/ui/button/button.svelte';
	import { UserPlus, Shield, Crown, Trash2, MoreVertical, ChevronDown, User as UserIcon, Users } from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole, SortablePageInput, PageResource } from '$lib/api/models';
	import { currentUser } from '$lib/stores/auth';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn, DataTableService } from '$lib/components/data-table/types';
	import { Badge } from '$lib/components/ui/badge';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { getInitials } from '$lib/utils/string';
	import { formatRelativeDate } from '$lib/utils/date';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import {
		AddMemberSheet,
		RemoveMemberSheet,
		TransferOwnershipSheet,
		RoleBadge
	} from '$lib/components/members';
	import {
		searchCurrentMembers,
		searchUsersToAdd,
		addMember,
		updateMemberRole,
		removeMember,
		transferOwnership
	} from '$lib/api/organization/OrganizationMembershipController';
	import { assignOrganizationOwner } from '$lib/api/admin/AdminController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import type { UserResponse } from '$lib/api/models';

	// Reactive orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	// DataTable service - will be recreated when orgId changes
	let service: DataTableService<OrganizationMembershipResponse> | undefined = $state(undefined);
	let lastLoadedOrgId: number = $state(0);

	// Recreate service when orgId changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			service = createDataTableService<OrganizationMembershipResponse>({
				fetchFn: (input: SortablePageInput): Promise<PageResource<OrganizationMembershipResponse>> =>
					searchCurrentMembers(currentOrgId, input),
				pageSize: 10,
				defaultSort: [{ name: 'email', direction: 'ASC' }]
			});
			service.load();
			lastLoadedOrgId = currentOrgId;
		}
	});

	// Columns configuration
	const columns: DataTableColumn<OrganizationMembershipResponse>[] = [
		{
			key: 'search',
			label: 'Search',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search members...',
			colSpan: 0
		},
		{
			key: 'member',
			label: 'Member',
			colSpan: 4
		},
		{
			key: 'email',
			label: 'Email',
			sortable: true,
			colSpan: 3
		},
		{
			key: 'role',
			label: 'Role',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ value: 'OWNER', label: 'Owner' },
				{ value: 'ADMIN', label: 'Admin' },
				{ value: 'MEMBER', label: 'Member' }
			],
			colSpan: 2
		},
		{
			key: 'joinedAt',
			label: 'Joined',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'actions',
			label: 'Actions',
			colSpan: 1
		}
	];

	// Derived permissions
	const currentUserRole: OrganizationalRole | null = $derived.by(() => {
		if (!service) return null;
		const userId: number | undefined = $currentUser?.id;
		if (!userId) return null;
		const member: OrganizationMembershipResponse | undefined = service.items.find(
			(m: OrganizationMembershipResponse) => m.userId === userId
		);
		return member?.role ?? null;
	});
	const isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	const canManageMembers: boolean = $derived(
		isGlobalAdmin || currentUserRole === 'OWNER' || currentUserRole === 'ADMIN'
	);
	const isOwner: boolean = $derived(isGlobalAdmin || currentUserRole === 'OWNER');
	const hasOwner: boolean = $derived.by(() => {
		if (!service) return false;
		return service.items.some((m: OrganizationMembershipResponse) => m.role === 'OWNER');
	});

	// Sheet state
	let showAddSheet: boolean = $state(false);
	let showRemoveSheet: boolean = $state(false);
	let showTransferSheet: boolean = $state(false);

	// Add member state
	let searchQuery: string = $state('');
	let debouncedQuery: string = $state('');
	let isSearching: boolean = $state(false);
	let searchResults: UserResponse[] = $state([]);
	let showSearchDropdown: boolean = $state(false);
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;
	let selectedUser: UserResponse | null = $state(null);
	let selectedRole: OrganizationalRole = $state('MEMBER');
	let isAdding: boolean = $state(false);

	// Remove member state
	let memberToRemove: OrganizationMembershipResponse | null = $state(null);
	let isRemoving: boolean = $state(false);

	// Transfer state
	let transferTarget: OrganizationMembershipResponse | null = $state(null);
	let transferConfirmation: string = $state('');
	let isTransferring: boolean = $state(false);

	// Search functions
	async function performSearch(query: string): Promise<void> {
		isSearching = true;
		showSearchDropdown = true;
		try {
			searchResults = await searchUsersToAdd(orgId, query);
		} catch (err: unknown) {
			console.error('Search error:', err);
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	function setSearchQuery(query: string): void {
		searchQuery = query;
		if (debounceTimer) {
			clearTimeout(debounceTimer);
			debounceTimer = null;
		}
		if (query.length === 0) {
			debouncedQuery = '';
			searchResults = [];
			showSearchDropdown = false;
		} else if (query.length >= 3) {
			showSearchDropdown = true;
			debounceTimer = setTimeout(() => {
				debouncedQuery = query;
				performSearch(query);
			}, 300);
		} else {
			showSearchDropdown = true;
			debouncedQuery = '';
			searchResults = [];
		}
	}

	function selectUser(user: UserResponse): void {
		selectedUser = user;
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	function clearUserSelection(): void {
		selectedUser = null;
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	function resetAddState(): void {
		selectedUser = null;
		selectedRole = 'MEMBER';
		searchQuery = '';
		debouncedQuery = '';
		searchResults = [];
		showSearchDropdown = false;
		if (debounceTimer) {
			clearTimeout(debounceTimer);
			debounceTimer = null;
		}
	}

	// Mutation handlers
	async function handleAdd(): Promise<void> {
		if (!selectedUser?.email || !selectedRole) {
			toast.error('Please select a user and role');
			return;
		}
		isAdding = true;
		try {
			if (selectedRole === 'OWNER') {
				await assignOrganizationOwner(orgId, { email: selectedUser.email });
			} else {
				await addMember(orgId, { email: selectedUser.email, role: selectedRole });
			}
			toast.success(`${selectedUser.email} added successfully`);
			resetAddState();
			showAddSheet = false;
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to add member');
			toast.error(message);
		} finally {
			isAdding = false;
		}
	}

	async function handleUpdateRole(member: OrganizationMembershipResponse, newRole: OrganizationalRole): Promise<void> {
		if (member.role === newRole || member.role === 'OWNER') return;
		if (!member.userId) return;
		try {
			await updateMemberRole(orgId, member.userId, { role: newRole });
			toast.success(`Role updated to ${newRole}`);
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to update role');
			toast.error(message);
		}
	}

	async function handleRemove(): Promise<void> {
		if (!memberToRemove || !memberToRemove.userId) return;
		isRemoving = true;
		try {
			await removeMember(orgId, memberToRemove.userId);
			toast.success(`${memberToRemove.userEmail ?? 'Member'} removed successfully`);
			memberToRemove = null;
			showRemoveSheet = false;
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to remove member');
			toast.error(message);
		} finally {
			isRemoving = false;
		}
	}

	async function handleTransfer(): Promise<void> {
		if (!transferTarget || transferConfirmation !== 'Transfer Ownership') {
			toast.error('Please type "Transfer Ownership" to confirm');
			return;
		}
		const currentOwner: OrganizationMembershipResponse | undefined = service?.items.find(
			(m: OrganizationMembershipResponse) => m.role === 'OWNER'
		);
		if (!currentOwner || !currentOwner.userId || !transferTarget.userId) {
			toast.error('Could not find current owner');
			return;
		}
		isTransferring = true;
		try {
			await transferOwnership(orgId, {
				currentOwnerUserId: currentOwner.userId,
				newOwnerUserId: transferTarget.userId
			});
			toast.success(`Ownership transferred to ${transferTarget.userEmail ?? 'new owner'}`);
			transferTarget = null;
			transferConfirmation = '';
			showTransferSheet = false;
			service?.load();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to transfer ownership');
			toast.error(message);
		} finally {
			isTransferring = false;
		}
	}

	// Sheet handlers
	function openAddSheet(): void {
		resetAddState();
		showAddSheet = true;
	}

	function openRemoveSheet(member: OrganizationMembershipResponse): void {
		memberToRemove = member;
		showRemoveSheet = true;
	}

	function openTransferSheet(member: OrganizationMembershipResponse): void {
		transferTarget = member;
		transferConfirmation = '';
		showTransferSheet = true;
	}

	function handleAddSheetOpenChange(open: boolean): void {
		showAddSheet = open;
		if (!open) resetAddState();
	}

	function handleRemoveSheetOpenChange(open: boolean): void {
		showRemoveSheet = open;
		if (!open) memberToRemove = null;
	}

	function handleTransferSheetOpenChange(open: boolean): void {
		showTransferSheet = open;
		if (!open) {
			transferTarget = null;
			transferConfirmation = '';
		}
	}
</script>

<svelte:head>
	<title>Organization Members - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between mb-8">
			<div>
				<h1
					class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent"
				>
					Organization Members
				</h1>
				<p class="text-muted-foreground mt-2">Manage members and permissions</p>
			</div>

			{#if canManageMembers}
				<Button
					onclick={openAddSheet}
					class="bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg hover:shadow-primary/50"
				>
					<UserPlus class="mr-2 h-4 w-4" />
					Add Member
				</Button>
			{/if}
		</div>

		<!-- DataTable -->
		{#if service}
			<DataTable {columns} {service} title="Members" icon={Users}>
				{#snippet row(member: OrganizationMembershipResponse)}
					<div
						class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
					>
						<!-- Avatar & Name -->
						<div class="col-span-1 md:col-span-4 flex items-center gap-3">
							<div
								class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
							>
								<span class="text-sm font-medium text-primary">
									{getInitials(member.userFirstName ?? '', member.userLastName ?? '')}
								</span>
							</div>
							<div class="min-w-0">
								<p class="font-medium truncate">
									{member.userFirstName ?? ''}
									{member.userLastName ?? ''}
								</p>
								<p class="text-sm text-muted-foreground md:hidden truncate">
									{member.userEmail ?? ''}
								</p>
							</div>
						</div>

						<!-- Email (desktop only) -->
						<div class="hidden md:flex md:col-span-3 items-center">
							<p class="text-sm text-muted-foreground truncate">{member.userEmail ?? ''}</p>
						</div>

						<!-- Role Badge/Selector -->
						<div class="col-span-1 md:col-span-2 flex items-center">
							{#if member.role === 'OWNER'}
								<RoleBadge role={member.role} />
							{:else if canManageMembers && member.role}
								<DropdownMenu.Root>
									<DropdownMenu.Trigger>
										{#snippet child({ props })}
											<Button
												{...props}
												variant="outline"
												size="sm"
												class="bg-background/50 border-border/50 hover:bg-accent/10"
											>
											<Badge class={getOrganizationalRoleBadgeClass(member.role ?? 'MEMBER')}>
												{#if member.role === 'ADMIN'}
													<Shield class="mr-1 h-3 w-3" />
												{/if}
												{member.role}
											</Badge>
												<ChevronDown class="ml-1 h-3 w-3" />
											</Button>
										{/snippet}
									</DropdownMenu.Trigger>
									<DropdownMenu.Content class="bg-card/95 backdrop-blur-xl border-border/50">
										<DropdownMenu.Item
											onclick={() => handleUpdateRole(member, 'ADMIN')}
											class="hover:bg-accent/10"
										>
											<Shield class="mr-2 h-4 w-4" />
											ADMIN
										</DropdownMenu.Item>
										<DropdownMenu.Item
											onclick={() => handleUpdateRole(member, 'MEMBER')}
											class="hover:bg-accent/10"
										>
											<UserIcon class="mr-2 h-4 w-4" />
											MEMBER
										</DropdownMenu.Item>
									</DropdownMenu.Content>
								</DropdownMenu.Root>
							{:else if member.role}
								<RoleBadge role={member.role} />
							{/if}
						</div>

						<!-- Joined Date -->
						<div class="col-span-1 md:col-span-2 flex items-center">
							<p class="text-sm text-muted-foreground">
								{formatRelativeDate(member.joinedAt ?? '')}
							</p>
						</div>

						<!-- Actions -->
						<div class="col-span-1 md:col-span-1 flex items-center justify-end">
							{#if canManageMembers && member.role !== 'OWNER'}
								<DropdownMenu.Root>
									<DropdownMenu.Trigger>
										{#snippet child({ props })}
											<Button
												{...props}
												variant="outline"
												size="sm"
												class="bg-background/50 border-border/50 hover:bg-accent/10"
											>
												<MoreVertical class="h-4 w-4" />
											</Button>
										{/snippet}
									</DropdownMenu.Trigger>
									<DropdownMenu.Content class="bg-card/95 backdrop-blur-xl border-border/50">
										{#if isOwner}
											<DropdownMenu.Item
												onclick={() => openTransferSheet(member)}
												class="hover:bg-accent/10"
											>
												<Crown class="mr-2 h-4 w-4 text-primary" />
												Transfer Ownership
											</DropdownMenu.Item>
										{/if}
										<DropdownMenu.Item
											onclick={() => openRemoveSheet(member)}
											class="hover:bg-destructive/10 text-destructive"
										>
											<Trash2 class="mr-2 h-4 w-4" />
											Remove
										</DropdownMenu.Item>
									</DropdownMenu.Content>
								</DropdownMenu.Root>
							{/if}
						</div>
					</div>
				{/snippet}
			</DataTable>
		{/if}
	</div>
</main>

<!-- Sheets -->
<AddMemberSheet
	open={showAddSheet}
	searching={isSearching}
	adding={isAdding}
	{searchQuery}
	{searchResults}
	{selectedUser}
	{selectedRole}
	{showSearchDropdown}
	{hasOwner}
	{isGlobalAdmin}
	debouncedQueryLength={debouncedQuery.length}
	onOpenChange={handleAddSheetOpenChange}
	onSearchQueryChange={setSearchQuery}
	onSelectUser={selectUser}
	onClearSelection={clearUserSelection}
	onRoleChange={(role) => selectedRole = role}
	onSubmit={handleAdd}
	onSearchFocus={() => { if (searchQuery.length > 0) showSearchDropdown = true; }}
/>

<RemoveMemberSheet
	open={showRemoveSheet}
	member={memberToRemove}
	removing={isRemoving}
	onOpenChange={handleRemoveSheetOpenChange}
	onConfirm={handleRemove}
/>

<TransferOwnershipSheet
	open={showTransferSheet}
	member={transferTarget}
	confirmation={transferConfirmation}
	transferring={isTransferring}
	onOpenChange={handleTransferSheetOpenChange}
	onConfirmationChange={(value) => transferConfirmation = value}
	onConfirm={handleTransfer}
/>
