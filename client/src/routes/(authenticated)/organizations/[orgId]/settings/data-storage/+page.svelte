<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { DataRetentionSettings } from '$lib/components/settings';
	import { toast } from 'svelte-sonner';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createRetentionService } from '$lib/api/settings/service/RetentionService.svelte';
	import type { UserOrganizationResponse, OrganizationResponse } from '$lib/api/models';

	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	// Get organization from store
	const organizationFromStore: UserOrganizationResponse | undefined = $derived(
		organizationStore.organizations.find(
			(org: UserOrganizationResponse) => org.organizationId === orgId
		)
	);

	// Check permissions - canManage if OWNER, ADMIN, or global ADMIN
	const isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
	const canManage: boolean = $derived.by((): boolean => {
		if (isGlobalAdmin) return true;
		if (!organizationFromStore) return false;
		const role: string | undefined = organizationFromStore.role;
		return role === 'OWNER' || role === 'ADMIN';
	});

	// For global admins not in the org, fetch org details from API
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let lastFetchedOrgId: number = $state(0);

	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		const needsFetch: boolean =
			isGlobalAdmin &&
			!organizationFromStore &&
			currentOrgId > 0 &&
			currentOrgId !== lastFetchedOrgId;

		if (needsFetch) {
			lastFetchedOrgId = currentOrgId;
			getOrganizationById(currentOrgId)
				.then((org: OrganizationResponse | null) => {
					adminFetchedOrg = org;
				})
				.catch(() => {
					adminFetchedOrg = null;
				});
		}
	});

	// Organization name for display
	const orgName: string = $derived.by((): string => {
		if (organizationFromStore) return organizationFromStore.organizationName ?? 'Organization';
		if (adminFetchedOrg) return adminFetchedOrg.name ?? 'Organization';
		return 'Organization';
	});

	// Create retention service - recreate when orgId changes
	// Initialize with 0; the $effect below will set the correct orgId
	let retentionService = $state(createRetentionService('organization', 0));
	let lastServiceOrgId: number = $state(0);

	// Redirect if not authorized
	$effect(() => {
		if (browser && !retentionService.loading && !canManage && orgId > 0) {
			toast.error('You do not have permission to access this page');
			goto(CLIENT_ROUTES.ORGANIZATION_DASHBOARD_PAGE(orgId).path);
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
