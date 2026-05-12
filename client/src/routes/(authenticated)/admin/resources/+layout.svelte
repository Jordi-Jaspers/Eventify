<script lang="ts">
	import { page } from '$app/stores';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { TabNav } from '$lib/components/ui/tab-nav';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const tabs: { label: string; path: string }[] = [
		{ label: 'Users', path: CLIENT_ROUTES.ADMIN_USERS_PAGE.path },
		{ label: 'Organizations', path: CLIENT_ROUTES.ADMIN_ORGANIZATIONS_PAGE.path },
		{ label: 'API Keys', path: CLIENT_ROUTES.ADMIN_API_KEYS_PAGE.path }
	];

	const showTabs: boolean = $derived(
		tabs.some((t: { label: string; path: string }) => $page.url.pathname === t.path)
	);
</script>

{#if showTabs}
	<TabNav {tabs} ariaLabel="Admin resources navigation" />
	<div class="mb-6"></div>
{/if}

{@render children()}
