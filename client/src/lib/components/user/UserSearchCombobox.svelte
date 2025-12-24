<script lang="ts">
	import { Search, X, LoaderCircle, User } from '@lucide/svelte';
	import { Button } from '$lib/components/ui/button';
	import { searchUsers } from '$lib/api/admin/AdminUserController';
	import type { UserSearchResult } from '$lib/api/models';

	interface Props {
		onSelect: (user: UserSearchResult) => void;
		selectedUser?: UserSearchResult;
		placeholder?: string;
		disabled?: boolean;
	}

	let {
		onSelect,
		selectedUser = undefined,
		placeholder = 'Search for a user by name or email...',
		disabled = false
	}: Props = $props();

	let searchQuery: string = $state('');
	let debouncedQuery: string = $state('');
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;
	let isSearching: boolean = $state(false);
	let searchResults: UserSearchResult[] = $state([]);
	let showDropdown: boolean = $state(false);
	let inputElement: HTMLInputElement | undefined = $state(undefined);
	let dropdownElement: HTMLDivElement | undefined = $state(undefined);

	// Debounce search query
	$effect(() => {
		if (debounceTimer) clearTimeout(debounceTimer);

		if (searchQuery.length >= 3) {
			debounceTimer = setTimeout(() => {
				debouncedQuery = searchQuery;
			}, 300);
		} else {
			debouncedQuery = '';
			searchResults = [];
		}

		return () => {
			if (debounceTimer) clearTimeout(debounceTimer);
		};
	});

	// Perform search when debounced query changes
	$effect(() => {
		if (debouncedQuery.length >= 3) {
			performSearch();
		}
	});

	async function performSearch(): Promise<void> {
		isSearching = true;
		showDropdown = true;

		try {
			const results: UserSearchResult[] = await searchUsers(debouncedQuery);
			searchResults = results;
		} catch (error) {
			console.error('Search error:', error);
			searchResults = [];
		} finally {
			isSearching = false;
		}
	}

	function handleSelectUser(user: UserSearchResult): void {
		onSelect(user);
		searchQuery = '';
		searchResults = [];
		showDropdown = false;
	}

	function handleClear(): void {
		onSelect(undefined as unknown as UserSearchResult);
		searchQuery = '';
		searchResults = [];
		showDropdown = false;
	}

	function handleInputFocus(): void {
		if (searchQuery.length >= 3) {
			showDropdown = true;
		}
	}

	function handleClickOutside(event: MouseEvent): void {
		const target: Node = event.target as Node;
		if (
			inputElement &&
			dropdownElement &&
			!inputElement.contains(target) &&
			!dropdownElement.contains(target)
		) {
			showDropdown = false;
		}
	}

	// Click outside handler
	$effect(() => {
		if (typeof document !== 'undefined') {
			document.addEventListener('click', handleClickOutside);
			return () => {
				document.removeEventListener('click', handleClickOutside);
			};
		}
	});
</script>

<div class="relative">
	{#if selectedUser}
		<!-- Selected User Display -->
		<div
			class="flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-background/50 backdrop-blur-sm"
		>
			<div
				class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20"
			>
				<User class="h-5 w-5 text-primary" />
			</div>
			<div class="flex-1 min-w-0">
				<p class="text-sm font-medium truncate">
					{selectedUser.firstName} {selectedUser.lastName}
				</p>
				<p class="text-xs text-muted-foreground truncate">{selectedUser.email}</p>
			</div>
			<Button
				variant="ghost"
				size="sm"
				onclick={handleClear}
				{disabled}
				class="h-8 w-8 p-0 hover:bg-destructive/10 hover:text-destructive transition-colors"
				title="Clear selection"
			>
				<X class="h-4 w-4" />
			</Button>
		</div>
	{:else}
		<!-- Search Input -->
		<div class="relative">
			<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
			<input
				bind:this={inputElement}
				type="text"
				bind:value={searchQuery}
				{placeholder}
				{disabled}
				onfocus={handleInputFocus}
				class="flex h-9 w-full rounded-md border border-border/50 bg-background/50 px-3 py-1 pl-9 pr-10 text-sm shadow-sm transition-all file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
			/>
			{#if isSearching}
				<LoaderCircle
					class="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 animate-spin text-primary"
				/>
			{:else if searchQuery.length > 0}
				<button
					type="button"
					onclick={() => {
						searchQuery = '';
						searchResults = [];
						showDropdown = false;
					}}
					class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
					aria-label="Clear search"
				>
					<X class="h-4 w-4" />
				</button>
			{/if}
		</div>

		<!-- Dropdown -->
		{#if showDropdown}
			<div
				bind:this={dropdownElement}
				class="absolute z-50 w-full mt-2 rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl overflow-hidden"
			>
				{#if searchQuery.length > 0 && searchQuery.length < 3}
					<!-- Hint message -->
					<div class="p-4 text-center">
						<p class="text-sm text-muted-foreground">
							Type at least 3 characters to search
						</p>
					</div>
				{:else if isSearching}
					<!-- Loading state -->
					<div class="p-4 flex items-center justify-center gap-2">
						<LoaderCircle class="h-4 w-4 animate-spin text-primary" />
						<p class="text-sm text-muted-foreground">Searching...</p>
					</div>
				{:else if searchResults.length === 0 && debouncedQuery.length >= 3}
					<!-- No results -->
					<div class="p-4 text-center">
						<User class="h-8 w-8 mx-auto mb-2 text-muted-foreground/50" />
						<p class="text-sm text-muted-foreground">No users found</p>
					</div>
				{:else if searchResults.length > 0}
					<!-- Results list -->
					<div class="max-h-[300px] overflow-y-auto">
						{#each searchResults as user (user.id)}
							<button
								type="button"
								onclick={() => handleSelectUser(user)}
								class="w-full p-3 flex items-center gap-3 hover:bg-accent/10 transition-colors border-b border-border/30 last:border-0"
							>
								<div
									class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
								>
									<User class="h-5 w-5 text-primary" />
								</div>
								<div class="flex-1 min-w-0 text-left">
									<p class="text-sm font-medium truncate">
										{user.firstName} {user.lastName}
									</p>
									<p class="text-xs text-muted-foreground truncate">{user.email}</p>
								</div>
							</button>
						{/each}
					</div>
				{/if}
			</div>
		{/if}
	{/if}
</div>
