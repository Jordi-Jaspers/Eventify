<script lang="ts">
	import { page } from '$app/state';
	import { browser } from '$app/environment';
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Building2, CalendarDays, Shield, Crown, User, Loader2, Hash, Users } from '@lucide/svelte';
	import { Badge } from '$lib/components/ui/badge';
	import { formatRelativeDate } from '$lib/utils/date';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { getOrganizationById } from '$lib/api/organization/OrganizationController';
	import type { UserOrganizationResponse, OrganizationResponse, OrganizationalRole } from '$lib/api/models';

	// Reactive orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0'));
	
	// Get organization from store - find by orgId to handle navigation
	const organizationFromStore: UserOrganizationResponse | undefined = $derived(
		organizationStore.organizations.find(
			(org: UserOrganizationResponse) => org.organizationId === orgId
		)
	);
	
	// Check if user is global admin (for users who aren't members of this org)
	const isGlobalAdmin: boolean = $derived($currentUser?.role === 'ADMIN');

	// For global admins not in the org, fetch org details from API
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let isLoadingAdminOrg: boolean = $state(false);
	let lastFetchedOrgId: number = $state(0);

	// Fetch org for global admin if not in store
	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		const needsFetch: boolean = isGlobalAdmin && !organizationFromStore && currentOrgId > 0 && currentOrgId !== lastFetchedOrgId;
		
		if (needsFetch) {
			isLoadingAdminOrg = true;
			lastFetchedOrgId = currentOrgId;
			getOrganizationById(currentOrgId)
				.then((org: OrganizationResponse | null) => {
					adminFetchedOrg = org;
				})
				.catch(() => {
					adminFetchedOrg = null;
				})
				.finally(() => {
					isLoadingAdminOrg = false;
				});
		}
	});

	// Combined org info - prefer store data, fall back to fetched data for admins
	const hasOrgFromStore: boolean = $derived(!!organizationFromStore);
	const hasAdminFetchedOrg: boolean = $derived(adminFetchedOrg !== null);
	const orgName: string = $derived.by((): string => {
		if (organizationFromStore) return organizationFromStore.organizationName ?? 'Unknown Organization';
		if (adminFetchedOrg) return adminFetchedOrg.name ?? 'Unknown Organization';
		return 'Unknown Organization';
	});
	const orgSlug: string = $derived.by((): string => {
		if (organizationFromStore) return organizationFromStore.organizationSlug ?? 'N/A';
		if (adminFetchedOrg) return adminFetchedOrg.slug ?? 'N/A';
		return 'N/A';
	});
	const orgRole: OrganizationalRole | undefined = $derived(organizationFromStore?.role);
	const joinedAt: string | undefined = $derived(organizationFromStore?.joinedAt);
	const createdAt: string | undefined = $derived.by((): string | undefined => {
		return adminFetchedOrg?.createdAt;
	});
	const ownerName: string = $derived.by((): string => {
		if (adminFetchedOrg?.owner) {
			const owner = adminFetchedOrg.owner;
			return `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim() || 'Unknown';
		}
		return '';
	});
	const memberCount: number | undefined = $derived.by((): number | undefined => {
		return adminFetchedOrg?.memberCount;
	});
</script>

<svelte:head>
	<title>General Settings - {orgName} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-8 animate-fade-in">
		{#if isLoadingAdminOrg}
			<!-- Loading state -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
				<CardContent class="py-12 flex flex-col items-center justify-center">
					<Loader2 class="w-8 h-8 animate-spin text-primary mb-3" />
					<span class="text-muted-foreground">Loading organization...</span>
				</CardContent>
			</Card>
		{:else if hasOrgFromStore}
			<!-- User is a member of this organization -->
			
			<!-- Hero Card with Organization Name -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
				<!-- Gradient overlay -->
				<div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>
				
				<CardHeader class="relative z-10 pb-8">
					<div class="flex items-center gap-3 mb-2">
						<div class="p-3 rounded-xl bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30">
							<Building2 class="w-6 h-6 text-primary" />
						</div>
						{#if isGlobalAdmin}
							<Badge variant="outline" class="text-xs">
								<Shield class="mr-1 h-3 w-3" />
								Global Admin View
							</Badge>
						{/if}
					</div>
					<CardTitle class="text-4xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
						{orgName}
					</CardTitle>
					<CardDescription class="text-base mt-2">Organization Details & Information</CardDescription>
				</CardHeader>
			</Card>

			<!-- Stats Grid -->
			<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
				<!-- Role Card -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
					<CardHeader>
						<div class="flex items-center gap-2 text-muted-foreground mb-2">
							{#if orgRole === 'OWNER'}
								<Crown class="w-4 h-4" />
							{:else if orgRole === 'ADMIN'}
								<Shield class="w-4 h-4" />
							{:else}
								<User class="w-4 h-4" />
							{/if}
							<CardDescription class="text-xs uppercase tracking-wide">Your Role</CardDescription>
						</div>
						<Badge class={`${getOrganizationalRoleBadgeClass(orgRole ?? 'MEMBER')} text-sm py-1.5 px-3 w-fit`}>
							{#if orgRole === 'OWNER'}
								<Crown class="mr-1.5 h-3.5 w-3.5" />
							{:else if orgRole === 'ADMIN'}
								<Shield class="mr-1.5 h-3.5 w-3.5" />
							{:else}
								<User class="mr-1.5 h-3.5 w-3.5" />
							{/if}
							{orgRole ?? 'MEMBER'}
						</Badge>
					</CardHeader>
				</Card>

				<!-- Joined Date Card -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
					<CardHeader>
						<div class="flex items-center gap-2 text-muted-foreground mb-2">
							<CalendarDays class="w-4 h-4" />
							<CardDescription class="text-xs uppercase tracking-wide">Member Since</CardDescription>
						</div>
						<CardTitle class="text-2xl font-semibold">
							{formatRelativeDate(joinedAt ?? '')}
						</CardTitle>
					</CardHeader>
				</Card>
			</div>

			<!-- Details Card -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
				<CardHeader>
					<CardTitle class="text-xl flex items-center gap-2">
						<Hash class="w-5 h-5 text-primary" />
						Organization Identifier
					</CardTitle>
				</CardHeader>
				<CardContent>
					<div class="flex items-center gap-3 p-4 rounded-lg bg-background/50 border border-border/50">
						<div class="p-2 rounded-lg bg-primary/10">
							<Hash class="w-4 h-4 text-primary" />
						</div>
						<div class="flex-1">
							<p class="text-xs text-muted-foreground mb-1">Slug</p>
							<p class="text-lg font-mono text-foreground">{orgSlug}</p>
						</div>
					</div>
				</CardContent>
			</Card>

		{:else if isGlobalAdmin && hasAdminFetchedOrg}
			<!-- Global admin viewing org they're not a member of -->
			
			<!-- Hero Card with Organization Name -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
				<!-- Gradient overlay -->
				<div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>
				
				<CardHeader class="relative z-10 pb-8">
					<div class="flex items-center gap-3 mb-2">
						<div class="p-3 rounded-xl bg-gradient-to-br from-primary/20 to-accent/20 border border-primary/30">
							<Building2 class="w-6 h-6 text-primary" />
						</div>
						<Badge variant="outline" class="text-xs">
							<Shield class="mr-1 h-3 w-3" />
							Global Administrator View
						</Badge>
					</div>
					<CardTitle class="text-4xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
						{orgName}
					</CardTitle>
					<CardDescription class="text-base mt-2">Organization Details & Information</CardDescription>
				</CardHeader>
			</Card>

			<!-- Stats Grid -->
			<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
				<!-- Owner Card -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
					<CardHeader>
						<div class="flex items-center gap-2 text-muted-foreground mb-2">
							<Crown class="w-4 h-4" />
							<CardDescription class="text-xs uppercase tracking-wide">Owner</CardDescription>
						</div>
						<CardTitle class="text-xl font-semibold">
							{ownerName || 'Unassigned'}
						</CardTitle>
					</CardHeader>
				</Card>

				<!-- Member Count Card -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
					<CardHeader>
						<div class="flex items-center gap-2 text-muted-foreground mb-2">
							<Users class="w-4 h-4" />
							<CardDescription class="text-xs uppercase tracking-wide">Members</CardDescription>
						</div>
						<CardTitle class="text-3xl font-bold">
							{memberCount ?? 0}
						</CardTitle>
					</CardHeader>
				</Card>

				<!-- Created Date Card -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
					<CardHeader>
						<div class="flex items-center gap-2 text-muted-foreground mb-2">
							<CalendarDays class="w-4 h-4" />
							<CardDescription class="text-xs uppercase tracking-wide">Created</CardDescription>
						</div>
						<CardTitle class="text-xl font-semibold">
							{formatRelativeDate(createdAt ?? '')}
						</CardTitle>
					</CardHeader>
				</Card>
			</div>

			<!-- Details Card -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
				<CardHeader>
					<CardTitle class="text-xl flex items-center gap-2">
						<Hash class="w-5 h-5 text-primary" />
						Organization Identifier
					</CardTitle>
				</CardHeader>
				<CardContent>
					<div class="flex items-center gap-3 p-4 rounded-lg bg-background/50 border border-border/50">
						<div class="p-2 rounded-lg bg-primary/10">
							<Hash class="w-4 h-4 text-primary" />
						</div>
						<div class="flex-1">
							<p class="text-xs text-muted-foreground mb-1">Slug</p>
							<p class="text-lg font-mono text-foreground">{orgSlug}</p>
						</div>
					</div>
				</CardContent>
			</Card>

		{:else}
			<!-- Not a member and not an admin, or org not found -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
				<CardContent class="py-12 text-center">
					<div class="flex flex-col items-center">
						<div class="p-4 rounded-2xl bg-destructive/10 border border-destructive/30 mb-4">
							<Shield class="w-10 h-10 text-destructive" />
						</div>
						<h3 class="text-lg font-semibold mb-2">Access Denied</h3>
						<p class="text-muted-foreground">You do not have access to this organization.</p>
					</div>
				</CardContent>
			</Card>
		{/if}
	</div>
</main>
