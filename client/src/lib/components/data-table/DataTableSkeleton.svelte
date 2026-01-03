<script lang="ts">
	interface Props {
		rows?: number;
		columns: number;
	}

	let { rows = 5, columns }: Props = $props();

	// Generate varied widths for more natural look
	function getWidth(index: number): string {
		const widths: string[] = ['w-3/4', 'w-1/2', 'w-2/3', 'w-1/3', 'w-4/5'];
		return widths[index % widths.length] ?? 'w-1/2';
	}
</script>

<style>
	@keyframes shimmer {
		0% {
			background-position: -200% 0;
		}
		100% {
			background-position: 200% 0;
		}
	}

	.skeleton-shimmer {
		background: linear-gradient(
			90deg,
			hsl(var(--muted) / 0.3) 0%,
			hsl(var(--muted) / 0.5) 50%,
			hsl(var(--muted) / 0.3) 100%
		);
		background-size: 200% 100%;
		animation: shimmer 1.5s ease-in-out infinite;
	}
</style>

<div class="space-y-3">
	{#each Array(rows) as _, rowIndex}
		<div
			class="grid gap-4 p-4 rounded-lg border border-border/50 bg-card/30"
			style="grid-template-columns: repeat({columns}, minmax(0, 1fr));"
		>
			{#each Array(columns) as _, colIndex}
				{#if colIndex === 0}
					<!-- First column: avatar + text -->
					<div class="flex items-center gap-3">
						<div class="h-10 w-10 rounded-full skeleton-shimmer flex-shrink-0"></div>
						<div class="flex-1 space-y-2">
							<div class="h-4 skeleton-shimmer rounded {getWidth(rowIndex)}"></div>
							<div class="h-3 skeleton-shimmer rounded w-1/3"></div>
						</div>
					</div>
				{:else if colIndex === columns - 1}
					<!-- Last column: action button -->
					<div class="flex items-center justify-end">
						<div class="h-8 w-8 skeleton-shimmer rounded"></div>
					</div>
				{:else}
					<!-- Middle columns: varied content -->
					<div class="flex items-center">
						<div class="h-4 skeleton-shimmer rounded {getWidth(rowIndex + colIndex)}"></div>
					</div>
				{/if}
			{/each}
		</div>
	{/each}
</div>
