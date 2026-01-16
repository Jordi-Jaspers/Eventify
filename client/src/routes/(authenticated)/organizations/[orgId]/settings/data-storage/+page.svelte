<script lang="ts">
	import { browser } from '$app/environment';
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { DataRetentionSettings } from '$lib/components/settings';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import type {
		RetentionSettingsResponse,
		UserOrganizationResponse,
		OrganizationResponse
	} from '$lib/api/models';

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

	let loading: boolean = $state(true);
	let saving: boolean = $state(false);
	let retentionDays: number = $state(365);

	// Redirect if not authorized
	$effect(() => {
		if (browser && !loading && !canManage && orgId > 0) {
			toast.error('You do not have permission to access this page');
			goto(CLIENT_ROUTES.ORGANIZATION_DASHBOARD_PAGE(orgId).path);
		}
	});

	// Load retention settings
	async function loadSettings(): Promise<void> {
		if (!canManage) {
			loading = false;
			return;
		}

		loading = true;
		try {
			const response: Response = await fetch(`/api/v1/organization/${orgId}/settings/retention`, {
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
			const response: Response = await fetch(`/api/v1/organization/${orgId}/settings/retention`, {
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

	// Load settings when orgId or canManage changes
	let lastLoadedOrgId: number = $state(0);
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		if (currentOrgId !== lastLoadedOrgId && currentOrgId > 0 && canManage) {
			loadSettings();
			lastLoadedOrgId = currentOrgId;
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
				initialRetentionDays={retentionDays}
				{loading}
				{saving}
				onSave={handleSave}
			/>
		{/if}
	</div>
</main>
