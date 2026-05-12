<script lang="ts">
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { onMount } from 'svelte';

	onMount(() => {
		goto(CLIENT_ROUTES.ADMIN_TOOLS_NOTIFICATIONS_SEND_PAGE.path, { replaceState: true });
	});
</script>
