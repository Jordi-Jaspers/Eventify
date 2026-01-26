<!--
  AppSidebar Component

  Main sidebar composition combining header, nav, and user components.

  Props:
  - currentPath: string - Current URL path for highlighting active items

  Usage:
  <AppSidebar currentPath={page.url.pathname} />
-->
<script lang="ts">
	import * as Sidebar from '$lib/components/ui/sidebar';
	import AppSidebarNav from './AppSidebarNav.svelte';
	import AppSidebarUser from './AppSidebarUser.svelte';
	import AppLogo from './AppLogo.svelte';
	import { PanelLeft } from '@lucide/svelte';

	interface Props {
		currentPath: string;
	}

	let { currentPath }: Props = $props();
</script>

<Sidebar.Sidebar collapsible="icon" class="bg-card/50 backdrop-blur-xl border-border/50">
	<!-- Header with logo and trigger -->
	<Sidebar.Header class="border-b border-border/50 !flex-row items-center justify-between group-data-[collapsible=icon]:justify-center">
		<!-- Logo section -->
		<AppLogo size="small" class="group-data-[collapsible=icon]:hidden" />
		<AppLogo size="small" variant="icon" class="hidden group-data-[collapsible=icon]:flex" />
		<!-- Trigger button (hidden when collapsed - Rail handles expand) -->
		<Sidebar.Trigger class="size-8 group-data-[collapsible=icon]:hidden">
			<PanelLeft class="size-4" />
			<span class="sr-only">Toggle sidebar</span>
		</Sidebar.Trigger>
	</Sidebar.Header>

	<!-- Navigation -->
	<AppSidebarNav {currentPath} />

	<!-- Combined Org + User footer -->
	<AppSidebarUser />

	<!-- Rail for hover-expand -->
	<Sidebar.Rail />
</Sidebar.Sidebar>
