<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { LoaderCircle } from 'lucide-svelte';

	let {
		isLoading = false,
		isDisabled = false,
		title = 'Submit',
		form = 'submit',
		children
	} = $props<{
		isLoading: boolean;
		isDisabled: boolean;
		title: string;
		form: string;
	}>();
</script>

<Button {form} type="submit" class="w-full" disabled={isDisabled}>
	{#if isLoading}
		<LoaderCircle class="h-4 w-4 animate-spin" />
	{:else}
		<span>{title}</span>
	{/if}
	{#if children}
		{@render children()}
	{/if}
</Button>
