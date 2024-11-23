<script lang="ts">
	import * as Sidebar from "$lib/components/ui/sidebar";
	import {SquareActivity} from "lucide-svelte";
	import House from "lucide-svelte/icons/house";
	import {CLIENT_ROUTES} from "$lib/config/paths";

	const navigations = [
				{
					title: "Home",
					url: CLIENT_ROUTES.HOME_PAGE.path,
					icon: House
				},
				{
					title: "Monitoring",
					url: CLIENT_ROUTES.MONITORING_PAGE.path,
					icon: SquareActivity,
				},
			];


</script>

<Sidebar.Menu>
	{#each navigations as item (item.title)}
		<Sidebar.MenuItem>
			<Sidebar.MenuButton>
				{#snippet child({ props })}
					<a href={item.url} {...props}>
						<item.icon />
						<span>{item.title}</span>
					</a>
				{/snippet}
			</Sidebar.MenuButton>
		</Sidebar.MenuItem>
	{/each}
</Sidebar.Menu>
