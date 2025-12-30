<script lang="ts">
	import { onMount } from 'svelte';
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Input } from '$lib/components/ui/input';
	import { SidebarTrigger } from '$lib/components/ui/sidebar';
	import { searchOrganizations } from '$lib/api/organization/OrganizationController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import {
		Building2,
		Search,
		ChevronLeft,
		ChevronRight,
		CircleAlert,
		Filter
	} from '@lucide/svelte';
	import type { OrganizationResponse, OrganizationStatus } from '$lib/api/models.ts';

	let organizations: OrganizationResponse[] = $state([]);
	let loading: boolean = $state(true);
	let error: string | null = $state(null);
	let searchQuery: string = $state('');
	let selectedStatus: OrganizationStatus | undefined = $state(undefined);
	let currentPage: number = $state(0);
	let totalPages: number = $state(0);
	let totalElements: number = $state(0);
	const pageSize: number = 10;

	let debounceTimer: ReturnType<typeof setTimeout>;

	async function loadOrganizations(): Promise<void> {
		loading = true;
		error = null;

		try {
			const response = await searchOrganizations({
				page: currentPage,
				size: pageSize,
				search: searchQuery || undefined,
				status: selectedStatus
			});

			organizations = response.content ?? [];
			totalPages = response.totalPages ?? 0;
			totalElements = response.totalElements ?? 0;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to load organizations'
			);
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function handleSearchInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		searchQuery = target.value;

		clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			currentPage = 0;
			loadOrganizations();
		}, 300);
	}

	function handleStatusFilter(status: OrganizationStatus | undefined): void {
		selectedStatus = status;
		currentPage = 0;
		loadOrganizations();
	}

	function previousPage(): void {
		if (currentPage > 0) {
			currentPage--;
			loadOrganizations();
		}
	}

	function nextPage(): void {
		if (currentPage < totalPages - 1) {
			currentPage++;
			loadOrganizations();
		}
	}

	function getStatusBadgeVariant(
		status: OrganizationStatus | undefined
	): 'default' | 'success' | 'destructive' {
		switch (status) {
			case 'ACTIVE':
				return 'success';
			case 'SUSPENDED':
				return 'destructive';
			case 'TRIAL':
			default:
				return 'default';
		}
	}

	function formatDate(dateString: string | undefined): string {
		if (!dateString) return 'N/A';
		const date: Date = new Date(dateString);
		return date.toLocaleDateString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric'
		});
	}

	function getShowingRange(): string {
		if (totalElements === 0) return 'Showing 0 organizations';

		const start: number = currentPage * pageSize + 1;
		const end: number = Math.min((currentPage + 1) * pageSize, totalElements);
		return `Showing ${start}-${end} of ${totalElements} organizations`;
	}

	onMount(() => {
		loadOrganizations();
	});
</script>

