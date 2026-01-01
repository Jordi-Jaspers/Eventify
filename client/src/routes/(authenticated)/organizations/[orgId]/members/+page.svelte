<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/state';
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { SidebarTrigger } from '$lib/components/ui/sidebar';
	import * as Sheet from '$lib/components/ui/sheet';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import {
		getOrganizationMembers,
		addMember,
		updateMemberRole,
		removeMember,
		transferOwnership,
		searchUsersToAdd
	} from '$lib/api/organization/OrganizationMembershipController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import {
		Users,
		UserPlus,
		Shield,
		Crown,
		MoreVertical,
		Trash2,
		CircleAlert,
		LoaderCircle,
		Search,
		X,
		User as UserIcon,
		RefreshCw,
		ChevronDown
	} from '@lucide/svelte';
	import type {
		OrganizationMembershipResponse,
		OrganizationalRole,
		UserSearchResult
	} from '$lib/api/models';
	import { currentUser } from '$lib/stores/auth';

	// Get orgId from route params
	const orgId: number = parseInt(page.params.orgId ?? '0');

	// State
	let members: OrganizationMembershipResponse[] = $state([]);
	let loading: boolean = $state(true);
	let error: string | null = $state(null);

	// Current user's role in this organization
	let currentUserRole: OrganizationalRole | null = $derived(
		members.find((m: OrganizationMembershipResponse) => m.userId === $currentUser?.id)?.role ?? null
	);
	// Global admins have full access (equivalent to owner)
	let isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	let canManageMembers: boolean = $derived(
		isGlobalAdmin || currentUserRole === 'OWNER' || currentUserRole === 'ADMIN'
	);
	// Global admins can do everything an owner can
	let isOwner: boolean = $derived(isGlobalAdmin || currentUserRole === 'OWNER');

	// Add Member Sheet
	let showAddMemberSheet: boolean = $state(false);
	let addMemberEmail: string = $state('');
	let addMemberRole: OrganizationalRole = $state('MEMBER');
	let addingMember: boolean = $state(false);

	// User search state
	let searchQuery: string = $state('');
	let debouncedQuery: string = $state('');
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;
	let isSearching: boolean = $state(false);
	let searchResults: UserSearchResult[] = $state([]);
	let selectedUser: UserSearchResult | null = $state(null);
	let showSearchDropdown: boolean = $state(false);

	// Remove Member Sheet
	let showRemoveSheet: boolean = $state(false);
	let memberToRemove: OrganizationMembershipResponse | null = $state(null);
	let removingMember: boolean = $state(false);

	// Transfer Ownership Sheet
	let showTransferSheet: boolean = $state(false);
	let transferTargetMember: OrganizationMembershipResponse | null = $state(null);
	let transferConfirmation: string = $state('');
	let transferringOwnership: boolean = $state(false);

	// Role update dropdown state
	let roleDropdownOpen: { [key: number]: boolean } = $state({});

	// Debounce search query
	$effect(() => {
		if (debounceTimer) clearTimeout(debounceTimer);

		if (searchQuery.length >= 3) {
			debounceTimer = setTimeout(() => {
				debouncedQuery = searchQuery;
			}, 300);
		} else {
			debouncedQuery = '';
			searchResults = [];
		}

		return () => {
			if (debounceTimer) clearTimeout(debounceTimer);
		};
	});

	// Perform search when debounced query changes
	$effect(() => {
		if (debouncedQuery.length >= 3) {
			performSearch();
		}
	});

	async function performSearch(): Promise<void> {
		isSearching = true;
		showSearchDropdown = true;

		try {
			const results: UserSearchResult[] = await searchUsersToAdd(orgId, debouncedQuery);
			// Filter out users who are already members (by email)
			const memberEmails: Set<string> = new Set(
				members.map((m: OrganizationMembershipResponse) => m.userEmail.toLowerCase())
			);
			searchResults = results.filter((user: UserSearchResult) => {
				const email: string | undefined = user.email;
				return email !== undefined && !memberEmails.has(email.toLowerCase());
			});
		} catch (err: unknown) {
			console.error('Search error:', err);
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	async function loadMembers(): Promise<void> {
		loading = true;
		error = null;

		try {
			members = await getOrganizationMembers(orgId);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load members');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function openAddMemberSheet(): void {
		showAddMemberSheet = true;
		addMemberEmail = '';
		addMemberRole = 'MEMBER';
		selectedUser = null;
		searchQuery = '';
		searchResults = [];
	}

	function handleSelectUser(user: UserSearchResult): void {
		selectedUser = user;
		addMemberEmail = user.email ?? '';
		searchQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	function handleClearUserSelection(): void {
		selectedUser = null;
		addMemberEmail = '';
		searchQuery = '';
		searchResults = [];
		showSearchDropdown = false;
	}

	async function handleAddMember(): Promise<void> {
		if (!addMemberEmail || !addMemberRole) {
			toast.error('Please select a user and role');
			return;
		}

		addingMember = true;

		try {
			const newMember: OrganizationMembershipResponse = await addMember(orgId, {
				email: addMemberEmail,
				role: addMemberRole
			});
			members = [...members, newMember];
			showAddMemberSheet = false;
			toast.success(`${addMemberEmail} added successfully`);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to add member');
			toast.error(message);
		} finally {
			addingMember = false;
		}
	}

	async function handleUpdateRole(
		member: OrganizationMembershipResponse,
		newRole: OrganizationalRole
	): Promise<void> {
		if (member.role === newRole || member.role === 'OWNER') {
			return;
		}

		// Optimistic update
		const oldRole: OrganizationalRole = member.role;
		const memberIndex: number = members.findIndex(
			(m: OrganizationMembershipResponse) => m.id === member.id
		);
		if (memberIndex >= 0) {
			members[memberIndex].role = newRole;
			members = [...members];
		}

		try {
			await updateMemberRole(orgId, member.userId, { role: newRole });
			toast.success(`Role updated to ${newRole}`);
		} catch (err: unknown) {
			// Rollback on error
			if (memberIndex >= 0) {
				members[memberIndex].role = oldRole;
				members = [...members];
			}
			const { message }: { message: string } = handleError(err, 'Failed to update role');
			toast.error(message);
		}
	}

	function openRemoveSheet(member: OrganizationMembershipResponse): void {
		memberToRemove = member;
		showRemoveSheet = true;
	}

	async function handleRemoveMember(): Promise<void> {
		if (!memberToRemove) return;

		removingMember = true;

		try {
			await removeMember(orgId, memberToRemove.userId);
			members = members.filter(
				(m: OrganizationMembershipResponse) => m.id !== memberToRemove?.id
			);
			showRemoveSheet = false;
			toast.success(`${memberToRemove.userEmail} removed successfully`);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to remove member');
			toast.error(message);
		} finally {
			removingMember = false;
			memberToRemove = null;
		}
	}

	function openTransferSheet(member: OrganizationMembershipResponse): void {
		transferTargetMember = member;
		transferConfirmation = '';
		showTransferSheet = true;
	}

	async function handleTransferOwnership(): Promise<void> {
		if (!transferTargetMember || transferConfirmation !== 'transfer') {
			toast.error('Please type "transfer" to confirm');
			return;
		}

		transferringOwnership = true;

		try {
			await transferOwnership(orgId, { newOwnerUserId: transferTargetMember.userId });
			// Reload members to reflect changes
			await loadMembers();
			showTransferSheet = false;
			toast.success(`Ownership transferred to ${transferTargetMember.userEmail}`);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to transfer ownership');
			toast.error(message);
		} finally {
			transferringOwnership = false;
			transferTargetMember = null;
			transferConfirmation = '';
		}
	}

	function getRoleBadgeClass(role: OrganizationalRole): string {
		switch (role) {
			case 'OWNER':
				return 'bg-gradient-to-r from-purple-500 to-purple-600 border-0 text-white';
			case 'ADMIN':
				return 'bg-blue-500/10 border-blue-500/50 text-blue-500';
			case 'MEMBER':
			default:
				return 'border-border/50 bg-background/50 text-muted-foreground';
		}
	}

	function getInitials(firstName: string, lastName: string): string {
		return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
	}

	function formatRelativeDate(dateString: string): string {
		const date: Date = new Date(dateString);
		const now: Date = new Date();
		const diffMs: number = now.getTime() - date.getTime();
		const diffDays: number = Math.floor(diffMs / (1000 * 60 * 60 * 24));

		if (diffDays === 0) return 'Today';
		if (diffDays === 1) return 'Yesterday';
		if (diffDays < 7) return `${diffDays} days ago`;
		if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
		if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
		return `${Math.floor(diffDays / 365)} years ago`;
	}

	onMount(() => {
		loadMembers();
	});
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
					onclick={openAddMemberSheet}
					class="bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg hover:shadow-primary/50"
				>
					<UserPlus class="mr-2 h-4 w-4" />
					Add Member
				</Button>
			{/if}
		</div>

		<!-- Error Alert -->
		{#if error && !loading}
			<Alert
				variant="destructive"
				class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
			>
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{error}
					<Button variant="outline" size="sm" class="ml-4" onclick={loadMembers}>
						<RefreshCw class="h-4 w-4" />
					</Button>
				</AlertDescription>
			</Alert>
		{/if}

		<!-- Members List -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
			<CardHeader>
				<div class="flex items-center gap-2">
					<Users class="w-5 h-5 text-primary" />
					<CardTitle class="text-xl">Members</CardTitle>
				</div>
				<CardDescription>
					{members.length}
					{members.length === 1 ? 'member' : 'members'}
				</CardDescription>
			</CardHeader>
			<CardContent>
				{#if loading}
					<!-- Loading Skeleton -->
					<div class="space-y-3">
						{#each Array(5) as _, i}
							<div
								class="flex items-center gap-4 p-4 rounded-lg border border-border/50 bg-card/30"
							>
								<div class="h-10 w-10 rounded-full bg-muted/50 animate-pulse"></div>
								<div class="flex-1 space-y-2">
									<div class="h-4 bg-muted/50 rounded animate-pulse w-1/4"></div>
									<div class="h-3 bg-muted/50 rounded animate-pulse w-1/3"></div>
								</div>
								<div class="h-6 w-16 bg-muted/50 rounded animate-pulse"></div>
								<div class="h-4 w-24 bg-muted/50 rounded animate-pulse"></div>
							</div>
						{/each}
					</div>
				{:else if members.length === 0}
					<!-- Empty State -->
					<div class="flex flex-col items-center justify-center py-12">
						<div class="relative">
							<div
								class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"
							></div>
							<div
								class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 backdrop-blur-sm"
							>
								<Users class="w-12 h-12 text-primary" />
							</div>
						</div>
						<h3 class="mt-6 text-lg font-semibold">No members yet</h3>
						<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
							Add members to collaborate on this organization
						</p>
					</div>
				{:else}
					<!-- Members List -->
					<div class="space-y-2">
						<!-- Desktop Table Header -->
						<div
							class="hidden md:grid md:grid-cols-12 gap-4 px-4 py-2 text-sm font-medium text-muted-foreground border-b border-border/50"
						>
							<div class="col-span-4">Member</div>
							<div class="col-span-3">Email</div>
							<div class="col-span-2">Role</div>
							<div class="col-span-2">Joined</div>
							<div class="col-span-1 text-right">Actions</div>
						</div>

						<!-- Member Rows -->
						{#each members as member (member.id)}
							<div
								class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
							>
								<!-- Avatar & Name -->
								<div class="col-span-1 md:col-span-4 flex items-center gap-3">
									<div
										class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
									>
										<span class="text-sm font-medium text-primary">
											{getInitials(member.userFirstName, member.userLastName)}
										</span>
									</div>
									<div class="min-w-0">
										<p class="font-medium truncate">
											{member.userFirstName}
											{member.userLastName}
										</p>
										<p class="text-sm text-muted-foreground md:hidden truncate">
											{member.userEmail}
										</p>
									</div>
								</div>

								<!-- Email (desktop only) -->
								<div class="hidden md:flex md:col-span-3 items-center">
									<p class="text-sm text-muted-foreground truncate">{member.userEmail}</p>
								</div>

								<!-- Role Badge/Selector -->
								<div class="col-span-1 md:col-span-2 flex items-center">
									{#if member.role === 'OWNER'}
										<Badge class={getRoleBadgeClass(member.role)}>
											<Crown class="mr-1 h-3 w-3" />
											{member.role}
										</Badge>
									{:else if canManageMembers}
										<!-- Role dropdown for admins/owners -->
										<DropdownMenu.Root>
											<DropdownMenu.Trigger>
												{#snippet child({ props })}
													<Button
														{...props}
														variant="outline"
														size="sm"
														class="bg-background/50 border-border/50 hover:bg-accent/10"
													>
														<Badge class={getRoleBadgeClass(member.role)}>
															{#if member.role === 'ADMIN'}
																<Shield class="mr-1 h-3 w-3" />
															{/if}
															{member.role}
														</Badge>
														<ChevronDown class="ml-1 h-3 w-3" />
													</Button>
												{/snippet}
											</DropdownMenu.Trigger>
											<DropdownMenu.Content
												class="bg-card/95 backdrop-blur-xl border-border/50"
											>
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
									{:else}
										<Badge class={getRoleBadgeClass(member.role)}>
											{#if member.role === 'ADMIN'}
												<Shield class="mr-1 h-3 w-3" />
											{/if}
											{member.role}
										</Badge>
									{/if}
								</div>

								<!-- Joined Date -->
								<div class="col-span-1 md:col-span-2 flex items-center">
									<p class="text-sm text-muted-foreground">
										{formatRelativeDate(member.joinedAt)}
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
											<DropdownMenu.Content
												class="bg-card/95 backdrop-blur-xl border-border/50"
											>
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
						{/each}
					</div>
				{/if}
			</CardContent>
		</Card>
	</div>
</main>

<!-- Add Member Sheet -->
<Sheet.Root bind:open={showAddMemberSheet}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2">
				<UserPlus class="h-5 w-5 text-primary" />
				Add Member
			</Sheet.Title>
			<Sheet.Description>
				Search for a user and assign them a role in the organization
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 space-y-4 py-6">
			<!-- Role Selector -->
			<div class="space-y-2">
				<Label>Role</Label>
				<div class="flex gap-2">
					<Button
							variant={addMemberRole === 'ADMIN' ? 'default' : 'outline'}
							size="sm"
							onclick={() => {
							addMemberRole = 'ADMIN';
						}}
							disabled={addingMember}
							class={addMemberRole === 'ADMIN'
							? 'bg-gradient-to-r from-primary to-accent'
							: 'bg-background/50 border-border/50'}
					>
						<Shield class="mr-2 h-4 w-4" />
						ADMIN
					</Button>
					<Button
							variant={addMemberRole === 'MEMBER' ? 'default' : 'outline'}
							size="sm"
							onclick={() => {
							addMemberRole = 'MEMBER';
						}}
							disabled={addingMember}
							class={addMemberRole === 'MEMBER'
							? 'bg-gradient-to-r from-primary to-accent'
							: 'bg-background/50 border-border/50'}
					>
						<UserIcon class="mr-2 h-4 w-4" />
						MEMBER
					</Button>
				</div>
			</div>

			<!-- User Search -->
			<div class="space-y-2">
				<Label for="user-search">User</Label>
				{#if selectedUser}
					<!-- Selected User Display -->
					<div
						class="flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-background/50 backdrop-blur-sm"
					>
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20"
						>
							<UserIcon class="h-5 w-5 text-primary" />
						</div>
						<div class="flex-1 min-w-0">
							<p class="text-sm font-medium truncate">
								{selectedUser.firstName}
								{selectedUser.lastName}
							</p>
							<p class="text-xs text-muted-foreground truncate">{selectedUser.email}</p>
						</div>
						<Button
							variant="ghost"
							size="sm"
							onclick={handleClearUserSelection}
							disabled={addingMember}
							class="h-8 w-8 p-0 hover:bg-destructive/10 hover:text-destructive transition-colors"
							title="Clear selection"
						>
							<X class="h-4 w-4" />
						</Button>
					</div>
				{:else}
					<!-- Search Input -->
					<div class="relative">
						<Search
							class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground"
						/>
						<input
							type="text"
							bind:value={searchQuery}
							placeholder="Search by name or email (min 3 chars)..."
							disabled={addingMember}
							onfocus={() => {
								if (searchQuery.length >= 3) showSearchDropdown = true;
							}}
							class="flex h-9 w-full rounded-md border border-border/50 bg-background/50 px-3 py-1 pl-9 pr-10 text-sm shadow-sm transition-all file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
						/>
						{#if isSearching}
							<LoaderCircle
								class="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 animate-spin text-primary"
							/>
						{:else if searchQuery.length > 0}
							<button
								type="button"
								onclick={() => {
									searchQuery = '';
									searchResults = [];
									showSearchDropdown = false;
								}}
								class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
								aria-label="Clear search"
							>
								<X class="h-4 w-4" />
							</button>
						{/if}
					</div>

					<!-- Search Dropdown -->
					{#if showSearchDropdown}
						<div
							class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl overflow-hidden"
						>
							{#if searchQuery.length > 0 && searchQuery.length < 3}
								<div class="p-4 text-center">
									<p class="text-sm text-muted-foreground">Type at least 3 characters to search</p>
								</div>
							{:else if isSearching}
								<div class="p-4 flex items-center justify-center gap-2">
									<LoaderCircle class="h-4 w-4 animate-spin text-primary" />
									<p class="text-sm text-muted-foreground">Searching...</p>
								</div>
							{:else if searchResults.length === 0 && debouncedQuery.length >= 3}
								<div class="p-4 text-center">
									<UserIcon class="h-8 w-8 mx-auto mb-2 text-muted-foreground/50" />
									<p class="text-sm text-muted-foreground">No users found</p>
								</div>
							{:else if searchResults.length > 0}
								<div class="max-h-[200px] overflow-y-auto">
									{#each searchResults as user (user.email)}
										<button
											type="button"
											onclick={() => handleSelectUser(user)}
											class="w-full p-3 flex items-center gap-3 hover:bg-accent/10 transition-colors border-b border-border/30 last:border-0"
										>
											<div
												class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
											>
												<UserIcon class="h-5 w-5 text-primary" />
											</div>
											<div class="flex-1 min-w-0 text-left">
												<p class="text-sm font-medium truncate">
													{user.firstName}
													{user.lastName}
												</p>
												<p class="text-xs text-muted-foreground truncate">{user.email}</p>
											</div>
										</button>
									{/each}
								</div>
							{/if}
						</div>
					{/if}
				{/if}
			</div>
		</div>

		<Sheet.Footer class="flex-row gap-2 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => {
					showAddMemberSheet = false;
				}}
				disabled={addingMember}
				class="flex-1 bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={handleAddMember}
				disabled={addingMember || !selectedUser}
				class="flex-1 bg-gradient-to-r from-primary to-accent hover:opacity-90"
			>
				{#if addingMember}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Adding...
				{:else}
					Add Member
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>

<!-- Remove Member Sheet -->
<Sheet.Root bind:open={showRemoveSheet}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<Sheet.Header>
			<Sheet.Title>Remove Member</Sheet.Title>
			<Sheet.Description>
				Are you sure you want to remove <strong>{memberToRemove?.userEmail}</strong> from this organization?
				This action cannot be undone.
			</Sheet.Description>
		</Sheet.Header>

		<Sheet.Footer class="mt-6">
			<Button
				variant="outline"
				onclick={() => {
					showRemoveSheet = false;
				}}
				disabled={removingMember}
				class="bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={handleRemoveMember}
				disabled={removingMember}
				class="bg-destructive hover:bg-destructive/90"
			>
				{#if removingMember}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Removing...
				{:else}
					<Trash2 class="mr-2 h-4 w-4" />
					Remove
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>

<!-- Transfer Ownership Sheet -->
<Sheet.Root bind:open={showTransferSheet}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50">
		<Sheet.Header>
			<Sheet.Title class="flex items-center gap-2">
				<Crown class="h-5 w-5 text-primary" />
				Transfer Ownership
			</Sheet.Title>
			<Sheet.Description class="space-y-2">
				<p>
					You are about to transfer ownership to <strong>{transferTargetMember?.userEmail}</strong
					>.
				</p>
			</Sheet.Description>
		</Sheet.Header>

		<div class="space-y-4 mt-6">
			<Alert class="bg-destructive/10 border-destructive/50">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					<strong>Warning:</strong> After this transfer, you will become an ADMIN and will no longer
					have owner privileges. This action cannot be undone.
				</AlertDescription>
			</Alert>

			<div class="space-y-2">
				<Label>Type <strong>transfer</strong> to confirm:</Label>
				<Input
					type="text"
					bind:value={transferConfirmation}
					placeholder="Type 'transfer' to confirm"
					disabled={transferringOwnership}
					class="bg-background/50 border-border"
				/>
			</div>
		</div>

		<Sheet.Footer class="mt-6">
			<Button
				variant="outline"
				onclick={() => {
					showTransferSheet = false;
				}}
				disabled={transferringOwnership}
				class="bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={handleTransferOwnership}
				disabled={transferringOwnership || transferConfirmation !== 'transfer'}
				class="bg-gradient-to-r from-primary to-accent hover:opacity-90"
			>
				{#if transferringOwnership}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Transferring...
				{:else}
					<Crown class="mr-2 h-4 w-4" />
					Transfer Ownership
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
