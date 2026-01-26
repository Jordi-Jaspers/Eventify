<script lang="ts">
	import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '$lib/components/ui/sheet';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Radio, Search } from '@lucide/svelte';
	import type { ChannelDetailsResponse } from '$lib/api/models';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		availableChannels: ChannelDetailsResponse[];
		onSelect: (channel: ChannelDetailsResponse) => void;
	}

	let { open = $bindable(), onOpenChange, availableChannels, onSelect }: Props = $props();

	let searchQuery: string = $state('');

	const filteredChannels: ChannelDetailsResponse[] = $derived(
		availableChannels.filter((ch: ChannelDetailsResponse) =>
			ch.name.toLowerCase().includes(searchQuery.toLowerCase())
		)
	);

	function handleSelect(channel: ChannelDetailsResponse): void {
		onSelect(channel);
		searchQuery = '';
		onOpenChange(false);
	}
</script>

<Sheet {open} {onOpenChange}>
	<SheetContent side="right" class="sm:max-w-md bg-card/95 backdrop-blur-xl border-border/50">
		<SheetHeader>
			<SheetTitle class="flex items-center gap-2">
				<Radio class="h-5 w-5 text-primary" />
				Select Channel
			</SheetTitle>
			<SheetDescription>
				Choose a channel to add to your watchlist
			</SheetDescription>
		</SheetHeader>

		<div class="mt-6 flex flex-col h-[calc(100vh-200px)] overflow-hidden">
			<!-- Search -->
			<div class="relative mb-4 shrink-0">
				<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
				<Input
					bind:value={searchQuery}
					placeholder="Search channels..."
					class="pl-9 bg-background border-border focus-visible:ring-primary"
				/>
			</div>

			<!-- Channel List -->
			<div class="space-y-2 flex-1 overflow-y-auto pr-1">
				{#if filteredChannels.length === 0}
					<div class="text-center py-8">
						<p class="text-sm text-muted-foreground">No channels found</p>
					</div>
				{:else}
					{#each filteredChannels as channel (channel.id)}
						<button
							onclick={() => handleSelect(channel)}
							class="
								w-full text-left rounded-lg border border-border/50 bg-card/50
								p-4 hover:border-primary/50 hover:bg-card/80 transition-all
								flex items-center gap-3
								focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2
							"
						>
							<div class="p-2 rounded-md bg-primary/10 shrink-0">
								<Radio class="h-5 w-5 text-primary" />
							</div>
							<div class="flex-1 min-w-0">
								<p class="font-medium text-sm truncate text-foreground">{channel.name}</p>
								{#if channel.description}
									<p class="text-xs text-muted-foreground truncate">{channel.description}</p>
								{/if}
							</div>
						</button>
					{/each}
				{/if}
			</div>
		</div>
	</SheetContent>
</Sheet>
