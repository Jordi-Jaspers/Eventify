<script lang="ts">
	import { Input } from '$lib/components/ui/input';

	interface Props {
		value: number | null;
		onChange: (value: number | null) => void;
		placeholder?: string;
		min?: number;
		max?: number;
	}

	let {
		value = $bindable(null),
		onChange,
		placeholder = 'Enter number...',
		min,
		max
	}: Props = $props();

	function handleInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		const newValue: string = target.value;

		if (newValue === '' || newValue === null) {
			onChange(null);
			return;
		}

		const parsed: number = parseInt(newValue, 10);
		if (!isNaN(parsed)) {
			onChange(parsed);
		}
	}
</script>

<Input
	type="number"
	{placeholder}
	value={value ?? ''}
	oninput={handleInput}
	{min}
	{max}
	class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
/>
