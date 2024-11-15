<script lang="ts">
	import { EyeIcon, EyeOffIcon } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';

	let { enabled = $bindable(false) } = $props<{ enabled: boolean }>();
	function togglePrivacy() {
		enabled = !enabled;
	}
</script>

<button
	class="absolute right-3 top-1/2 -translate-y-1/2 transform text-gray-400 hover:text-gray-800 focus:outline-none"
	type="button"
	onclick={togglePrivacy}
>
	{#if enabled}
		<EyeIcon class="h-5 w-5" />
	{:else}
		<EyeOffIcon class="h-5 w-5" />
	{/if}
</button>
