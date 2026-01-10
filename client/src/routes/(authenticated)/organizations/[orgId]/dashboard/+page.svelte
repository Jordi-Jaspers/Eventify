<script lang="ts">
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Building2, Users, Clock, Sparkles } from '@lucide/svelte';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import type { UserOrganizationResponse } from '$lib/api/models';
	import { formatDate } from '$lib/utils/date';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';

	const currentOrganization: UserOrganizationResponse | null = $derived(
		organizationStore.currentOrganization
	);
</script>

<svelte:head>
	<title>{currentOrganization?.organizationName || 'Organization'} Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1
				class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent"
			>
				{currentOrganization?.organizationName || 'Organization'} Dashboard
			</h1>
			<p class="text-muted-foreground mt-2">Welcome to your organization workspace</p>
		</div>

		<!-- Organization Info Card -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
			<CardHeader class="flex flex-row items-center gap-3">
				<Building2 class="h-5 w-5 text-primary" />
				<div>
					<CardTitle>Organization Details</CardTitle>
					<CardDescription>Your current workspace information</CardDescription>
				</div>
			</CardHeader>

			<CardContent class="space-y-4">
				<!-- Info Grid -->
				<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
					<!-- Organization Name -->
					<div class="space-y-2">
						<p class="text-sm text-muted-foreground">Organization Name</p>
						<p class="font-semibold text-foreground text-lg">
							{currentOrganization?.organizationName || 'N/A'}
						</p>
					</div>

					<!-- Your Role -->
					<div class="space-y-2">
						<div class="flex items-center gap-2">
							<Users class="h-4 w-4 text-primary" />
							<p class="text-sm text-muted-foreground">Your Role</p>
						</div>
						{#if currentOrganization?.role}
							<Badge class={getOrganizationalRoleBadgeClass(currentOrganization.role)}>
								{currentOrganization.role}
							</Badge>
						{:else}
							<p class="font-semibold text-foreground">N/A</p>
						{/if}
					</div>

					<!-- Member Since -->
					<div class="space-y-2 md:col-span-2">
						<div class="flex items-center gap-2">
							<Clock class="h-4 w-4 text-primary" />
							<p class="text-sm text-muted-foreground">Member Since</p>
						</div>
						<p class="font-semibold text-foreground">
							{currentOrganization?.joinedAt ? formatDate(currentOrganization.joinedAt) : 'N/A'}
						</p>
					</div>
				</div>
			</CardContent>
		</Card>

		<!-- Coming Soon Card -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
			<CardHeader class="flex flex-row items-center gap-3">
				<Sparkles class="h-5 w-5 text-primary" />
				<div>
					<CardTitle>Coming Soon</CardTitle>
					<CardDescription>Features being built for your organization</CardDescription>
				</div>
			</CardHeader>

			<CardContent>
				<ul class="space-y-3">
					<li class="flex items-center gap-3">
						<div class="flex-shrink-0 h-1.5 w-1.5 rounded-full bg-muted-foreground"></div>
						<span class="text-sm text-foreground">Event monitoring and tracking</span>
					</li>
					<li class="flex items-center gap-3">
						<div class="flex-shrink-0 h-1.5 w-1.5 rounded-full bg-muted-foreground"></div>
						<span class="text-sm text-foreground">Real-time notifications</span>
					</li>
					<li class="flex items-center gap-3">
						<div class="flex-shrink-0 h-1.5 w-1.5 rounded-full bg-muted-foreground"></div>
						<span class="text-sm text-foreground">Analytics and insights</span>
					</li>
					<li class="flex items-center gap-3">
						<div class="flex-shrink-0 h-1.5 w-1.5 rounded-full bg-muted-foreground"></div>
						<span class="text-sm text-foreground">Custom workflows</span>
					</li>
				</ul>
			</CardContent>
		</Card>
	</div>
</main>
