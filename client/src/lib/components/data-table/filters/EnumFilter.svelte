<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import type { FilterOption } from '../types';

	interface Props {
		value: string | null;
		options: FilterOption[];
		onChange: (value: string | null) => void;
		placeholder?: string;
	}

	let { value = $bindable(null), options, onChange, placeholder = 'All' }: Props = $props();

	function handleChange(newValue: string | null): void {
		onChange(newValue);
	}
</script>

<div class="flex gap-2 flex-wrap">
	<Button
		variant={value === null ? 'default' : 'outline'}
		size="sm"
		onclick={() => handleChange(null)}
		class={value !== null ? 'bg-background/50 border-border/50' : ''}
	>
		{placeholder}
	</Button>

	{#each options as option}
		<Button
			variant={value === option.value ? 'default' : 'outline'}
			size="sm"
			onclick={() => handleChange(option.value)}
			class={value !== option.value ? 'bg-background/50 border-border/50' : ''}
		>
			{option.label}
		</Button>
	{/each}
</div>
