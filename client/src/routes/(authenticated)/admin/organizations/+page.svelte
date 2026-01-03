<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Input } from '$lib/components/ui/input';
	import { SortableTableHeader } from '$lib/components/ui/table';
	import {
		Building2,
		Search,
		ChevronLeft,
		ChevronRight,
		CircleAlert,
		Filter,
		Users
	} from '@lucide/svelte';
	import type { OrganizationStatus } from '$lib/api/models';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createOrganizationListService } from '$lib/api/organization/OrganizationListService.svelte';
	import { formatRelativeDate } from '$lib/utils/date';

	const service = createOrganizationListService(10);

	const columns = [
		{ key: 'name', label: 'Name', sortable: true, colSpan: 2 },
		{ key: 'slug', label: 'Slug', sortable: false, colSpan: 2 },
		{ key: 'status', label: 'Status', sortable: true, colSpan: 1 },
		{ key: 'owner', label: 'Owner', sortable: false, colSpan: 2 },
		{ key: 'memberCount', label: 'Members', sortable: true, colSpan: 1 },
		{ key: 'createdAt', label: 'Created', sortable: true, colSpan: 2 },
		{ key: 'actions', label: 'Actions', sortable: false, colSpan: 1 }
	];

	function navigateToMembers(orgId: number | undefined): void {
		if (orgId) {
			goto(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(orgId).path);
		}
	}

	function handleSearchInput(event: Event): void {
		const target = event.target as HTMLInputElement;
		service.setSearchQuery(target.value);
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

	function getOwnerName(owner: { firstName?: string; lastName?: string } | undefined): string {
		if (!owner || (!owner.firstName && !owner.lastName)) {
			return 'No owner';
		}
		return `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim();
	}

	onMount(() => {
		service.load();
	});
</script>

<svelte:head>
	<title>Organizations - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
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
		{#if service.error && !service.loading}
			<Alert
				variant="destructive"
				class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm"
			>
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{service.error}
					<Button variant="outline" size="sm" class="ml-4" onclick={service.load}>
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
							value={service.searchQuery}
							oninput={handleSearchInput}
							class="pl-9 bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
						/>
					</div>

					<!-- Status Filter -->
					<div class="flex gap-2 items-center">
						<Filter class="h-4 w-4 text-muted-foreground" />
						{#each [undefined, 'TRIAL', 'ACTIVE', 'SUSPENDED'] as status}
							{@const label = status ?? 'All'}
							<Button
								variant={service.selectedStatus === status ? 'default' : 'outline'}
								size="sm"
								onclick={() => service.setStatusFilter(status as OrganizationStatus | undefined)}
								class={service.selectedStatus === status
									? 'bg-gradient-to-r from-primary to-accent'
									: 'bg-background/50 border-border/50'}
							>
								{label}
							</Button>
						{/each}
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
				<CardDescription>{service.showingRange}</CardDescription>
			</CardHeader>
			<CardContent>
				{#if service.loading}
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
				{:else if service.organizations.length === 0}
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
							{#if service.searchQuery || service.selectedStatus}
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
						<SortableTableHeader
							{columns}
							currentSortKey={service.sortKey}
							currentSortDirection={service.sortDirection}
							onSort={service.setSort}
						/>

						<!-- Table Rows -->
						{#each service.organizations as org (org.slug)}
							<div
								class="grid grid-cols-1 md:grid-cols-11 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
							>
								<!-- Name -->
								<div class="col-span-1 md:col-span-2">
									<div class="flex items-center gap-2">
										<Building2 class="h-4 w-4 text-primary md:hidden" />
										<div>
											<div class="font-medium">{org.name}</div>
											<div class="text-sm text-muted-foreground md:hidden">{org.slug}</div>
										</div>
									</div>
								</div>

								<!-- Slug (desktop only) -->
								<div class="hidden md:block md:col-span-2">
									<div class="text-sm text-muted-foreground">{org.slug}</div>
								</div>

								<!-- Status -->
								<div class="col-span-1 md:col-span-1">
									<Badge variant={service.getStatusBadgeVariant(org.status)}>
										{org.status}
									</Badge>
								</div>

								<!-- Owner -->
								<div class="col-span-1 md:col-span-2">
									<div class="text-sm {org.owner ? '' : 'text-muted-foreground italic'}">
										<span class="md:hidden font-medium">Owner: </span>
										{getOwnerName(org.owner)}
									</div>
								</div>

								<!-- Members -->
								<div class="col-span-1 md:col-span-1">
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

								<!-- Actions -->
								<div class="col-span-1 md:col-span-1">
									<Button
										variant="ghost"
										size="sm"
										onclick={() => navigateToMembers(org.id)}
										class="gap-1 text-primary hover:text-primary hover:bg-primary/10"
									>
										<Users class="h-4 w-4" />
										<span class="md:hidden">Manage Members</span>
									</Button>
								</div>
							</div>
						{/each}
					</div>

					<!-- Pagination -->
					{#if service.totalPages > 1}
						<div class="flex items-center justify-between mt-6 pt-4 border-t border-border/50">
							<div class="text-sm text-muted-foreground">{service.showingRange}</div>
							<div class="flex gap-2">
								<Button
									variant="outline"
									size="sm"
									onclick={service.previousPage}
									disabled={!service.hasPreviousPage}
									class="bg-background/50 border-border/50 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed"
								>
									<ChevronLeft class="h-4 w-4" />
									Previous
								</Button>
								<Button
									variant="outline"
									size="sm"
									onclick={service.nextPage}
									disabled={!service.hasNextPage}
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
