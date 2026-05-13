<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Button } from '$lib/components/ui/button';
	import { Users, Search, ChevronLeft, ChevronRight, LoaderCircle } from '@lucide/svelte';
	import type { RecipientResponse } from '$lib/api/models';
	import type { PageResource } from '$lib/api/models';

	interface Props {
		loading: boolean;
		search: string;
		data: PageResource<RecipientResponse> | null;
		onSearchInput: (value: string) => void;
		onPageChange: (page: number) => void;
	}

	let { loading, search, data, onSearchInput, onPageChange }: Props = $props();

	const content: RecipientResponse[] = $derived(data?.content ?? []);
	const pageNumber: number = $derived(data?.pageNumber ?? 0);
	const pageSize: number = $derived(data?.pageSize ?? 20);
	const totalElements: number = $derived(data?.totalElements ?? 0);
	const totalPages: number = $derived(data?.totalPages ?? 0);
</script>

<div class="border-t border-border/30 pt-3 space-y-3">
	<div class="flex items-center justify-between gap-3">
		<span class="text-xs uppercase tracking-wide text-muted-foreground flex items-center gap-1.5">
			<Users class="h-3.5 w-3.5" />
			Recipients
		</span>
		<div class="relative w-56">
			<Search class="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-muted-foreground pointer-events-none" />
			<Input
				class="pl-8 h-7 text-xs bg-background/50 border-border/50"
				placeholder="Search email or name..."
				value={search}
				oninput={(e: Event) => onSearchInput((e.target as HTMLInputElement).value)}
			/>
		</div>
	</div>

	{#if loading}
		<div class="flex items-center justify-center py-4">
			<LoaderCircle class="h-4 w-4 animate-spin text-muted-foreground" />
		</div>
	{:else if data}
		{#if content.length === 0}
			<p class="text-xs text-muted-foreground text-center py-3">No recipients found</p>
		{:else}
			<div class="max-h-48 overflow-y-auto rounded border border-border/30 divide-y divide-border/20">
				{#each content as recipient (recipient.userId)}
					<div class="flex items-center gap-3 px-3 py-2 text-xs hover:bg-muted/20">
						<span class="font-medium truncate flex-1">{recipient.email}</span>
						<span class="text-muted-foreground truncate">{recipient.name}</span>
					</div>
				{/each}
			</div>
			<div class="flex items-center justify-between text-xs text-muted-foreground">
				<span>
					Showing {pageNumber * pageSize + 1}–{Math.min((pageNumber + 1) * pageSize, totalElements)} of {totalElements}
				</span>
				<div class="flex items-center gap-1">
					<Button
						variant="ghost"
						size="icon"
						class="h-6 w-6"
						disabled={pageNumber === 0}
						onclick={() => onPageChange(pageNumber - 1)}
					>
						<ChevronLeft class="h-3.5 w-3.5" />
					</Button>
					<Button
						variant="ghost"
						size="icon"
						class="h-6 w-6"
						disabled={pageNumber >= totalPages - 1}
						onclick={() => onPageChange(pageNumber + 1)}
					>
						<ChevronRight class="h-3.5 w-3.5" />
					</Button>
				</div>
			</div>
		{/if}
	{/if}
</div>
