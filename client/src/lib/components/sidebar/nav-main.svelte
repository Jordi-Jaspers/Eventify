<script lang="ts">
	import { SidebarMenu, SidebarMenuButton, SidebarMenuItem } from '$lib/components/ui/sidebar';
	import { List, SquareActivity } from 'lucide-svelte';
	import { CLIENT_ROUTES } from '$lib/config/paths';

	const navigations = [
		{
			title: 'Dashboards',
			url: CLIENT_ROUTES.DASHBOARDS_PAGE.path,
			icon: List
		},
		{
			title: 'Monitoring',
			url: CLIENT_ROUTES.MONITORING_PAGE.path,
			icon: SquareActivity
		}
	];
</script>

<SidebarMenu>
	{#each navigations as item (item.title)}
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
