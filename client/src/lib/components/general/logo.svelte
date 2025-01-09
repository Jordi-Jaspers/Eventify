<script>
	import { CLIENT_ROUTES } from '$lib/config/paths.ts';
	import { Activity } from 'lucide-svelte';
</script>

<a href={CLIENT_ROUTES.LOGIN_PAGE.path} class="flex p-4">
	<Activity class="mr-2 inline-block h-6 w-6" />
	<h4>Eventify.io</h4>
</a>
