<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Search } from '@lucide/svelte';

	interface Props {
		value: string;
		onChange: (value: string) => void;
		placeholder?: string;
		debounce?: boolean;
	}

	let { value = $bindable(''), onChange, placeholder = 'Search...', debounce = false }: Props = $props();

	let debounceTimer: ReturnType<typeof setTimeout> | null = null;

	function handleInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		const newValue: string = target.value;

		if (debounce) {
			if (debounceTimer) {
				clearTimeout(debounceTimer);
			}
			debounceTimer = setTimeout(() => {
				onChange(newValue);
			}, 300);
		} else {
			onChange(newValue);
		}
	}
</script>

<div class="relative">
	{#if debounce}
		<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
		<Input
			type="text"
			{placeholder}
			value={value}
			oninput={handleInput}
			class="pl-9 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
		/>
	{:else}
		<Input
			type="text"
			{placeholder}
			value={value}
			oninput={handleInput}
			class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
		/>
	{/if}
</div>
