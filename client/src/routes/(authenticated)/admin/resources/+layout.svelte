<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const tabs: { label: string; path: string }[] = [
		{ label: 'Users', path: CLIENT_ROUTES.ADMIN_USERS_PAGE.path },
		{ label: 'Organizations', path: CLIENT_ROUTES.ADMIN_ORGANIZATIONS_PAGE.path },
		{ label: 'API Keys', path: CLIENT_ROUTES.ADMIN_API_KEYS_PAGE.path }
	];

	const tabPaths: string[] = tabs.map((t) => t.path);

	const showTabs: boolean = $derived(tabPaths.some((p) => $page.url.pathname === p));
</script>

{#if showTabs}
	<div class="border-b border-border/50 mb-6">
		<nav class="flex gap-1 px-1">
			{#each tabs as tab (tab.path)}
				<button
					onclick={() => goto(tab.path)}
					class="px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px
						{$page.url.pathname === tab.path
							? 'border-primary text-primary'
							: 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'}"
				>
					{tab.label}
				</button>
			{/each}
		</nav>
	</div>
{/if}

{@render children()}
