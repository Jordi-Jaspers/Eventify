<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/state';
	import { DataRetentionSettings, SettingsNav } from '$lib/components/settings';
	import { createRetentionService } from '$lib/api/settings/service/RetentionService.svelte';

	const currentPath: string = $derived(page.url.pathname);

	const retentionService = createRetentionService('user');

	// Load settings on mount
	$effect(() => {
		if (browser) {
			retentionService.loadSettings();
		}
	});
</script>

<svelte:head>
	<title>Data & Storage - Eventify</title>
</svelte:head>

<SettingsNav {currentPath} />

<main class="container mx-auto px-4 py-8">
	<div class="max-w-2xl mx-auto space-y-6 animate-fade-in">
		<DataRetentionSettings
			initialRetentionDays={retentionService.retentionDays}
			loading={retentionService.loading}
			saving={retentionService.saving}
			onSave={retentionService.saveSettings}
		/>
	</div>
</main>
