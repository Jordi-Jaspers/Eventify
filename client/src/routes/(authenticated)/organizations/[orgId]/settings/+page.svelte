<script lang="ts">
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { onMount } from 'svelte';

	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));

	onMount(() => {
		goto(CLIENT_ROUTES.ORGANIZATION_SETTINGS_GENERAL_PAGE(orgId).path, { replaceState: true });
	});
</script>
