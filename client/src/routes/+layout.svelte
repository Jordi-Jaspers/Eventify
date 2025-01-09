<script lang="ts">
	import '../app.css';

	import { Toaster } from '$lib/components/ui/sonner';
	import { ModeWatcher } from 'mode-watcher';
	import { breadcrumbs, user } from '$lib/store/global.js';

	let { data, children } = $props();

	$effect(() => {
		if (!data.user) {
			user.clear();
			return;
		}

		user.setDetails(data.user);
		if (data.paths) {
			breadcrumbs._locations = data.paths;
		}
	});
</script>

<main class="h-screen">
	<ModeWatcher defaultTheme={'dark'} />
	<Toaster />
	{@render children()}
</main>
