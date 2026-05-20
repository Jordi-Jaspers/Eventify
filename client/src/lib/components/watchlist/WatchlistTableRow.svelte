<script lang="ts">
	import type { WatchlistDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { ClipboardList, Edit, Trash2, Eye } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { truncateText } from '$lib/utils/string';

	interface Props {
		watchlist: WatchlistDetailsResponse;
		onMonitor: (watchlist: WatchlistDetailsResponse) => void;
		onEdit: (watchlist: WatchlistDetailsResponse) => void;
		onDelete: (watchlist: WatchlistDetailsResponse) => void;
	}

	let { watchlist, onMonitor, onEdit, onDelete }: Props = $props();
</script>

<div
	class="grid grid-cols-1 md:grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all text-left w-full"
>
	<!-- Watchlist Name -->
	<div class="col-span-1 md:col-span-3">
		<div class="flex items-center gap-3">
			<ClipboardList class="h-5 w-5 text-primary shrink-0" />
			<div class="min-w-0">
				<div class="font-medium truncate">{watchlist.name}</div>
				<div class="text-sm text-muted-foreground truncate md:hidden">
					{truncateText(watchlist.description, 40, 'No description')}
				</div>
			</div>
		</div>
	</div>

	<!-- Description (desktop only) -->
	<div class="hidden md:flex md:col-span-6 items-center">
		<span class="text-sm text-muted-foreground truncate">
			{truncateText(watchlist.description, 120, 'No description')}
		</span>
	</div>

	<!-- Created -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<span class="text-sm text-muted-foreground whitespace-nowrap">
			<span class="md:hidden">Created: </span>
			{formatDate(watchlist.createdAt ?? '')}
		</span>
	</div>

	<!-- Actions -->
	<div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-primary"
			onclick={() => onMonitor(watchlist)}
			aria-label="Monitor watchlist"
		>
			<Eye class="h-4 w-4" />
		</Button>
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-primary"
			onclick={() => onEdit(watchlist)}
			aria-label="Edit watchlist"
		>
			<Edit class="h-4 w-4" />
		</Button>
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-destructive"
			onclick={() => onDelete(watchlist)}
			aria-label="Delete watchlist"
		>
			<Trash2 class="h-4 w-4" />
		</Button>
	</div>
</div>
