<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';

	interface Props {
		value: boolean | null;
		onChange: (value: boolean | null) => void;
		trueLabel?: string;
		falseLabel?: string;
		allLabel?: string;
	}

	let {
		value = $bindable(null),
		onChange,
		trueLabel = 'Yes',
		falseLabel = 'No',
		allLabel = 'All'
	}: Props = $props();

	function handleChange(newValue: boolean | null): void {
		onChange(newValue);
	}
</script>

<div class="flex gap-2">
	<Button
		variant={value === null ? 'default' : 'outline'}
		size="sm"
		onclick={() => handleChange(null)}
		class={value === null
			? 'bg-gradient-to-r from-primary to-accent'
			: 'bg-background/50 border-border/50'}
	>
		{allLabel}
	</Button>

	<Button
		variant={value === true ? 'default' : 'outline'}
		size="sm"
		onclick={() => handleChange(true)}
		class={value === true
			? 'bg-gradient-to-r from-primary to-accent'
			: 'bg-background/50 border-border/50'}
	>
		{trueLabel}
	</Button>

	<Button
		variant={value === false ? 'default' : 'outline'}
		size="sm"
		onclick={() => handleChange(false)}
		class={value === false
			? 'bg-gradient-to-r from-primary to-accent'
			: 'bg-background/50 border-border/50'}
	>
		{falseLabel}
	</Button>
</div>
