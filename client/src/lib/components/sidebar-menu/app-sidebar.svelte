<script lang="ts">
	import NavUser from '$lib/components/sidebar-menu/nav-user.svelte';
	import NavMain from '$lib/components/sidebar-menu/nav-main.svelte';
	import TeamSwitcher from '$lib/components/sidebar-menu/team-switcher.svelte';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import type { ComponentProps } from 'svelte';

	let { ref = $bindable(null), collapsible = 'icon', ...restProps }: ComponentProps<typeof Sidebar.Root> = $props();
</script>

<Sidebar.Root bind:ref {collapsible} {...restProps}>
	<Sidebar.Header>
		<TeamSwitcher />
	</Sidebar.Header>
	<Sidebar.Content>
		<NavMain />
	</Sidebar.Content>
	<Sidebar.Footer>
		<NavUser />
	</Sidebar.Footer>
	<Sidebar.Rail />
</Sidebar.Root>
