<script lang="ts">
	import { page } from '$app/state';
	import { CalendarDays, Crown, Users } from '@lucide/svelte';
	import { formatRelativeDate } from '$lib/utils/date';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import { currentUser } from '$lib/stores/auth';
	import { createOrganizationDetailsService } from '$lib/api/organization/service/OrganizationDetailsService.svelte';
	import type { UserOrganizationResponse } from '$lib/api/models';
	import {
		OrganizationHero,
		RoleBadgeCard,
		OrganizationIdentifierCard
	} from '$lib/components/organization';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { AccessDeniedCard } from '$lib/components/ui/access-denied-card';
	import { LoadingCard } from '$lib/components/ui/loading-card';

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

	// Create service for fetching/managing organization details
	const service = $derived.by(() => createOrganizationDetailsService(orgId, organizationFromStore, isGlobalAdmin));
</script>

<svelte:head>
	<title>General Settings - {service.details.name} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-8 animate-fade-in">
		{#if service.isLoadingAdminOrg}
			<LoadingCard />
		{:else if service.hasOrgFromStore}
			<!-- User is a member of this organization -->
			<OrganizationHero name={service.details.name} isGlobalAdmin={isGlobalAdmin} />

			<!-- Stats Grid -->
			<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
				{#if service.details.role}
					<RoleBadgeCard role={service.details.role} />
				{/if}
				{#if service.details.joinedAt}
					<StatCard
						icon={CalendarDays}
						title="Member Since"
						value={formatRelativeDate(service.details.joinedAt)}
					/>
				{/if}
			</div>

			<OrganizationIdentifierCard slug={service.details.slug} />

		{:else if isGlobalAdmin && service.hasAdminFetchedOrg}
			<!-- Global admin viewing org they're not a member of -->
			<OrganizationHero name={service.details.name} isGlobalAdmin={true} />

			<!-- Stats Grid -->
			<div class="grid grid-cols-1 md:grid-cols-3 gap-6">
				<StatCard
					icon={Crown}
					title="Owner"
					value={service.details.ownerName || 'Unassigned'}
				/>
				<StatCard
					icon={Users}
					title="Members"
					value={String(service.details.memberCount ?? 0)}
				/>
				{#if service.details.createdAt}
					<StatCard
						icon={CalendarDays}
						title="Created"
						value={formatRelativeDate(service.details.createdAt)}
					/>
				{/if}
			</div>

			<OrganizationIdentifierCard slug={service.details.slug} />

		{:else}
			<AccessDeniedCard />
		{/if}
	</div>
</main>
