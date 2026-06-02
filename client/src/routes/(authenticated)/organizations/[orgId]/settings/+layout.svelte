<script lang="ts">
	import { page } from '$app/state';
	import { OrgSettingsNav } from '$lib/components/settings';
	import { createAdminOrgContext } from '$lib/api/organization/service/AdminOrgContext.svelte';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));
	const ctx = createAdminOrgContext(() => orgId);
	const canManage: boolean = $derived(ctx.canManage);
</script>

<svelte:head>
	<title>Organization Settings - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<OrgSettingsNav {orgId} {canManage} />

<!-- Page Content -->
{@render children()}
