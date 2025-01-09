<script lang="ts">
	import { LoadingScreen } from '$lib/components/general';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/paths';

	let { data } = $props();
	$effect(() => {
		if (!data.response) return;
		if (data.response.success) {
			toast.error(data.response.message);
			goto(CLIENT_ROUTES.LOGIN_PAGE.path);
		} else {
			toast.success('Email verified successfully! You can now login.');
			goto(CLIENT_ROUTES.APPLICATION_PAGE.path);
		}
	});
</script>

{#if !data}
	<LoadingScreen />
{/if}
