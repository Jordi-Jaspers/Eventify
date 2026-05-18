<script lang="ts">
	import { page } from '$app/stores';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { TabNav } from '$lib/components/ui/tab-nav';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const tabs: { label: string; path: string }[] = [
		{ label: 'Notifications', path: CLIENT_ROUTES.ADMIN_TOOLS_NOTIFICATIONS_PAGE.path },
		{ label: 'Audit Log', path: CLIENT_ROUTES.ADMIN_AUDIT_LOG_PAGE.path }
	];

	const showTabs: boolean = $derived(
		tabs.some((t: { label: string; path: string }) => $page.url.pathname.startsWith(t.path))
	);
</script>

{#if showTabs}
	<TabNav {tabs} ariaLabel="Admin tools navigation" />
	<div class="mb-6"></div>
{/if}

{@render children()}
