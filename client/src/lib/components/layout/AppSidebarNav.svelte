<script lang="ts">
	import { goto } from '$app/navigation';
	import { currentUser } from '$lib/stores/auth';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import type { UserOrganizationResponse } from '$lib/api/models';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { isOrgAdmin } from '$lib/utils/role';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import { LayoutDashboard, Shield, Building2, Users, UserCog, Settings, Key, Radio, ClipboardList, Activity, BarChart3, Database, Wrench } from '@lucide/svelte';

	interface Props {
		currentPath: string;
	}

	let { currentPath }: Props = $props();

	const isAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	const currentOrganization: UserOrganizationResponse | null = $derived(
		organizationStore.currentOrganization
	);
	
	// Check if user can manage org settings (OWNER, ADMIN, or global ADMIN)
	const canManageOrgSettings: boolean = $derived.by((): boolean => {
		if (!currentOrganization) return false;
		return isAdmin || isOrgAdmin(currentOrganization.role);
	});

	// Helper to check if route is active
	function isActive(path: string): boolean {
		return currentPath === path;
	}
</script>

<Sidebar.Content>
	<!-- USER WORKSPACE Section -->
	<Sidebar.Group>
		<Sidebar.GroupLabel>USER WORKSPACE</Sidebar.GroupLabel>
		<Sidebar.GroupContent>
			<Sidebar.Menu>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton
						onclick={() => goto(CLIENT_ROUTES.DASHBOARD_PAGE.path)}
						isActive={isActive(CLIENT_ROUTES.DASHBOARD_PAGE.path)}
						tooltipContent="Dashboard"
					>
						<LayoutDashboard class="size-4" />
						<span>Dashboard</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton
						onclick={() => goto(CLIENT_ROUTES.MONITOR_PAGE.path)}
						isActive={isActive(CLIENT_ROUTES.MONITOR_PAGE.path)}
						tooltipContent="Monitor"
					>
						<Activity class="size-4" />
						<span>Monitor</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton
						onclick={() => goto(CLIENT_ROUTES.WATCHLISTS_PAGE.path)}
						isActive={isActive(CLIENT_ROUTES.WATCHLISTS_PAGE.path)}
						tooltipContent="Watchlists"
					>
						<ClipboardList class="size-4" />
						<span>Watchlists</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton
						onclick={() => goto(CLIENT_ROUTES.CHANNELS_PAGE.path)}
						isActive={isActive(CLIENT_ROUTES.CHANNELS_PAGE.path)}
						tooltipContent="Channels"
					>
						<Radio class="size-4" />
						<span>Channels</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
			</Sidebar.Menu>
		</Sidebar.GroupContent>
	</Sidebar.Group>

	<!-- ORG WORKSPACE Section (only if org selected) -->
	{#if currentOrganization}
		<Sidebar.Group>
			<Sidebar.GroupLabel>ORG WORKSPACE</Sidebar.GroupLabel>
			<Sidebar.GroupContent>
				<Sidebar.Menu>
					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(currentOrganization.organizationId).path)}
							isActive={isActive(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(currentOrganization.organizationId).path)}
							tooltipContent="Monitor"
						>
							<Activity class="size-4" />
							<span>Monitor</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>
					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(currentOrganization.organizationId).path)}
							isActive={isActive(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(currentOrganization.organizationId).path)}
							tooltipContent="Watchlists"
						>
							<ClipboardList class="size-4" />
							<span>Watchlists</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>
					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_CHANNELS_PAGE(currentOrganization.organizationId).path)}
							isActive={isActive(CLIENT_ROUTES.ORGANIZATION_CHANNELS_PAGE(currentOrganization.organizationId).path)}
							tooltipContent="Channels"
						>
							<Radio class="size-4" />
							<span>Channels</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>
					{#if canManageOrgSettings}
						<Sidebar.MenuItem>
							<Sidebar.MenuButton
								onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(currentOrganization.organizationId).path)}
								isActive={isActive(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(currentOrganization.organizationId).path)}
								tooltipContent="Members"
							>
								<Users class="size-4" />
								<span>Members</span>
							</Sidebar.MenuButton>
						</Sidebar.MenuItem>
						<Sidebar.MenuItem>
							<Sidebar.MenuButton
								onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_STATISTICS_PAGE(currentOrganization.organizationId).path)}
								isActive={isActive(CLIENT_ROUTES.ORGANIZATION_STATISTICS_PAGE(currentOrganization.organizationId).path)}
								tooltipContent="Statistics"
							>
								<BarChart3 class="size-4" />
								<span>Statistics</span>
							</Sidebar.MenuButton>
						</Sidebar.MenuItem>
						<Sidebar.MenuItem>
							<Sidebar.MenuButton
								onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_SETTINGS_PAGE(currentOrganization.organizationId).path)}
								isActive={currentPath.startsWith(`/organizations/${currentOrganization.organizationId}/settings`)}
								tooltipContent="Settings"
							>
								<Settings class="size-4" />
								<span>Settings</span>
							</Sidebar.MenuButton>
						</Sidebar.MenuItem>
					{/if}
				</Sidebar.Menu>
			</Sidebar.GroupContent>
		</Sidebar.Group>
	{/if}

	<!-- ADMINISTRATION Section (admin only) -->
	{#if isAdmin}
		<Sidebar.Group>
			<Sidebar.GroupLabel>ADMINISTRATION</Sidebar.GroupLabel>
			<Sidebar.GroupContent>
				<Sidebar.Menu>
					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ADMIN_MANAGE_PAGE.path)}
							isActive={currentPath.startsWith('/admin/manage')}
							tooltipContent="Manage"
						>
							<Database class="size-4" />
							<span>Manage</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>

					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ADMIN_TOOLS_PAGE.path)}
							isActive={currentPath.startsWith('/admin/tools')}
							tooltipContent="Tools"
						>
							<Wrench class="size-4" />
							<span>Tools</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>

					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ADMIN_STATISTICS_PAGE.path)}
							isActive={currentPath.startsWith('/admin/statistics')}
							tooltipContent="Statistics"
						>
							<BarChart3 class="size-4" />
							<span>Statistics</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>
				</Sidebar.Menu>
			</Sidebar.GroupContent>
		</Sidebar.Group>
	{/if}
</Sidebar.Content>
