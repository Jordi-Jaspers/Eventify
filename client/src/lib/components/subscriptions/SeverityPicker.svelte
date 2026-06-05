<script lang="ts">
	import { Check } from '@lucide/svelte';
	import { cn } from '$lib/utils';

	interface Props {
		selected: string[];
		onChange: (severities: string[]) => void;
	}

	const { selected, onChange }: Props = $props();

	type SeverityValue = 'CRITICAL' | 'WARNING' | 'OK' | 'NO_DATA';

	interface SeverityDef {
		value: SeverityValue;
		label: string;
		selectedClass: string;
		unselectedClass: string;
	}

	const severities: SeverityDef[] = [
		{
			value: 'CRITICAL',
			label: 'Critical',
			selectedClass: 'bg-red-500/10 border-red-500/40 text-red-500',
			unselectedClass: 'border-border/50 text-muted-foreground hover:border-red-500/30 hover:text-red-500/80'
		},
		{
			value: 'WARNING',
			label: 'Warning',
			selectedClass: 'bg-amber-500/10 border-amber-500/40 text-amber-600',
			unselectedClass: 'border-border/50 text-muted-foreground hover:border-amber-500/30 hover:text-amber-600/80'
		},
		{
			value: 'OK',
			label: 'OK',
			selectedClass: 'bg-green-500/10 border-green-500/40 text-green-600',
			unselectedClass: 'border-border/50 text-muted-foreground hover:border-green-500/30 hover:text-green-600/80'
		},
		{
			value: 'NO_DATA',
			label: 'No Data',
			selectedClass: 'bg-muted border-border text-muted-foreground',
			unselectedClass: 'border-border/50 text-muted-foreground/60 hover:border-border hover:text-muted-foreground'
		}
	];

	function toggle(value: SeverityValue): void {
		const next = selected.includes(value)
			? selected.filter((s) => s !== value)
			: [...selected, value];
		onChange(next);
	}
</script>

<div class="flex flex-wrap gap-2" role="group" aria-label="Severity levels">
	{#each severities as sev}
		{@const isSelected = selected.includes(sev.value)}
		<button
			type="button"
			class={cn(
				'flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs font-semibold transition-all cursor-pointer select-none',
				isSelected ? sev.selectedClass : sev.unselectedClass
			)}
			onclick={() => toggle(sev.value)}
			aria-pressed={isSelected}
		>
			{#if isSelected}
				<Check class="h-3 w-3" />
			{/if}
			{sev.label}
		</button>
	{/each}
</div>
