<script lang="ts">
	import { page } from '$app/stores';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { TabNav } from '$lib/components/ui/tab-nav';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const tabs: { label: string; path: string }[] = [
		{ label: 'Notifications', path: CLIENT_ROUTES.ADMIN_TOOLS_NOTIFICATIONS_PAGE.path }
	];

	const tabPaths: string[] = tabs.map((t: { label: string; path: string }) => t.path);

	const showTabs: boolean = $derived(
		tabPaths.some((p: string) => $page.url.pathname.startsWith(p))
	);
</script>

{#if showTabs}
	<TabNav {tabs} ariaLabel="Admin tools navigation" />
	<div class="mb-6"></div>
{/if}

{@render children()}
