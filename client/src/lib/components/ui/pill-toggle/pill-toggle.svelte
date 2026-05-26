<script lang="ts">
	interface Props {
		items: { value: string; label: string }[];
		active: string;
		onSelect: (value: string) => void;
	}
	let { items, active, onSelect }: Props = $props();
</script>

<div class="flex items-center gap-1 bg-muted/40 rounded-full p-1 border border-border/50 w-fit">
	{#each items as item (item.value)}
		<button
			class="px-3 py-1 rounded-full text-xs font-medium transition-all {active === item.value
				? 'bg-primary text-primary-foreground shadow-sm'
				: 'text-muted-foreground hover:text-foreground'}"
			onclick={() => onSelect(item.value)}
		>
			{item.label}
		</button>
	{/each}
</div>
