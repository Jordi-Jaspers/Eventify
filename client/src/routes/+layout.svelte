<script lang="ts">
	import type { Snippet } from 'svelte';
	import '../app.css';
	import faviconProduction from '$lib/assets/favicon.svg';
	import faviconDev from '$lib/assets/favicon-dev.svg';
	import faviconTest from '$lib/assets/favicon-tst.svg';
    import { ModeWatcher } from 'mode-watcher';
	import { Toaster } from 'svelte-sonner';
	import { authStore } from '$lib/stores/auth';
	import { getEnvironment } from '$lib/config/env';
	import { onMount } from 'svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
	
	// Select favicon based on environment
	const currentFavicon: string = $derived.by(() => {
		const environment: string = getEnvironment();
		if (environment === 'local') return faviconDev;
		if (environment === 'test') return faviconTest;
		return faviconProduction;
	});
	
	onMount(() => {
		authStore.initializeFromToken();
	});
</script>

<svelte:head>
	<link rel="icon" href={currentFavicon} />
</svelte:head>

<ModeWatcher defaultTheme={'dark'} />
<Toaster richColors />
{@render children()}
