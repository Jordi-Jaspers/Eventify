<script lang="ts">
	import { Eye, EyeOff } from '@lucide/svelte';
	import { Input } from '$lib/components/ui/input';

	let {
		id,
		placeholder = 'Enter your password',
		value = $bindable(''),
		disabled = false,
		required = false,
		ariaInvalid = false,
		ariaDescribedby = undefined,
		class: className = ''
	}: {
		id: string;
		placeholder?: string;
		value?: string;
		disabled?: boolean;
		required?: boolean;
		ariaInvalid?: boolean;
		ariaDescribedby?: string | undefined;
		class?: string;
	} = $props();

	let show: boolean = $state(false);
</script>

<div class="relative">
	<Input
		{id}
		type={show ? 'text' : 'password'}
		{placeholder}
		bind:value
		{disabled}
		{required}
		aria-invalid={ariaInvalid || undefined}
		aria-describedby={ariaDescribedby}
		class="pr-10 bg-background/50 border-border transition-all focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-primary/20 {className}"
	/>
	<button
		type="button"
		onclick={() => (show = !show)}
		{disabled}
		class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm p-0.5"
		aria-label={show ? 'Hide password' : 'Show password'}
		tabindex="0"
	>
		{#if show}
			<EyeOff class="h-4 w-4" />
		{:else}
			<Eye class="h-4 w-4" />
		{/if}
	</button>
</div>
