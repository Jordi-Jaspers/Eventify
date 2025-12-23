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
	import { Activity, PanelLeft } from '@lucide/svelte';

	interface Props {
		currentPath: string;
	}

	let { currentPath }: Props = $props();
</script>

<Sidebar.Sidebar collapsible="icon" class="bg-card/50 backdrop-blur-xl border-border/50">
	<!-- Header with logo and trigger -->
	<Sidebar.Header class="border-b border-border/50 !flex-row items-center justify-between">
		<!-- Logo section -->
		<div class="flex items-center gap-3">
			<div
				class="inline-flex items-center justify-center size-8 rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg"
			>
				<Activity class="size-4 text-primary-foreground" />
			</div>
			<h1
				class="text-xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent group-data-[collapsible=icon]:hidden"
			>
				Eventify
			</h1>
		</div>
		<!-- Trigger button (hidden when collapsed - Rail handles expand) -->
		<Sidebar.Trigger class="size-8 group-data-[collapsible=icon]:hidden">
			<PanelLeft class="size-4" />
			<span class="sr-only">Toggle sidebar</span>
		</Sidebar.Trigger>
	</Sidebar.Header>

	<!-- Navigation -->
	<AppSidebarNav {currentPath} />

	<!-- User footer -->
	<AppSidebarUser />

	<!-- Rail for hover-expand -->
	<Sidebar.Rail />
</Sidebar.Sidebar>
