<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Label } from '$lib/components/ui/label';
	import * as Sheet from '$lib/components/ui/sheet';
	import {
		UserPlus,
		Shield,
		Search,
		X,
		User as UserIcon,
		LoaderCircle
	} from '@lucide/svelte';
	import type { OrganizationalRole, UserSearchResult } from '$lib/api/models';

	interface Props {
		open: boolean;
		searching: boolean;
		adding: boolean;
		searchQuery: string;
		searchResults: UserSearchResult[];
		selectedUser: UserSearchResult | null;
		selectedRole: OrganizationalRole;
		showSearchDropdown: boolean;
		onOpenChange: (open: boolean) => void;
		onSearchQueryChange: (query: string) => void;
		onSelectUser: (user: UserSearchResult) => void;
		onClearSelection: () => void;
		onRoleChange: (role: OrganizationalRole) => void;
		onSubmit: () => void;
		onSearchFocus: () => void;
		debouncedQueryLength: number;
	}

	let {
		open,
		searching,
		adding,
		searchQuery,
		searchResults,
		selectedUser,
		selectedRole,
		showSearchDropdown,
		onOpenChange,
		onSearchQueryChange,
		onSelectUser,
		onClearSelection,
		onRoleChange,
		onSubmit,
		onSearchFocus,
		debouncedQueryLength
	}: Props = $props();

	function handleSearchInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		onSearchQueryChange(target.value);
	}

	function handleClearSearch(): void {
		onSearchQueryChange('');
	}
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
	<Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col px-6">
		<Sheet.Header class="pt-6">
			<Sheet.Title class="flex items-center gap-2">
				<UserPlus class="h-5 w-5 text-primary" />
				Add Member
			</Sheet.Title>
			<Sheet.Description>
				Search for a user and assign them a role in the organization
			</Sheet.Description>
		</Sheet.Header>

		<div class="flex-1 space-y-4 py-6">
			<!-- Role Selector -->
			<div class="space-y-2">
				<Label>Role</Label>
				<div class="flex gap-2">
					<Button
						variant={selectedRole === 'ADMIN' ? 'default' : 'outline'}
						size="sm"
						onclick={() => onRoleChange('ADMIN')}
						disabled={adding}
						class={selectedRole === 'ADMIN'
							? 'bg-gradient-to-r from-primary to-accent'
							: 'bg-background/50 border-border/50'}
					>
						<Shield class="mr-2 h-4 w-4" />
						ADMIN
					</Button>
					<Button
						variant={selectedRole === 'MEMBER' ? 'default' : 'outline'}
						size="sm"
						onclick={() => onRoleChange('MEMBER')}
						disabled={adding}
						class={selectedRole === 'MEMBER'
							? 'bg-gradient-to-r from-primary to-accent'
							: 'bg-background/50 border-border/50'}
					>
						<UserIcon class="mr-2 h-4 w-4" />
						MEMBER
					</Button>
				</div>
			</div>

			<!-- User Search -->
			<div class="space-y-2">
				<Label for="user-search">User</Label>
				{#if selectedUser}
					<!-- Selected User Display -->
					<div
						class="flex items-center gap-3 p-3 rounded-lg border border-border/50 bg-background/50 backdrop-blur-sm"
					>
						<div
							class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20"
						>
							<UserIcon class="h-5 w-5 text-primary" />
						</div>
						<div class="flex-1 min-w-0">
							<p class="text-sm font-medium truncate">
								{selectedUser.firstName}
								{selectedUser.lastName}
							</p>
							<p class="text-xs text-muted-foreground truncate">{selectedUser.email}</p>
						</div>
						<Button
							variant="ghost"
							size="sm"
							onclick={onClearSelection}
							disabled={adding}
							class="h-8 w-8 p-0 hover:bg-destructive/10 hover:text-destructive transition-colors"
							title="Clear selection"
						>
							<X class="h-4 w-4" />
						</Button>
					</div>
				{:else}
					<!-- Search Input -->
					<div class="relative">
						<Search
							class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground"
						/>
						<input
							type="text"
							value={searchQuery}
							oninput={handleSearchInput}
							placeholder="Search by name or email (min 3 chars)..."
							disabled={adding}
							onfocus={onSearchFocus}
							class="flex h-9 w-full rounded-md border border-border/50 bg-background/50 px-3 py-1 pl-9 pr-10 text-sm shadow-sm transition-all file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary disabled:cursor-not-allowed disabled:opacity-50"
						/>
						{#if searching}
							<LoaderCircle
								class="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 animate-spin text-primary"
							/>
						{:else if searchQuery.length > 0}
							<button
								type="button"
								onclick={handleClearSearch}
								class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
								aria-label="Clear search"
							>
								<X class="h-4 w-4" />
							</button>
						{/if}
					</div>

					<!-- Search Dropdown -->
					{#if showSearchDropdown}
						<div
							class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl overflow-hidden"
						>
							{#if searchQuery.length > 0 && searchQuery.length < 3}
								<div class="p-4 text-center">
									<p class="text-sm text-muted-foreground">Type at least 3 characters to search</p>
								</div>
							{:else if searching}
								<div class="p-4 flex items-center justify-center gap-2">
									<LoaderCircle class="h-4 w-4 animate-spin text-primary" />
									<p class="text-sm text-muted-foreground">Searching...</p>
								</div>
							{:else if searchResults.length === 0 && debouncedQueryLength >= 3}
								<div class="p-4 text-center">
									<UserIcon class="h-8 w-8 mx-auto mb-2 text-muted-foreground/50" />
									<p class="text-sm text-muted-foreground">No users found</p>
								</div>
							{:else if searchResults.length > 0}
								<div class="max-h-[200px] overflow-y-auto">
									{#each searchResults as user (user.email)}
										<button
											type="button"
											onclick={() => onSelectUser(user)}
											class="w-full p-3 flex items-center gap-3 hover:bg-accent/10 transition-colors border-b border-border/30 last:border-0"
										>
											<div
												class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 border border-primary/20 flex-shrink-0"
											>
												<UserIcon class="h-5 w-5 text-primary" />
											</div>
											<div class="flex-1 min-w-0 text-left">
												<p class="text-sm font-medium truncate">
													{user.firstName}
													{user.lastName}
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
		</div>

		<Sheet.Footer class="flex-row gap-2 sm:flex-row pb-6">
			<Button
				variant="outline"
				onclick={() => onOpenChange(false)}
				disabled={adding}
				class="flex-1 bg-background/50 border-border/50"
			>
				Cancel
			</Button>
			<Button
				onclick={onSubmit}
				disabled={adding || !selectedUser}
				class="flex-1 bg-gradient-to-r from-primary to-accent hover:opacity-90"
			>
				{#if adding}
					<LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
					Adding...
				{:else}
					Add Member
				{/if}
			</Button>
		</Sheet.Footer>
	</Sheet.Content>
</Sheet.Root>
