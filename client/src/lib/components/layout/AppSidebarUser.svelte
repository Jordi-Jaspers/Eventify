<!--
  AppSidebarUser Component

  Combined organization switcher and user footer for sidebar.
  - Always shows user avatar with initials
  - Shows current org name and user info
  - Dropdown for org switching and user actions
  - Handles collapsed state gracefully

  Props:
  - None (uses currentUser from auth store and organizationStore)

  Usage:
  <AppSidebarUser />
-->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { env } from '$env/dynamic/public';
	import { authStore, currentUser } from '$lib/stores/auth';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { ChevronsUpDown, User, LogOut, Building2, Check, RefreshCw, Sun, Moon, Palette } from '@lucide/svelte';
	import { Badge } from '$lib/components/ui/badge';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import type { UserOrganizationResponse } from '$lib/api/models';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import { onMount } from 'svelte';

	// Theme state
	let isDarkMode: boolean = $state(true);
	const showDevPlaybook: boolean = env.PUBLIC_SHOW_DEV_CREDENTIALS === 'true';

	onMount(() => {
		isDarkMode = document.documentElement.classList.contains('dark');
	});

	function toggleTheme(): void {
		isDarkMode = !isDarkMode;
		if (isDarkMode) {
			document.documentElement.classList.add('dark');
		} else {
			document.documentElement.classList.remove('dark');
		}
	}

	// Organization state
	const loading: boolean = $derived(organizationStore.loading);
	const error: string | null = $derived(organizationStore.error);
	const organizations: UserOrganizationResponse[] = $derived(organizationStore.organizations);
	const currentOrganization: UserOrganizationResponse | null = $derived(
		organizationStore.currentOrganization
	);
	const hasOrgs: boolean = $derived(organizations.length > 0);

	const currentPath: string = $derived(page.url.pathname);

	async function handleLogout(): Promise<void> {
		try {
			await authStore.logout();
			await goto(CLIENT_ROUTES.LANDING_PAGE.path);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Logout failed. Please try again.'
			);
			toast.error(message);
		}
	}

	async function handleOrgSwitch(orgId: number): Promise<void> {
		organizationStore.switchOrganization(orgId);

		// Check if we're on an org-specific page and navigate to the new org
		const orgRouteMatch: RegExpMatchArray | null = currentPath.match(/^\/organizations\/(\d+)(\/.*)?$/);
		if (orgRouteMatch) {
			const subPath: string = orgRouteMatch[2] || '';
			const newPath: string = `/organizations/${orgId}${subPath}`;
			await goto(newPath);
		}
	}

	async function handleRetry(): Promise<void> {
		await organizationStore.refreshOrganizations();
	}

	// Generate user initials
	const userInitials: string = $derived.by(() => {
		if (!$currentUser) return '?';
		const firstInitial: string = $currentUser.firstName?.charAt(0)?.toUpperCase() || '';
		const lastInitial: string = $currentUser.lastName?.charAt(0)?.toUpperCase() || '';
		return firstInitial + lastInitial || '?';
	});

	const userName: string = $derived.by(() => {
		if (!$currentUser) return 'User';
		return `${$currentUser.firstName} ${$currentUser.lastName}`;
	});

	const userEmail: string = $derived($currentUser?.email || 'user@example.com');

	function getOrgInitial(name: string | undefined): string {
		return name?.charAt(0)?.toUpperCase() || '?';
	}
</script>

