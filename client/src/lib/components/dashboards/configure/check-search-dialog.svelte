<script lang="ts">
	import { Check, Loader2, Search } from 'lucide-svelte';
	import { cn, debounce } from '$lib/utils';
	import { CLIENT_ROUTES } from '$lib/config/paths';
	import { Exception } from '$lib/models/exception.error';
	import { toast } from 'svelte-sonner';
	import { Button } from '$lib/components/ui/button';
	import { Dialog, DialogContent, DialogTrigger } from '$lib/components/ui/dialog';
	import { Input } from '$lib/components/ui/input';
	import type { Snippet } from 'svelte';

	let open = $state(false);
	let searchTerm = $state('');
	let isLoading = $state(false);
	let selectedIndex = $state(-1);
	let searchResults: CheckResponse[] = $state([]);

	let {
		children,
		variant = 'default',
		allConfiguredChecks = [],
		onCheckSelect
	} = $props<{
		children?: Snippet;
		variant?: 'default' | 'outline' | 'ghost';
		allConfiguredChecks: CheckResponse[];
		onCheckSelect?: (check: CheckResponse) => void;
	}>();

	const debouncedSearch = debounce((term: string) => performSearch(term), 500);

	async function performSearch(term: string) {
		if (term.length < 2) {
			searchResults = [];
			return;
		}

		isLoading = true;
		const url: string = CLIENT_ROUTES.PROXY_CHECK_SEARCH.path + '?' + new URLSearchParams({ q: term });
		const response: Response = await fetch(url, { method: 'GET' });

		if (!response.ok) {
			const exception: Exception = new Exception(response, await response.json());
			toast.error(exception.message);
			searchResults = [];
			return;
		}

		const data: PageResponse<CheckResponse> = await response.json();
		searchResults = data.content;
		selectedIndex = -1;
		isLoading = false;
	}

	function isCheckConfigured(check: CheckResponse): boolean {
		return allConfiguredChecks.some((configuredCheck: CheckResponse) => configuredCheck.id === check.id);
	}

	function handleSelect(check: CheckResponse) {
		if (!isCheckConfigured(check)) {
			onCheckSelect?.(check);
			open = false;
			searchTerm = '';
			searchResults = [];
		}
	}

	function handleOpenChange(isOpen: boolean) {
		open = isOpen;
		if (!isOpen) {
			handleClose();
		}
	}

	function handleClose() {
		open = false;
		searchTerm = '';
		searchResults = [];
		selectedIndex = -1;
	}

	function handleKeydown(event: KeyboardEvent) {
		if (!searchResults.length) return;

		switch (event.key) {
			case 'ArrowDown':
				event.preventDefault();
				selectedIndex = (selectedIndex + 1) % searchResults.length;
				break;
			case 'ArrowUp':
				event.preventDefault();
				selectedIndex = selectedIndex <= 0 ? searchResults.length - 1 : selectedIndex - 1;
				break;
			case 'Enter':
				if (selectedIndex >= 0) {
					handleSelect(searchResults[selectedIndex]);
				}
				break;
			case 'Escape':
				handleClose();
				break;
		}
	}

	$effect(() => {
		debouncedSearch(searchTerm);
	});
</script>

<Dialog bind:open onOpenChange={handleOpenChange}>
	<DialogTrigger>
		<Button
			class="h-8"
			size="sm"
			{variant}
			onclick={(e) => {
				e.stopPropagation();
				open = true;
			}}
		>
			{@render children()}
		</Button>
	</DialogTrigger>
	<DialogContent class="p-0 sm:max-w-[525px]">
		<div class="rounded-lg border bg-background shadow-md">
			<div class="flex items-center px-2" data-command-input-wrapper="">
				<Search class="mr-2 size-4 shrink-0 opacity-50" />
				<Input
					type="text"
					placeholder="Search checks..."
					class="border-0 focus-visible:ring-0 focus-visible:ring-offset-0"
					bind:value={searchTerm}
					onkeydown={handleKeydown}
				/>
			</div>
			{#if searchTerm.length >= 2}
				<div class="max-h-[300px] overflow-y-auto">
					{#if isLoading}
						<div class="p-4 text-center text-sm text-muted-foreground">
							<Loader2 class="mx-auto mb-2 h-4 w-4 animate-spin" />
							<span>Searching...</span>
						</div>
					{:else if searchResults.length === 0}
						<div class="p-4 text-center text-sm text-muted-foreground">No checks found</div>
					{:else}
						<div>
							{#each searchResults as check, index}
								{@const configured = isCheckConfigured(check)}
								<button
									class={cn(
										'w-full px-4 py-2 text-left transition-colors hover:bg-muted/50',
										'flex items-center justify-between',
										configured && 'cursor-not-allowed opacity-50',
										selectedIndex === index && 'bg-muted'
									)}
									disabled={configured}
									onclick={() => handleSelect(check)}
									onmouseenter={() => (selectedIndex = index)}
								>
									<span class="text-sm">{check.name}</span>
									{#if configured}
										<Check class="h-4 w-4 text-green-500" />
									{/if}
								</button>
							{/each}
						</div>
					{/if}
				</div>
			{/if}
		</div>
	</DialogContent>
</Dialog>
