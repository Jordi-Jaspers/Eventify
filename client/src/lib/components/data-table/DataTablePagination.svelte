<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { ChevronLeft, ChevronRight } from '@lucide/svelte';

	interface Props {
		currentPage: number;
		totalPages: number;
		showingRange: string;
		hasPreviousPage: boolean;
		hasNextPage: boolean;
		onPreviousPage: () => void;
		onNextPage: () => void;
	}

	let {
		currentPage,
		totalPages,
		showingRange,
		hasPreviousPage,
		hasNextPage,
		onPreviousPage,
		onNextPage
	}: Props = $props();
</script>

{#if totalPages > 1}
	<div class="flex items-center justify-between mt-6 pt-4 border-t border-border/50">
		<div class="text-sm text-muted-foreground">{showingRange}</div>
		<div class="flex gap-2">
			<Button
				variant="outline"
				size="sm"
				onclick={onPreviousPage}
				disabled={!hasPreviousPage}
				class="bg-background/50 border-border/50 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				<ChevronLeft class="h-4 w-4" />
				Previous
			</Button>
			<Button
				variant="outline"
				size="sm"
				onclick={onNextPage}
				disabled={!hasNextPage}
				class="bg-background/50 border-border/50 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed"
			>
				Next
				<ChevronRight class="h-4 w-4" />
			</Button>
		</div>
	</div>
{/if}
