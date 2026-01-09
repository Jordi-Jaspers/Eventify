<script lang="ts">
	import { page } from '$app/state';
	import { OrgSettingsNav } from '$lib/components/settings';

	interface Props {
		children: import('svelte').Snippet;
	}

	let { children }: Props = $props();

	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));
	const currentPath: string = $derived(page.url.pathname);
</script>

<svelte:head>
	<title>Organization Settings - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<OrgSettingsNav {currentPath} {orgId} />

<!-- Page Content -->
{@render children()}
