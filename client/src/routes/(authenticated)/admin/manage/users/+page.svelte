<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import { searchUsers } from '$lib/api/admin/AdminUserController';
	import { createAdminUserService } from '$lib/api/admin/service/AdminUserService.svelte';
	import type { UserDetailsResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { UserCog, MoreVertical, Eye, Lock, Unlock, Key } from '@lucide/svelte';
	import { PageHeader } from '$lib/components/ui/page-header';
	import { getInitials, getFullName } from '$lib/utils/string';
	import { formatDate } from '$lib/utils/date';
	import { getUserRoleBadgeClass } from '$lib/utils/role';
	import { UserDetailsSheet } from '$lib/components/admin';
	import { formatLastUsed } from '$lib/components/admin/utils';
	import { InitialsAvatar } from '$lib/components/ui/initials-avatar';
	import { userTableColumns, getStatusBadgeVariant, getStatusLabel } from '$lib/config/user-table-columns';

	// Columns configuration
	const columns = userTableColumns;

	// Services
	const dataTableService = createDataTableService<UserDetailsResponse>({
		fetchFn: searchUsers,
		pageSize: 10,
		defaultSort: [{name: 'role', direction: 'ASC'},{name: 'email', direction: 'ASC'}]
	});
	const adminUserService = createAdminUserService();

	// User details sheet state
	let showUserSheet: boolean = $state(false);
	let selectedUser: UserDetailsResponse | null = $state(null);

	// Helper functions
	function getUserInitials(user: UserDetailsResponse): string {
		const firstName: string = user.firstName ?? 'U';
		const lastName: string = user.lastName ?? 'U';
		return getInitials(firstName, lastName);
	}

	function getFullNameForUser(user: UserDetailsResponse): string {
		return getFullName(user.firstName, user.lastName);
	}

	// User details sheet handlers
	function openUserSheet(user: UserDetailsResponse): void {
		selectedUser = user;
		showUserSheet = true;
	}

	function closeUserSheet(): void {
		showUserSheet = false;
		selectedUser = null;
	}

	// Update the selected user with the latest details
	function updateSelectedUser(details: UserDetailsResponse): void {
		selectedUser = details;
	}

	// Role update handler
	async function handleRoleChange(userId: number | undefined, newRole: 'USER' | 'ADMIN'): Promise<void> {
		const updated = await adminUserService.updateRole(userId, newRole);
		if (updated) {
			updateSelectedUser(updated);
			dataTableService.load(); // Refresh table
		}
	}

	// Lock/unlock handlers
	async function handleLockToggle(userId: number | undefined, isLocked: boolean): Promise<void> {
		const updated = await adminUserService.toggleLock(userId, isLocked);
		if (updated) {
			updateSelectedUser(updated);
			dataTableService.load(); // Refresh table
		}
	}

	async function handleQuickLock(user: UserDetailsResponse, isLocked: boolean): Promise<void> {
		const updated = await adminUserService.toggleLock(user.id, isLocked);
		if (updated) {
			dataTableService.load(); // Refresh table
		}
	}

	async function handleQuickRoleChange(user: UserDetailsResponse, newRole: 'USER' | 'ADMIN'): Promise<void> {
		const updated = await adminUserService.updateRole(user.id, newRole);
		if (updated) {
			dataTableService.load(); // Refresh table
		}
	}

	// Quick force reset from dropdown
	async function handleQuickForceReset(user: UserDetailsResponse): Promise<void> {
		await adminUserService.forcePasswordReset(user.id, user.email);
	}

	// Force reset from sheet
	async function handleForcePasswordReset(userId: number | undefined, email: string | undefined): Promise<void> {
		await adminUserService.forcePasswordReset(userId, email);
	}

	onMount(() => dataTableService.load());
</script>

<svelte:head>
	<title>Users - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<PageHeader title="Users" description="Manage and monitor all users on the platform" />

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Users" icon={UserCog}>
			{#snippet row(user: UserDetailsResponse)}
				<div
					role="button"
					tabindex="0"
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 p-4 hover:bg-muted/30 transition-all text-left w-full cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
					onclick={() => openUserSheet(user)}
					onkeydown={(e) => { if (e.key === 'Enter' || e.key === ' ') openUserSheet(user); }}
				>
					<!-- User (Avatar + Name + Email) -->
					<div class="col-span-1 md:col-span-3">
						<div class="flex items-center gap-3">
							<!-- Avatar -->
							<InitialsAvatar initials={getUserInitials(user)} size="md" />
							<!-- Name + Email -->
							<div class="min-w-0">
								<div class="font-medium truncate">{getFullNameForUser(user)}</div>
								<div class="text-sm text-muted-foreground truncate md:hidden">{user.email}</div>
							</div>
						</div>
					</div>

					<!-- Email (desktop only) -->
					<div class="hidden md:flex md:col-span-2 items-center">
						<span class="text-sm text-muted-foreground truncate">{user.email}</span>
					</div>

					<!-- Role -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<Badge class={getUserRoleBadgeClass(user.role)}>
							{user.role}
						</Badge>
					</div>

					<!-- Status -->
					<div class="col-span-1 md:col-span-1 flex items-center">
						<Badge variant={getStatusBadgeVariant(user.enabled, user.validated)}>
							{getStatusLabel(user.enabled, user.validated)}
						</Badge>
					</div>

					<!-- Created -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm text-muted-foreground">
							<span class="md:hidden">Created: </span>
							{formatDate(user.createdAt ?? '')}
						</span>
					</div>

					<!-- Last Login -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm text-muted-foreground">
							<span class="md:hidden">Last Login: </span>
							{formatLastUsed(user.lastLogin)}
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
								<DropdownMenu.Item onclick={() => openUserSheet(user)} class="cursor-pointer">
									<Eye class="mr-2 h-4 w-4" />
									View Details
								</DropdownMenu.Item>
								<DropdownMenu.Separator />
								<DropdownMenu.Item 
									onclick={() => handleQuickLock(user, !user.enabled)}
									class="cursor-pointer"
								>
									{#if user.enabled}
										<Lock class="mr-2 h-4 w-4" />
										Lock User
									{:else}
										<Unlock class="mr-2 h-4 w-4" />
										Unlock User
									{/if}
								</DropdownMenu.Item>
							<DropdownMenu.Sub>
									<DropdownMenu.SubTrigger class="cursor-pointer">
										<UserCog class="mr-2 h-4 w-4" />
										Change Role
									</DropdownMenu.SubTrigger>
									<DropdownMenu.SubContent class="bg-card/95 backdrop-blur-xl border-border/50">
										<DropdownMenu.Item 
											onclick={() => handleQuickRoleChange(user, 'USER')}
											class="cursor-pointer"
											disabled={user.role === 'USER'}
										>
											User
										</DropdownMenu.Item>
										<DropdownMenu.Item 
											onclick={() => handleQuickRoleChange(user, 'ADMIN')}
											class="cursor-pointer"
											disabled={user.role === 'ADMIN'}
										>
											Admin
										</DropdownMenu.Item>
									</DropdownMenu.SubContent>
							</DropdownMenu.Sub>
							<DropdownMenu.Separator />
							<DropdownMenu.Item 
								onclick={() => handleQuickForceReset(user)}
								class="cursor-pointer"
								disabled={adminUserService.forcingPasswordReset}
							>
								<Key class="mr-2 h-4 w-4" />
								<span>Force Password Reset</span>
							</DropdownMenu.Item>
							</DropdownMenu.Content>
						</DropdownMenu.Root>
					</div>
				</div>
			{/snippet}
		</DataTable>
	</div>
</main>

<!-- User Details Sheet -->
<UserDetailsSheet
	open={showUserSheet}
	user={selectedUser}
	updatingRole={adminUserService.updatingRole}
	lockingUser={adminUserService.lockingUser}
	forcingPasswordReset={adminUserService.forcingPasswordReset}
	onOpenChange={(open) => { if (!open) closeUserSheet(); }}
	onRoleChange={handleRoleChange}
	onLockToggle={handleLockToggle}
	onForcePasswordReset={handleForcePasswordReset}
/>
