<script lang="ts">
	import { onMount } from 'svelte';
	import { SettingsNav } from '$lib/components/settings';
	import { AlwaysActiveChannels, NotificationSettingsPanel } from '$lib/components/notification-settings';
	import { createAdapterConfigService } from '$lib/api/notification/service/AdapterConfigService.svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { currentUser } from '$lib/stores/auth';

	const service = createAdapterConfigService('personal');
	const webhookConfigs = $derived(service.configs.filter((c) => c.adapterType !== 'EMAIL'));
	const userEmail = $derived($currentUser?.email ?? '');

	onMount(() => service.loadConfigs());
</script>

<svelte:head>
	<title>Notification Settings - Eventify</title>
</svelte:head>

<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_NOTIFICATIONS_PAGE.path} />

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-8 animate-fade-in">
		<NotificationSettingsPanel
			{service}
			configs={webhookConfigs}
			connectionsDescription="Connect external services to receive notifications."
		>
			{#snippet alwaysActiveChannels()}
				<AlwaysActiveChannels email={userEmail} />
			{/snippet}
		</NotificationSettingsPanel>
	</div>
</main>
