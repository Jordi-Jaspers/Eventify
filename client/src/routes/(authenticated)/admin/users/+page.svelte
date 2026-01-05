<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchUsers } from '$lib/api/admin/AdminUserController';
	import { createAdminUserService } from '$lib/api/admin/AdminUserService.svelte';
	import type { UserDetailsResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Sheet from '$lib/components/ui/sheet';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { UserCog, MoreVertical, Eye, Lock, Unlock, Key, Building2 } from '@lucide/svelte';
	import { getInitials } from '$lib/utils/string';
	import { formatDate } from '$lib/utils/date';

	// Columns configuration
	const columns: DataTableColumn<UserDetailsResponse>[] = [
		{
			key: 'search',
			label: 'User',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search by name or email...',
			colSpan: 3
		},
		{
			key: 'email',
			label: 'Email',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'role',
			label: 'Role',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ value: 'USER', label: 'User' },
				{ value: 'ADMIN', label: 'Admin' }
			],
			colSpan: 1
		},
		{
			key: 'enabled',
			label: 'Status',
			sortable: true,
			filterable: true,
			filterType: 'BOOLEAN',
			colSpan: 1
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'lastLogin',
			label: 'Last Login',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'actions',
			label: 'Actions',
			colSpan: 1
		}
	];

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
	function getRoleBadgeVariant(role: 'USER' | 'ADMIN' | undefined): 'default' | 'secondary' {
		return role === 'ADMIN' ? 'secondary' : 'default';
	}

	function getStatusBadgeVariant(enabled: boolean | undefined, validated: boolean | undefined): 'success' | 'destructive' | 'default' {
		if (!enabled) return 'destructive';
		if (!validated) return 'default';
		return 'success';
	}

	function getStatusLabel(enabled: boolean | undefined, validated: boolean | undefined): string {
		if (!enabled) return 'Locked';
		if (!validated) return 'Pending Verification';
		return 'Active';
	}

	function formatLastLogin(lastLogin: string | null | undefined): string {
		if (!lastLogin) return 'Never';
		return formatDate(lastLogin);
	}

	function getUserInitials(user: UserDetailsResponse): string {
		const firstName: string = user.firstName ?? 'U';
		const lastName: string = user.lastName ?? 'U';
		return getInitials(firstName, lastName);
	}

	function getFullName(user: UserDetailsResponse): string {
		const firstName: string = user.firstName ?? '';
		const lastName: string = user.lastName ?? '';
		return `${firstName} ${lastName}`.trim() || 'Unknown User';
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

	onMount(() => dataTableService.load());
</script>

<svelte:head>
	<title>Users - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1
				class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent"
			>
				Users
			</h1>
			<p class="text-muted-foreground mt-2">Manage and monitor all users on the platform</p>
		</div>

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Users" icon={UserCog}>
			{#snippet row(user: UserDetailsResponse)}
				<div
					role="button"
					tabindex="0"
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors text-left w-full cursor-pointer"
					onclick={() => openUserSheet(user)}
					onkeydown={(e) => { if (e.key === 'Enter' || e.key === ' ') openUserSheet(user); }}
				>
					<!-- User (Avatar + Name + Email) -->
					<div class="col-span-1 md:col-span-3">
						<div class="flex items-center gap-3">
							<!-- Avatar -->
							<div class="h-10 w-10 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center flex-shrink-0">
								<span class="text-sm font-semibold text-primary">{getUserInitials(user)}</span>
							</div>
							<!-- Name + Email -->
							<div class="min-w-0">
								<div class="font-medium truncate">{getFullName(user)}</div>
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
						<Badge variant={getRoleBadgeVariant(user.role)}>
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
							{formatLastLogin(user.lastLogin)}
						</span>
					</div>

					<!-- Actions -->
					<div class="col-span-1 md:col-span-1 flex items-center justify-end" role="none" onclick={(e) => e.stopPropagation()}>
						<DropdownMenu.Root>
							<DropdownMenu.Trigger>
								<Button variant="ghost" size="sm" class="h-8 w-8 p-0">
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
								<DropdownMenu.Item disabled class="cursor-not-allowed opacity-50">
									<Key class="mr-2 h-4 w-4" />
									<span>Force Password Reset</span>
									<span class="ml-auto text-xs text-muted-foreground">(Soon)</span>
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
<Sheet.Root open={showUserSheet} onOpenChange={(open) => { if (!open) closeUserSheet(); }}>
	<Sheet.Content class="w-full sm:max-w-md bg-background border-border/50 overflow-y-auto p-0">
		{#if selectedUser}
			<!-- Hero Header with Gradient -->
			<div class="relative bg-gradient-to-br from-primary/20 via-accent/10 to-background pt-8 pb-12 px-6">
				<div class="absolute inset-0 bg-grid-white/5"></div>
				<div class="relative flex flex-col items-center text-center">
					<!-- Avatar -->
					<div class="h-20 w-20 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center shadow-lg shadow-primary/25 ring-4 ring-background">
						<span class="text-2xl font-bold text-white">
							{getUserInitials(selectedUser)}
						</span>
					</div>
					<!-- Name + Email -->
					<h2 class="mt-4 text-xl font-semibold">{getFullName(selectedUser)}</h2>
					<p class="text-sm text-muted-foreground mt-1">{selectedUser.email}</p>
					<!-- Status Badge -->
					<div class="mt-3">
						<Badge 
							variant={getStatusBadgeVariant(selectedUser.enabled, selectedUser.validated)}
							class="px-3 py-1"
						>
							{getStatusLabel(selectedUser.enabled, selectedUser.validated)}
						</Badge>
					</div>
				</div>
			</div>

			<!-- Content -->
			<div class="px-6 -mt-6">
				<!-- Role Card -->
				<div class="rounded-xl border border-border/50 bg-card p-4 shadow-sm">
					<div class="flex items-center justify-between mb-3">
						<span class="text-sm font-medium text-muted-foreground">Role</span>
						<Badge variant="outline" class="font-mono text-xs">
							{selectedUser.role}
						</Badge>
					</div>
					<div class="flex gap-2">
						<Button
							variant={selectedUser.role === 'USER' ? 'default' : 'outline'}
							size="sm"
							class="flex-1 {selectedUser.role === 'USER' ? 'bg-gradient-to-r from-primary to-accent hover:opacity-90' : ''}"
							onclick={() => handleRoleChange(selectedUser?.id, 'USER')}
							disabled={adminUserService.updatingRole || selectedUser.role === 'USER'}
						>
							<UserCog class="h-4 w-4 mr-1.5" />
							User
						</Button>
						<Button
							variant={selectedUser.role === 'ADMIN' ? 'default' : 'outline'}
							size="sm"
							class="flex-1 {selectedUser.role === 'ADMIN' ? 'bg-gradient-to-r from-primary to-accent hover:opacity-90' : ''}"
							onclick={() => handleRoleChange(selectedUser?.id, 'ADMIN')}
							disabled={adminUserService.updatingRole || selectedUser.role === 'ADMIN'}
						>
							<Key class="h-4 w-4 mr-1.5" />
							Admin
						</Button>
					</div>
				</div>

				<!-- Info Grid -->
				<div class="mt-4 grid grid-cols-2 gap-3">
					<div class="rounded-lg border border-border/50 bg-card/50 p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Created</p>
						<p class="text-sm font-medium mt-1">{formatDate(selectedUser.createdAt ?? '')}</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/50 p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Last Login</p>
						<p class="text-sm font-medium mt-1">{formatLastLogin(selectedUser.lastLogin)}</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/50 p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Email Verified</p>
						<p class="text-sm font-medium mt-1 flex items-center gap-1.5">
							{#if selectedUser.validated}
								<span class="h-2 w-2 rounded-full bg-green-500"></span>
								Verified
							{:else}
								<span class="h-2 w-2 rounded-full bg-yellow-500"></span>
								Pending
							{/if}
						</p>
					</div>
					<div class="rounded-lg border border-border/50 bg-card/50 p-3">
						<p class="text-xs text-muted-foreground uppercase tracking-wide">Account</p>
						<p class="text-sm font-medium mt-1 flex items-center gap-1.5">
							{#if selectedUser.enabled}
								<span class="h-2 w-2 rounded-full bg-green-500"></span>
								Active
							{:else}
								<span class="h-2 w-2 rounded-full bg-red-500"></span>
								Locked
							{/if}
						</p>
					</div>
				</div>

				<!-- Organizations -->
				{#if selectedUser?.organizations && selectedUser.organizations.length > 0}
					<div class="mt-4">
						<p class="text-xs text-muted-foreground uppercase tracking-wide mb-2">
							Organizations ({selectedUser.organizations.length})
						</p>
						<div class="space-y-2">
							{#each selectedUser.organizations as org}
								<div class="flex items-center gap-3 rounded-lg border border-border/50 bg-card/50 p-3">
									<div class="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
										<Building2 class="h-4 w-4 text-primary" />
									</div>
									<div class="flex-1 min-w-0">
										<p class="text-sm font-medium truncate">{org.organizationName}</p>
										<p class="text-xs text-muted-foreground">{org.role}</p>
									</div>
								</div>
							{/each}
						</div>
					</div>
				{/if}
			</div>

			<!-- Footer Actions -->
			<div class="sticky bottom-0 mt-6 p-4 border-t border-border/50 bg-background/95 backdrop-blur-sm">
				<div class="flex gap-2">
					<Button
						variant={selectedUser.enabled ? 'destructive' : 'default'}
						class="flex-1"
						onclick={() => handleLockToggle(selectedUser?.id, !selectedUser?.enabled)}
						disabled={adminUserService.lockingUser}
					>
						{#if selectedUser.enabled}
							<Lock class="mr-2 h-4 w-4" />
							Lock User
						{:else}
							<Unlock class="mr-2 h-4 w-4" />
							Unlock User
						{/if}
					</Button>
					<Button variant="outline" onclick={closeUserSheet}>
						Close
					</Button>
				</div>
			</div>
		{/if}
	</Sheet.Content>
</Sheet.Root>
