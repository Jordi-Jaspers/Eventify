<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/state';
	import { NotificationSettingsPanel } from '$lib/components/notification-settings';
	import { createAdapterConfigService } from '$lib/api/notification/service/AdapterConfigService.svelte';

	const orgId = $derived(parseInt(page.params.orgId ?? '0'));
	const service = $derived.by(() => createAdapterConfigService('organization', orgId));
	const webhookConfigs = $derived(service.configs.filter((c) => !c.systemManaged));

	onMount(() => service.loadConfigs());
</script>

<svelte:head>
	<title>Notification Settings - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-8 animate-fade-in">
		<NotificationSettingsPanel
			{service}
			configs={webhookConfigs}
			connectionsDescription="Manage organization notification connections."
			scope="organization"
			{orgId}
		/>
	</div>
</main>
