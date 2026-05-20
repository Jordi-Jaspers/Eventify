<script lang="ts">
	import { page } from '$app/state';
	import { OrgSettingsNav } from '$lib/components/settings';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import type { UserOrganizationResponse } from '$lib/api/models';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

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
</script>

<svelte:head>
	<title>Organization Settings - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<OrgSettingsNav {orgId} {canManage} />

<!-- Page Content -->
{@render children()}
