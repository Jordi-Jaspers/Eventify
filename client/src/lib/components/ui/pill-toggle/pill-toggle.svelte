<script lang="ts">
	interface Props {
		items: { value: string; label: string }[];
		active: string;
		onSelect: (value: string) => void;
		size?: 'sm' | 'md';
	}
	let { items, active, onSelect, size = 'sm' }: Props = $props();

	const sizeClasses: Record<'sm' | 'md', string> = {
		sm: 'px-3 py-1 text-xs',
		md: 'px-4 py-1.5 text-sm'
	};
</script>

<div class="flex items-center gap-1 bg-muted/40 rounded-full p-1 border border-border/50 w-fit">
	{#each items as item (item.value)}
		<button
			class="{sizeClasses[size]} rounded-full font-medium transition-all {active === item.value
				? 'bg-primary text-primary-foreground shadow-sm'
				: 'text-muted-foreground hover:text-foreground'}"
			onclick={() => onSelect(item.value)}
		>
			{item.label}
		</button>
	{/each}
</div>
