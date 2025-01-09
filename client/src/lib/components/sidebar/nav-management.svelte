<script lang="ts">
	import { Separator, SidebarGroup, SidebarGroupLabel, SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '$lib/components/ui/sidebar';
	import { UserRoundCog, UsersRound } from 'lucide-svelte';
	import { CLIENT_ROUTES } from '$lib/config/paths';
	import { user } from '$lib/store/global.js';

	const navigations = [
		{
			title: 'User Management',
			url: CLIENT_ROUTES.USER_MANAGEMENT_PAGE.path,
			icon: UserRoundCog,
			permission: 'WRITE_USERS'
		},
		{
			title: 'Team Management',
			url: CLIENT_ROUTES.TEAM_MANAGEMENT_PAGE.path,
			icon: UsersRound,
			permission: 'WRITE_TEAMS'
		}
	];

	const allowedNavigations = $derived(user ? navigations.filter((navigation) => user.permissions.includes(navigation.permission)) : []);
</script>

{#if allowedNavigations.length > 0}
	<Separator />
	<SidebarGroup>
		<SidebarGroupLabel>Management</SidebarGroupLabel>
		<SidebarMenu>
			{#each allowedNavigations as item (item.title)}
				<SidebarMenuItem>
					<SidebarMenuButton>
						{#snippet child({ props })}
							<a href={item.url} {...props}>
								<item.icon />
								<span>{item.title}</span>
							</a>
						{/snippet}
					</SidebarMenuButton>
				</SidebarMenuItem>
			{/each}
		</SidebarMenu>
	</SidebarGroup>
{/if}
