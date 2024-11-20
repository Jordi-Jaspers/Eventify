<script lang="ts">
	import * as Sidebar from "$lib/components/ui/sidebar";
	import {Sparkle, SquareActivity} from "lucide-svelte";
	import House from "lucide-svelte/icons/house";

	const navigations = [
				{
					title: "Ask AI",
					url: "#",
					icon: Sparkle
				},
				{
					title: "Home",
					url: "#",
					icon: House
				},
				{
					title: "Dashboards",
					url: "#",
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
