<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import Button from '$lib/components/ui/button/button.svelte';
	import { CircleAlert, UserPlus, RefreshCw } from '@lucide/svelte';
	import type { OrganizationMembershipResponse, OrganizationalRole } from '$lib/api/models';
	import { currentUser } from '$lib/stores/auth';
	import { createMembershipService, type MembershipService } from '$lib/api/organization/OrganizationMembershipService.svelte';
	import {
		MemberList,
		AddMemberSheet,
		RemoveMemberSheet,
		TransferOwnershipSheet
	} from '$lib/components/members';

	// Reactive orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	// Track last loaded orgId to detect changes
	let lastLoadedOrgId: number = $state(0);

	// Service instance - use undefined for uninitialized state
	let service = $state<MembershipService | undefined>(undefined);

	// Reload members when orgId changes (client-side only)
	$effect(() => {
		if (!browser) return;
		
		const currentOrgId: number = orgId;
		
		// Only reload if orgId actually changed
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0) {
			service = createMembershipService(currentOrgId);
			service.loadMembers();
			lastLoadedOrgId = currentOrgId;
		}
	});

	// Sheet visibility state (UI-only, not business logic)
	let showAddSheet: boolean = $state(false);
	let showRemoveSheet: boolean = $state(false);
	let showTransferSheet: boolean = $state(false);

	// Derived permissions - handle undefined service during SSR
	const currentUserRole: OrganizationalRole | null = $derived.by(() => {
		if (!service) return null;
		const userId = $currentUser?.id;
		if (!userId) return null;
		const member = service.members.find((m: OrganizationMembershipResponse) => m.userId === userId);
		return member?.role ?? null;
	});
	const isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	const canManageMembers: boolean = $derived(
		isGlobalAdmin || currentUserRole === 'OWNER' || currentUserRole === 'ADMIN'
	);
	const isOwner: boolean = $derived(isGlobalAdmin || currentUserRole === 'OWNER');
	const hasOwner: boolean = $derived(
		service?.members.some((m: OrganizationMembershipResponse) => m.role === 'OWNER') ?? false
	);

	// Sheet handlers
	function openAddSheet(): void {
		if (!service) return;
		service.resetAddState();
		showAddSheet = true;
	}

	async function handleAdd(): Promise<void> {
		if (!service) return;
		const success: boolean = await service.add();
		if (success) showAddSheet = false;
	}

	function openRemoveSheet(member: OrganizationMembershipResponse): void {
		if (!service) return;
		service.setMemberToRemove(member);
		showRemoveSheet = true;
	}

	async function handleRemove(): Promise<void> {
		if (!service) return;
		const success: boolean = await service.remove();
		if (success) showRemoveSheet = false;
	}

	function openTransferSheet(member: OrganizationMembershipResponse): void {
		if (!service) return;
		service.setTransferTarget(member);
		showTransferSheet = true;
	}

	async function handleTransfer(): Promise<void> {
		if (!service) return;
		const success: boolean = await service.transfer();
		if (success) showTransferSheet = false;
	}

	function handleRoleChange(role: OrganizationalRole): void {
		if (service) {
			service.selectedRole = role;
		}
	}

	function handleRemoveSheetOpenChange(open: boolean): void {
		showRemoveSheet = open;
		if (!open && service) {
			service.setMemberToRemove(null);
		}
	}

	function handleTransferSheetOpenChange(open: boolean): void {
		showTransferSheet = open;
		if (!open && service) {
			service.resetTransferState();
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

		<!-- Error Alert -->
		{#if service?.error && !service?.loading}
			<Alert
				variant="destructive"
				class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
			>
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{service.error}
					<Button variant="outline" size="sm" class="ml-4" onclick={service.loadMembers}>
						<RefreshCw class="h-4 w-4" />
					</Button>
				</AlertDescription>
			</Alert>
		{/if}

		<!-- Members List -->
		<MemberList
			members={service?.members ?? []}
			loading={service?.loading ?? true}
			{canManageMembers}
			{isOwner}
			onUpdateRole={service?.updateRole ?? (() => {})}
			onRemove={openRemoveSheet}
			onTransferOwnership={openTransferSheet}
		/>
	</div>
</main>

<!-- Add Member Sheet -->
{#if service}
	<AddMemberSheet
		open={showAddSheet}
		searching={service.isSearching}
		adding={service.isAdding}
		searchQuery={service.searchQuery}
		searchResults={service.searchResults}
		selectedUser={service.selectedUser}
		selectedRole={service.selectedRole}
		showSearchDropdown={service.showSearchDropdown}
		{hasOwner}
		{isGlobalAdmin}
		debouncedQueryLength={service.debouncedQueryLength}
		onOpenChange={(open) => (showAddSheet = open)}
		onSearchQueryChange={service.setSearchQuery}
		onSelectUser={service.selectUser}
		onClearSelection={service.clearUserSelection}
		onRoleChange={handleRoleChange}
		onSubmit={handleAdd}
		onSearchFocus={service.showDropdown}
	/>

	<!-- Remove Member Sheet -->
	<RemoveMemberSheet
		open={showRemoveSheet}
		member={service.memberToRemove}
		removing={service.isRemoving}
		onOpenChange={handleRemoveSheetOpenChange}
		onConfirm={handleRemove}
	/>

	<!-- Transfer Ownership Sheet -->
	<TransferOwnershipSheet
		open={showTransferSheet}
		member={service.transferTarget}
		confirmation={service.transferConfirmation}
		transferring={service.isTransferring}
		onOpenChange={handleTransferSheetOpenChange}
		onConfirmationChange={service.setTransferConfirmation}
		onConfirm={handleTransfer}
	/>
{/if}
