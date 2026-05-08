<script lang="ts">
	import { ChevronLeft, ChevronRight } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import type { ZoomBreadcrumb } from './types';

	interface Props {
		breadcrumbs: ZoomBreadcrumb[];
		currentLabel: string;
		onNavigate: (level: number) => void;
	}

	let { breadcrumbs, currentLabel, onNavigate }: Props = $props();
</script>

<div
	class="flex items-center gap-1 text-xs text-muted-foreground px-1"
	data-testid="zoom-breadcrumb"
>
	<Button
		variant="ghost"
		size="icon"
		class="h-5 w-5"
		onclick={() => onNavigate(breadcrumbs.length - 1)}
		title="Go back"
	>
		<ChevronLeft class="h-3 w-3" />
	</Button>

	{#each breadcrumbs as crumb, i (crumb.level)}
		<button
			class="hover:text-foreground transition-colors truncate max-w-[160px]"
			onclick={() => onNavigate(crumb.level)}
			title={crumb.label}
		>
			{crumb.label}
		</button>
		<ChevronRight class="h-3 w-3 shrink-0 text-muted-foreground/50" />
	{/each}

	<span class="text-foreground/70 font-medium truncate max-w-[200px]" title={currentLabel}>
		{currentLabel}
	</span>
</div>
