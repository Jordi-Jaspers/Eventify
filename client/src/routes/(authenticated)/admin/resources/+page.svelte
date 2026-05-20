<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	onMount((): void => {
		goto(CLIENT_ROUTES.ADMIN_USERS_PAGE.path, { replaceState: true });
	});
</script>
