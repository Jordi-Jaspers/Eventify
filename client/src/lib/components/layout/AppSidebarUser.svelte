<!--
  AppSidebarUser Component

  User footer for sidebar with avatar, name, email, and dropdown menu.

  Props:
  - None (uses currentUser from auth store)

  Usage:
  <AppSidebarUser />
-->
<script lang="ts">
	import { goto } from '$app/navigation';
	import { authStore, currentUser } from '$lib/stores/auth';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { ChevronsUpDown, User, LogOut } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';

	async function handleLogout(): Promise<void> {
		try {
			await authStore.logout();
			await goto(CLIENT_ROUTES.LANDING_PAGE.path);
		} catch (error: unknown) {
			const { message }: { message: string } = handleError(
				error,
				'Logout failed. Please try again.'
			);
			toast.error(message);
		}
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
</script>

<Sidebar.Footer>
	<Sidebar.Menu>
		<Sidebar.MenuItem>
			<DropdownMenu.Root>
				<DropdownMenu.Trigger>
					{#snippet child({ props })}
						<Sidebar.MenuButton
							{...props}
							size="lg"
							class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
						>
							<!-- Avatar -->
							<div
								class="flex aspect-square size-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent text-primary-foreground font-semibold text-sm"
							>
								{userInitials}
							</div>
							<div class="grid flex-1 text-left text-sm leading-tight">
								<span class="truncate font-semibold">{userName}</span>
								<span class="truncate text-xs text-muted-foreground">{userEmail}</span>
							</div>
							<ChevronsUpDown class="ml-auto size-4" />
						</Sidebar.MenuButton>
					{/snippet}
				</DropdownMenu.Trigger>
			<DropdownMenu.Content
				class="min-w-56 rounded-lg bg-card/95 backdrop-blur-xl border-border/50"
				side="right"
				align="end"
				sideOffset={4}
			>
					<DropdownMenu.Item
						class="cursor-pointer hover:bg-primary/10"
						onclick={() => goto(CLIENT_ROUTES.PROFILE_PAGE.path)}
					>
						<User class="mr-2 h-4 w-4" />
						<span>Profile</span>
					</DropdownMenu.Item>
					<DropdownMenu.Separator />
					<DropdownMenu.Item
						class="cursor-pointer hover:bg-destructive/10 hover:text-destructive focus:bg-destructive/10 focus:text-destructive"
						onclick={handleLogout}
					>
						<LogOut class="mr-2 h-4 w-4" />
						<span>Log out</span>
					</DropdownMenu.Item>
				</DropdownMenu.Content>
			</DropdownMenu.Root>
		</Sidebar.MenuItem>
	</Sidebar.Menu>
</Sidebar.Footer>
