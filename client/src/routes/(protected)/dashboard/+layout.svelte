<script lang="ts">
	import * as Breadcrumb from '$lib/components/ui/breadcrumb';
	import { Separator } from '$lib/components/ui/separator';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import { SidebarMenu } from '$lib/components/sidebar';
	import { page } from '$app/stores';

	let { children } = $props();
	let basePath = $page.url.pathname.split('/')[1];
</script>

<head>
	<title>Dashboard - Eventify</title>
	<meta name="description" content="Login to your Eventify account." />
	<meta name="keywords" content="login, eventify, account, register" />
</head>

<Sidebar.Provider>
	<SidebarMenu />
	<Sidebar.Inset>
		<header
			class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12"
		>
			<div class="flex items-center gap-2 px-4">
				<Sidebar.Trigger class="-ml-1" />
				<Separator orientation="vertical" class="mr-2 h-4" />
				<Breadcrumb.Root>{basePath}</Breadcrumb.Root>
			</div>
		</header>
		<main>
			{@render children()}
		</main>
	</Sidebar.Inset>
</Sidebar.Provider>
