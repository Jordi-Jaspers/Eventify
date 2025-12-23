<!--
  AppSidebarNav Component

  Navigation menu for sidebar with role-based sections.

  Props:
  - currentPath: string - Current URL path for highlighting active items

  Usage:
  <AppSidebarNav currentPath={page.url.pathname} />
-->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { currentUser } from '$lib/stores/auth';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import { LayoutDashboard, Clock, Shield, Building2, Plus, ChevronDown } from '@lucide/svelte';

	interface Props {
		currentPath: string;
	}

	let { currentPath }: Props = $props();

	const isAdmin: boolean = $derived($currentUser?.role === 'ADMIN');

	// Track which collapsible is open
	let isOrganizationsOpen: boolean = $state(false);

	// Helper to check if route is active
	function isActive(path: string): boolean {
		return currentPath === path;
	}
</script>

<Sidebar.Content>
	<!-- MAIN Section -->
	<Sidebar.Group>
		<Sidebar.GroupLabel>MAIN</Sidebar.GroupLabel>
		<Sidebar.GroupContent>
			<Sidebar.Menu>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton
						onclick={() => goto(CLIENT_ROUTES.DASHBOARD_PAGE.path)}
						isActive={isActive(CLIENT_ROUTES.DASHBOARD_PAGE.path)}
					>
						<LayoutDashboard class="size-4" />
						<span>Dashboard</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
				<Sidebar.MenuItem>
					<Sidebar.MenuButton class="opacity-50 cursor-not-allowed" onclick={() => {}}>
						<Clock class="size-4" />
						<span>Coming Soon...</span>
					</Sidebar.MenuButton>
				</Sidebar.MenuItem>
			</Sidebar.Menu>
		</Sidebar.GroupContent>
	</Sidebar.Group>

	<!-- ADMINISTRATION Section (admin only) -->
	{#if isAdmin}
		<Sidebar.Group>
			<Sidebar.GroupLabel>ADMINISTRATION</Sidebar.GroupLabel>
			<Sidebar.GroupContent>
				<Sidebar.Menu>
					<Sidebar.MenuItem>
						<Sidebar.MenuButton
							onclick={() => goto(CLIENT_ROUTES.ADMIN_DASHBOARD_PAGE.path)}
							isActive={isActive(CLIENT_ROUTES.ADMIN_DASHBOARD_PAGE.path)}
						>
							<Shield class="size-4" />
							<span>Admin Dashboard</span>
						</Sidebar.MenuButton>
					</Sidebar.MenuItem>

					<!-- Organizations Collapsible -->
					<Sidebar.MenuItem>
						<Sidebar.MenuButton onclick={() => (isOrganizationsOpen = !isOrganizationsOpen)}>
							<Building2 class="size-4" />
							<span>Organizations</span>
							<ChevronDown class="ml-auto size-4 transition-transform duration-200" style="transform: rotate({isOrganizationsOpen ? 180 : 0}deg)" />
						</Sidebar.MenuButton>
						{#if isOrganizationsOpen}
							<Sidebar.MenuSub>
								<Sidebar.MenuSubItem>
									<Sidebar.MenuSubButton
										onclick={() => goto(CLIENT_ROUTES.ADMIN_ORGANIZATIONS_NEW.path)}
										isActive={isActive(CLIENT_ROUTES.ADMIN_ORGANIZATIONS_NEW.path)}
									>
										<Plus class="size-4" />
										<span>Create New</span>
									</Sidebar.MenuSubButton>
								</Sidebar.MenuSubItem>
							</Sidebar.MenuSub>
						{/if}
					</Sidebar.MenuItem>
				</Sidebar.Menu>
			</Sidebar.GroupContent>
		</Sidebar.Group>
	{/if}
</Sidebar.Content>