<Sidebar.Footer class="border-t border-border/50">
	<Sidebar.Menu>
		<Sidebar.MenuItem>
			<DropdownMenu.Root>
				<DropdownMenu.Trigger>
					{#snippet child({ props })}
						<Sidebar.MenuButton
							{...props}
							size="lg"
							class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground h-auto py-3"
						>
							<!-- Avatar: Always show user initials -->
							<div
								class="flex aspect-square size-10 group-data-[collapsible=icon]:size-8 items-center justify-center rounded-full bg-gradient-to-br from-primary to-accent text-primary-foreground font-semibold text-sm group-data-[collapsible=icon]:text-xs shadow-lg transition-all"
							>
								{userInitials}
							</div>

							<!-- Text content - more detailed -->
							<div class="grid flex-1 text-left leading-tight gap-0.5">
								{#if loading}
									<span class="truncate font-semibold text-sm">{userName}</span>
									<span class="truncate text-xs text-muted-foreground">Loading organizations...</span>
								{:else if error}
									<span class="truncate font-semibold text-sm">{userName}</span>
									<span class="truncate text-xs text-destructive">Failed to load orgs</span>
								{:else if currentOrganization}
									<!-- Show user name prominently -->
									<span class="truncate font-semibold text-sm">{userName}</span>
									<!-- Show org name with icon -->
									<div class="flex items-center gap-1 text-xs text-muted-foreground">
										<Building2 class="size-3 flex-shrink-0" />
										<span class="truncate">{currentOrganization.organizationName}</span>
									</div>
								{:else}
									<span class="truncate font-semibold text-sm">{userName}</span>
									<span class="truncate text-xs text-muted-foreground">{userEmail}</span>
								{/if}
							</div>
							<ChevronsUpDown class="ml-auto size-4 text-muted-foreground" />
						</Sidebar.MenuButton>
					{/snippet}
				</DropdownMenu.Trigger>
				<DropdownMenu.Content
					class="min-w-72 rounded-lg bg-card/95 backdrop-blur-xl border-border/50"
					side="right"
					align="end"
					sideOffset={4}
				>
					<!-- User Header -->
					<div class="px-3 py-3 border-b border-border/50">
						<div class="flex items-center gap-3">
							<div
								class="flex aspect-square size-10 items-center justify-center rounded-full bg-gradient-to-br from-primary to-accent text-primary-foreground font-semibold text-sm"
							>
								{userInitials}
							</div>
							<div class="flex-1 min-w-0">
								<p class="font-semibold text-sm truncate">{userName}</p>
								<p class="text-xs text-muted-foreground truncate">{userEmail}</p>
							</div>
						</div>
					</div>

					<!-- Organization Section -->
					{#if error}
						<div class="p-1">
							<DropdownMenu.Item
								class="cursor-pointer hover:bg-primary/10"
								onclick={handleRetry}
							>
								<RefreshCw class="mr-2 h-4 w-4" />
								<span>Retry loading organizations</span>
							</DropdownMenu.Item>
						</div>
					{:else if hasOrgs}
						<div class="p-1">
							<DropdownMenu.Label class="text-xs text-muted-foreground px-2 py-1.5">
								Switch Organization
							</DropdownMenu.Label>
							{#each organizations as org (org.organizationId)}
								<DropdownMenu.Item
									class="cursor-pointer hover:bg-primary/10 flex items-center gap-3 px-2 py-2"
									onclick={() => handleOrgSwitch(org.organizationId!)}
								>
									<!-- Org Avatar -->
									<div
										class="flex aspect-square size-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary/80 to-accent/80 text-primary-foreground font-semibold text-xs"
									>
										{getOrgInitial(org.organizationName)}
									</div>
									<div class="flex-1 min-w-0">
										<div class="font-medium truncate text-sm">{org.organizationName}</div>
										<Badge
											class="{getOrganizationalRoleBadgeClass(org.role)} w-fit text-[10px] px-1.5 py-0 leading-tight mt-0.5"
										>
											{org.role}
										</Badge>
									</div>
									{#if org.organizationId === currentOrganization?.organizationId}
										<Check class="size-4 text-primary flex-shrink-0" />
									{/if}
								</DropdownMenu.Item>
							{/each}
						</div>
					{:else if !loading}
						<div class="p-1">
							<div class="px-2 py-3 text-center">
								<Building2 class="size-5 mx-auto mb-1 text-muted-foreground/50" />
								<p class="text-xs text-muted-foreground">No organizations</p>
							</div>
						</div>
					{/if}

					<DropdownMenu.Separator />

					<!-- Account Actions -->
					<div class="p-1">
						<DropdownMenu.Item
							class="cursor-pointer hover:bg-primary/10"
							onclick={() => goto(CLIENT_ROUTES.PROFILE_PAGE.path)}
						>
							<User class="mr-2 h-4 w-4" />
							<span>Profile</span>
						</DropdownMenu.Item>
						<DropdownMenu.Item
							class="cursor-pointer hover:bg-primary/10"
							onclick={toggleTheme}
						>
							{#if isDarkMode}
								<Sun class="mr-2 h-4 w-4" />
								<span>Light Mode</span>
							{:else}
								<Moon class="mr-2 h-4 w-4" />
								<span>Dark Mode</span>
							{/if}
						</DropdownMenu.Item>
						{#if showDevPlaybook}
							<DropdownMenu.Item
								class="cursor-pointer hover:bg-primary/10"
								onclick={() => goto('/dev-playbook')}
							>
								<Palette class="mr-2 h-4 w-4" />
								<span>Component Playbook</span>
							</DropdownMenu.Item>
						{/if}
					</div>

					<DropdownMenu.Separator />

					<div class="p-1">
						<DropdownMenu.Item
							class="cursor-pointer hover:bg-destructive/10 hover:text-destructive focus:bg-destructive/10 focus:text-destructive"
							onclick={handleLogout}
						>
							<LogOut class="mr-2 h-4 w-4" />
							<span>Log out</span>
						</DropdownMenu.Item>
					</div>
				</DropdownMenu.Content>
			</DropdownMenu.Root>
		</Sidebar.MenuItem>
	</Sidebar.Menu>
</Sidebar.Footer>
