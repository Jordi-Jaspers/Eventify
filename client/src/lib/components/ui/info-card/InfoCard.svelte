<script lang="ts">
	interface Props {
		label: string;
		value: string;
		class?: string;
	}

	let { label, value, class: className = '' }: Props = $props();
</script>

<div class="rounded-lg border border-border/50 bg-card/30 backdrop-blur-sm p-3 {className}">
	<p class="text-xs text-muted-foreground uppercase tracking-wide">{label}</p>
	<p class="text-sm font-medium mt-1">{value}</p>
</div>
