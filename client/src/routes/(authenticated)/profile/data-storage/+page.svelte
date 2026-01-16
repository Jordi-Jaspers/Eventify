<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/state';
	import { DataRetentionSettings } from '$lib/components/settings';
	import { SettingsNav } from '$lib/components/settings';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import type { RetentionSettingsResponse } from '$lib/api/models';

	let loading: boolean = $state(true);
	let saving: boolean = $state(false);
	let retentionDays: number = $state(365);

	const currentPath: string = $derived(page.url.pathname);

	// Load retention settings
	async function loadSettings(): Promise<void> {
		loading = true;
		try {
			const response: Response = await fetch('/api/v1/user/settings/retention', {
				credentials: 'include'
			});

			if (!response.ok) {
				throw new Error('Failed to load retention settings');
			}

			const data: RetentionSettingsResponse = await response.json();
			retentionDays = data.retentionDays ?? 365;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to load retention settings'
			);
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	// Save retention settings
	async function handleSave(days: number): Promise<void> {
		saving = true;
		try {
			const response: Response = await fetch('/api/v1/user/settings/retention', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json'
				},
				credentials: 'include',
				body: JSON.stringify({ retentionDays: days })
			});

			if (!response.ok) {
				throw new Error('Failed to update retention settings');
			}

			const data: RetentionSettingsResponse = await response.json();
			retentionDays = data.retentionDays ?? days;
			toast.success('Retention settings updated successfully');
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to update retention settings'
			);
			toast.error(message);
		} finally {
			saving = false;
		}
	}

	// Load settings on mount
	$effect(() => {
		if (browser) {
			loadSettings();
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
			initialRetentionDays={retentionDays}
			{loading}
			{saving}
			onSave={handleSave}
		/>
	</div>
</main>
