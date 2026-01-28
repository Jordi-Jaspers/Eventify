<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
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
	import { searchCurrentMembers } from '$lib/api/organization/OrganizationMembershipController';
	import { createMemberManagementService } from '$lib/api/organization/service/MemberManagementService.svelte';

	// Reactive orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	// DataTable service - will be recreated when orgId changes
	let dataTableService: DataTableService<OrganizationMembershipResponse> | undefined = $state(undefined);
	let memberService: ReturnType<typeof createMemberManagementService> | undefined = $state(undefined);
	let lastLoadedOrgId: number = $state(0);

	// Recreate services when orgId changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			dataTableService = createDataTableService<OrganizationMembershipResponse>({
				fetchFn: (input: SortablePageInput): Promise<PageResource<OrganizationMembershipResponse>> =>
					searchCurrentMembers(currentOrgId, input),
				pageSize: 10,
				defaultSort: [{ name: 'email', direction: 'ASC' }]
			});
			memberService = createMemberManagementService(currentOrgId, () => {
				dataTableService?.load();
			});
			dataTableService.load();
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
			key: 'actions'
		}
	];

	// Derived permissions
	const currentUserRole: OrganizationalRole | null = $derived.by(() => {
		if (!dataTableService) return null;
		const userId: number | undefined = $currentUser?.id;
		if (!userId) return null;
		const member: OrganizationMembershipResponse | undefined = dataTableService.items.find(
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
		if (!dataTableService) return false;
		return dataTableService.items.some((m: OrganizationMembershipResponse) => m.role === 'OWNER');
	});

	// Sheet state
	let showAddSheet: boolean = $state(false);
	let showRemoveSheet: boolean = $state(false);
	let showTransferSheet: boolean = $state(false);

	// Sheet handlers
	function openAddSheet(): void {
		if (!memberService) return;
		memberService.resetAddState();
		showAddSheet = true;
	}

	function openRemoveSheet(member: OrganizationMembershipResponse): void {
		if (!memberService) return;
		memberService.setMemberToRemove(member);
		showRemoveSheet = true;
	}

	function openTransferSheet(member: OrganizationMembershipResponse): void {
		if (!memberService) return;
		memberService.setTransferTarget(member);
		memberService.setTransferConfirmation('');
		showTransferSheet = true;
	}

	function handleAddSheetOpenChange(open: boolean): void {
		showAddSheet = open;
		if (!open && memberService) memberService.resetAddState();
	}

	function handleRemoveSheetOpenChange(open: boolean): void {
		showRemoveSheet = open;
		if (!open && memberService) memberService.setMemberToRemove(null);
	}

	function handleTransferSheetOpenChange(open: boolean): void {
		showTransferSheet = open;
		if (!open && memberService) {
			memberService.setTransferTarget(null);
			memberService.setTransferConfirmation('');
		}
	}

	// Delegate handler for transfer
	function handleTransfer(): void {
		if (!memberService || !dataTableService) return;
		const currentOwner: OrganizationMembershipResponse | undefined = dataTableService.items.find(
			(m: OrganizationMembershipResponse) => m.role === 'OWNER'
		);
		if (!currentOwner) return;
		memberService.handleTransfer(currentOwner);
		showTransferSheet = false;
	}

</script>

<svelte:head>
	<title>Organization Members - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-8 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between">
			<div>
				<h1
					class="text-3xl font-bold text-primary"
				>
					Organization Members
				</h1>
				<p class="text-muted-foreground mt-2">Manage members and permissions</p>
			</div>

			{#if canManageMembers}
				<Button onclick={openAddSheet}>
					<UserPlus class="mr-2 h-4 w-4" />
					Add Member
				</Button>
			{/if}
		</div>

		<!-- DataTable -->
		{#if dataTableService && memberService}
			<DataTable {columns} service={dataTableService} title="Members" icon={Users}>
				{#snippet row(member: OrganizationMembershipResponse)}
					<div
						class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 hover:bg-muted/30 transition-all"
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
												class="bg-background/50 border-border/50 hover:bg-accent/10 transition-all"
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
											onclick={() => memberService?.handleUpdateRole(member, 'ADMIN')}
											class="hover:bg-accent/10"
										>
											<Shield class="mr-2 h-4 w-4" />
											ADMIN
										</DropdownMenu.Item>
										<DropdownMenu.Item
											onclick={() => memberService?.handleUpdateRole(member, 'MEMBER')}
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
						<div class="col-span-1 md:col-span-1 flex items-center justify-center">
							{#if canManageMembers && member.role !== 'OWNER'}
								<DropdownMenu.Root>
									<DropdownMenu.Trigger>
										{#snippet child({ props })}
											<Button
												{...props}
												variant="outline"
												size="sm"
												class="bg-background/50 border-border/50 hover:bg-accent/10 transition-all"
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
{#if memberService}
	<AddMemberSheet
		open={showAddSheet}
		searching={memberService.isSearching}
		adding={memberService.isAdding}
		searchQuery={memberService.searchQuery}
		searchResults={memberService.searchResults}
		selectedUser={memberService.selectedUser}
		selectedRole={memberService.selectedRole}
		showSearchDropdown={memberService.showSearchDropdown}
		{hasOwner}
		{isGlobalAdmin}
		debouncedQueryLength={memberService.debouncedQuery.length}
		onOpenChange={handleAddSheetOpenChange}
		onSearchQueryChange={memberService.setSearchQuery}
		onSelectUser={memberService.selectUser}
		onClearSelection={memberService.clearUserSelection}
		onRoleChange={memberService.setSelectedRole}
		onSubmit={memberService.handleAdd}
		onSearchFocus={() => { 
			if (memberService && memberService.searchQuery.length > 0) {
				memberService.setShowSearchDropdown(true);
			}
		}}
	/>

	<RemoveMemberSheet
		open={showRemoveSheet}
		member={memberService.memberToRemove}
		removing={memberService.isRemoving}
		onOpenChange={handleRemoveSheetOpenChange}
		onConfirm={memberService.handleRemove}
	/>

	<TransferOwnershipSheet
		open={showTransferSheet}
		member={memberService.transferTarget}
		confirmation={memberService.transferConfirmation}
		transferring={memberService.isTransferring}
		onOpenChange={handleTransferSheetOpenChange}
		onConfirmationChange={memberService.setTransferConfirmation}
		onConfirm={handleTransfer}
	/>
{/if}
