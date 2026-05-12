<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { TabNav } from '$lib/components/ui/tab-nav';

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

		<TabNav tabs={subTabs} ariaLabel="Notification broadcasts navigation" />
		<div class="mb-6"></div>

		{@render children()}
	</div>
</main>