<svelte:head>
	<title>Organizations - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="mb-4">
		<SidebarTrigger />
	</div>

	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1
				class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent"
			>
				Organizations
			</h1>
			<p class="text-muted-foreground mt-2">Manage and monitor all organizations on the platform</p>
		</div>

		<!-- Error Alert -->
		{#if error && !loading}
			<Alert
				variant="destructive"
				class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
			>
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{error}
					<Button variant="outline" size="sm" class="ml-4" onclick={loadOrganizations}>
						Retry
					</Button>
				</AlertDescription>
			</Alert>
		{/if}

		<!-- Filters -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
			<CardContent class="pt-6">
				<div class="flex flex-col md:flex-row gap-4">
					<!-- Search Input -->
					<div class="flex-1 relative">
						<Search class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
						<Input
							type="text"
							placeholder="Search by name..."
							value={searchQuery}
							oninput={handleSearchInput}
							class="pl-9 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
						/>
					</div>

					<!-- Status Filter -->
					<div class="flex gap-2 items-center">
						<Filter class="h-4 w-4 text-muted-foreground" />
						<Button
							variant={selectedStatus === undefined ? 'default' : 'outline'}
							size="sm"
							onclick={() => handleStatusFilter(undefined)}
							class={selectedStatus === undefined
								? 'bg-gradient-to-r from-primary to-accent'
								: 'bg-background/50 border-border/50'}
						>
							All
						</Button>
						<Button
							variant={selectedStatus === 'TRIAL' ? 'default' : 'outline'}
							size="sm"
							onclick={() => handleStatusFilter('TRIAL')}
							class={selectedStatus === 'TRIAL'
								? 'bg-gradient-to-r from-primary to-accent'
								: 'bg-background/50 border-border/50'}
						>
							Trial
						</Button>
						<Button
							variant={selectedStatus === 'ACTIVE' ? 'default' : 'outline'}
							size="sm"
							onclick={() => handleStatusFilter('ACTIVE')}
							class={selectedStatus === 'ACTIVE'
								? 'bg-gradient-to-r from-primary to-accent'
								: 'bg-background/50 border-border/50'}
						>
							Active
						</Button>
						<Button
							variant={selectedStatus === 'SUSPENDED' ? 'default' : 'outline'}
							size="sm"
							onclick={() => handleStatusFilter('SUSPENDED')}
							class={selectedStatus === 'SUSPENDED'
								? 'bg-gradient-to-r from-primary to-accent'
								: 'bg-background/50 border-border/50'}
						>
							Suspended
						</Button>
					</div>
				</div>
			</CardContent>
		</Card>

		<!-- Organizations Table -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
			<CardHeader>
				<div class="flex items-center gap-2">
					<Building2 class="w-5 h-5 text-primary" />
					<CardTitle class="text-xl">All Organizations</CardTitle>
				</div>
				<CardDescription>{getShowingRange()}</CardDescription>
			</CardHeader>
			<CardContent>
				{#if loading}
					<!-- Loading Skeleton -->
					<div class="space-y-3">
						{#each Array(5) as _, i}
							<div class="flex items-center gap-4 p-4 rounded-lg border border-border/50 bg-card/30">
								<div class="flex-1 space-y-2">
									<div class="h-4 bg-muted/50 rounded animate-pulse w-1/4"></div>
									<div class="h-3 bg-muted/50 rounded animate-pulse w-1/3"></div>
								</div>
								<div class="h-6 w-16 bg-muted/50 rounded animate-pulse"></div>
								<div class="h-4 w-12 bg-muted/50 rounded animate-pulse"></div>
								<div class="h-4 w-24 bg-muted/50 rounded animate-pulse"></div>
							</div>
						{/each}
					</div>
				{:else if organizations.length === 0}
					<!-- Empty State -->
					<div class="flex flex-col items-center justify-center py-12">
						<div class="relative">
							<div
								class="absolute inset-0 blur-3xl bg-gradient-to-r from-primary/20 to-accent/20"
							></div>
							<div
								class="relative p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50 backdrop-blur-sm"
							>
								<Building2 class="w-12 h-12 text-primary" />
							</div>
						</div>
						<h3 class="mt-6 text-lg font-semibold">No organizations found</h3>
						<p class="mt-2 text-sm text-muted-foreground text-center max-w-sm">
							{#if searchQuery || selectedStatus}
								Try adjusting your search or filters
							{:else}
								No organizations have been created yet
							{/if}
						</p>
					</div>
				{:else}
					<!-- Organizations List -->
					<div class="space-y-2">
						<!-- Table Header (hidden on mobile) -->
						<div
							class="hidden md:grid md:grid-cols-12 gap-4 px-4 py-2 text-sm font-medium text-muted-foreground border-b border-border/50"
						>
							<div class="col-span-3">Name</div>
							<div class="col-span-3">Slug</div>
							<div class="col-span-2">Status</div>
							<div class="col-span-2">Members</div>
							<div class="col-span-2">Created</div>
						</div>

						<!-- Table Rows -->
						{#each organizations as org (org.id)}
							<div
								class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
							>
								<!-- Name -->
								<div class="col-span-1 md:col-span-3">
									<div class="flex items-center gap-2">
										<Building2 class="h-4 w-4 text-primary md:hidden" />
										<div>
											<div class="font-medium">{org.name}</div>
											<div class="text-sm text-muted-foreground md:hidden">{org.slug}</div>
										</div>
									</div>
								</div>

								<!-- Slug (desktop only) -->
								<div class="hidden md:block md:col-span-3">
									<div class="text-sm text-muted-foreground">{org.slug}</div>
								</div>

								<!-- Status -->
								<div class="col-span-1 md:col-span-2">
									<Badge variant={getStatusBadgeVariant(org.status)}>
										{org.status}
									</Badge>
								</div>

								<!-- Members -->
								<div class="col-span-1 md:col-span-2">
									<div class="text-sm">
										<span class="md:hidden text-muted-foreground">Members: </span>
										{org.memberCount}
									</div>
								</div>

								<!-- Created -->
								<div class="col-span-1 md:col-span-2">
									<div class="text-sm text-muted-foreground">
										<span class="md:hidden">Created: </span>
										{formatDate(org.createdAt)}
									</div>
								</div>
							</div>
						{/each}
					</div>

					<!-- Pagination -->
					{#if totalPages > 1}
						<div class="flex items-center justify-between mt-6 pt-4 border-t border-border/50">
							<div class="text-sm text-muted-foreground">{getShowingRange()}</div>
							<div class="flex gap-2">
								<Button
									variant="outline"
									size="sm"
									onclick={previousPage}
									disabled={currentPage === 0}
									class="bg-background/50 border-border/50 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed"
								>
									<ChevronLeft class="h-4 w-4" />
									Previous
								</Button>
								<Button
									variant="outline"
									size="sm"
									onclick={nextPage}
									disabled={currentPage >= totalPages - 1}
									class="bg-background/50 border-border/50 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed"
								>
									Next
									<ChevronRight class="h-4 w-4" />
								</Button>
							</div>
						</div>
					{/if}
				{/if}
			</CardContent>
		</Card>
	</div>
</main>
