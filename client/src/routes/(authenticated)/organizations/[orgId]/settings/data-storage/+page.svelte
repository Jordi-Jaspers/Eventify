<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { DataRetentionSettings } from '$lib/components/settings';
	import { toast } from 'svelte-sonner';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createRetentionService } from '$lib/api/settings/service/RetentionService.svelte';
	import { createAdminOrgContext } from '$lib/api/organization/service/AdminOrgContext.svelte';

	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));
	const ctx = createAdminOrgContext(() => orgId);

	const canManage: boolean = $derived(ctx.canManage);
	const orgName: string = $derived(ctx.orgName);

	// Create retention service - recreate when orgId changes
	// Initialize with 0; the $effect below will set the correct orgId
	let retentionService = $state(createRetentionService('organization', 0));
	let lastServiceOrgId: number = $state(0);

	// Redirect if not authorized
	$effect(() => {
		if (browser && !retentionService.loading && !canManage && orgId > 0) {
			toast.error('You do not have permission to access this page');
			goto(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(orgId).path);
		}
	});

	// Load settings when orgId or canManage changes
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastServiceOrgId && currentOrgId > 0 && canManage) {
			retentionService = createRetentionService('organization', currentOrgId);
			retentionService.loadSettings();
			lastServiceOrgId = currentOrgId;
		}
	});
</script>

<svelte:head>
	<title>Data & Storage - {orgName} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-2xl mx-auto space-y-6 animate-fade-in">
		{#if canManage}
			<DataRetentionSettings
				initialRetentionDays={retentionService.retentionDays}
				loading={retentionService.loading}
				saving={retentionService.saving}
				onSave={retentionService.saveSettings}
			/>
		{/if}
	</div>
</main>
