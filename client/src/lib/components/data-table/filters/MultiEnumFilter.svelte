<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import type { FilterOption } from '../types';

	interface Props {
		value: string[];
		options: FilterOption[];
		onChange: (value: string[]) => void;
		showAllOption?: boolean;
	}

	let {
		value = $bindable([]),
		options,
		onChange,
		showAllOption = true
	}: Props = $props();

	function isSelected(optionValue: string): boolean {
		return value.includes(optionValue);
	}

	function toggleOption(optionValue: string): void {
		if (isSelected(optionValue)) {
			const newValue: string[] = value.filter((v: string) => v !== optionValue);
			onChange(newValue);
		} else {
			const newValue: string[] = [...value, optionValue];
			onChange(newValue);
		}
	}

	function selectAll(): void {
		onChange([]);
	}

	const isAllSelected: boolean = $derived(value.length === 0);
</script>

<div class="flex gap-2 flex-wrap">
	{#if showAllOption}
		<Button
			variant={isAllSelected ? 'default' : 'outline'}
			size="sm"
			onclick={selectAll}
			class={isAllSelected
				? 'bg-gradient-to-r from-primary to-accent'
				: 'bg-background/50 border-border/50'}
		>
			All
		</Button>
	{/if}

	{#each options as option}
		<Button
			variant={isSelected(option.value) ? 'default' : 'outline'}
			size="sm"
			onclick={() => toggleOption(option.value)}
			class={isSelected(option.value)
				? 'bg-gradient-to-r from-primary to-accent'
				: 'bg-background/50 border-border/50'}
		>
			{option.label}
		</Button>
	{/each}
</div>
