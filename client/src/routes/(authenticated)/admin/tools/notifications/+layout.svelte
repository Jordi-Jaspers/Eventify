<script lang="ts">
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const subTabs: { label: string; path: string }[] = [
		{ label: 'Send', path: CLIENT_ROUTES.ADMIN_TOOLS_NOTIFICATIONS_SEND_PAGE.path },
		{ label: 'History', path: CLIENT_ROUTES.ADMIN_TOOLS_NOTIFICATIONS_HISTORY_PAGE.path }
	];
</script>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-primary">Notification Broadcasts</h1>
			<p class="text-muted-foreground mt-2">Send system-wide notifications to users</p>
		</div>

		<div class="border-b border-border/50 mb-6">
			<nav class="flex gap-1 px-1">
				{#each subTabs as tab (tab.path)}
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

		{@render children()}
	</div>
</main>
