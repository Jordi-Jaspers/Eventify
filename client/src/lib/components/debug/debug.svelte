<script lang="ts">
	import SuperDebug, { type Infer, type SuperValidated, superForm } from 'sveltekit-superforms';
	import { browser } from '$app/environment';

	let data: string = $props();
</script>

{#if browser}
	<SuperDebug {data} />
{/if}
