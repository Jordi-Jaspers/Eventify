<script lang="ts">
	import type { Snippet } from 'svelte';
	import '../app.css';
	import favicon from '$lib/assets/favicon.svg';
    import { ModeWatcher } from 'mode-watcher';
	import { Toaster } from 'svelte-sonner';
	import { authStore } from '$lib/stores/auth';
	import { onMount } from 'svelte';

	interface Props {
		children: Snippet;
	}

	let { children }: Props = $props();
	onMount(() => {
		authStore.initializeFromToken();
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<ModeWatcher defaultTheme={'dark'} />
<Toaster richColors />
{@render children()}
