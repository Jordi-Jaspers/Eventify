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
		<Card
			class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden"
		>
			<div
				class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"
			></div>

			<CardHeader class="relative z-10">
				<CardTitle class="text-2xl flex items-center gap-2">
					<Building2 class="w-6 h-6 text-primary" />
					Organization Details
				</CardTitle>
				<CardDescription>Your current workspace information</CardDescription>
			</CardHeader>

			<CardContent class="space-y-6 relative z-10">
				<!-- Info Grid -->
				<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
					<!-- Organization Name -->
					<div class="p-4 rounded-lg bg-background/50 border border-border/50">
						<p class="text-xs text-muted-foreground mb-1">Organization Name</p>
						<p class="font-medium text-foreground">
							{currentOrganization?.organizationName || 'N/A'}
						</p>
					</div>

					<!-- Your Role -->
					<div class="p-4 rounded-lg bg-background/50 border border-border/50">
						<div class="flex items-center gap-2 mb-1">
							<Users class="w-3 h-3 text-primary" />
							<p class="text-xs text-muted-foreground">Your Role</p>
						</div>
						{#if currentOrganization?.role}
							<Badge class={getOrganizationalRoleBadgeClass(currentOrganization.role)}>
								{currentOrganization.role}
							</Badge>
						{:else}
							<p class="font-medium text-foreground">N/A</p>
						{/if}
					</div>

					<!-- Member Since -->
					<div class="p-4 rounded-lg bg-background/50 border border-border/50 md:col-span-2">
						<div class="flex items-center gap-2 mb-1">
							<Clock class="w-3 h-3 text-primary" />
							<p class="text-xs text-muted-foreground">Member Since</p>
						</div>
						<p class="font-medium text-foreground">
							{currentOrganization?.joinedAt ? formatDate(currentOrganization.joinedAt) : 'N/A'}
						</p>
					</div>
				</div>
			</CardContent>
		</Card>

		<!-- Coming Soon Card -->
		<Card
			class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden"
		>
			<div
				class="absolute inset-0 bg-gradient-to-br from-accent/10 via-transparent to-primary/10 opacity-50"
			></div>

			<CardHeader class="relative z-10">
				<CardTitle class="text-xl flex items-center gap-2">
					<Sparkles class="w-5 h-5 text-accent" />
					Coming Soon
				</CardTitle>
				<CardDescription>Features being built for your organization</CardDescription>
			</CardHeader>

			<CardContent class="relative z-10">
				<ul class="space-y-3">
					<li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
						<div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
							<div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
						</div>
						<span class="text-sm text-muted-foreground">Event monitoring and tracking</span>
					</li>
					<li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
						<div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
							<div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
						</div>
						<span class="text-sm text-muted-foreground">Real-time notifications</span>
					</li>
					<li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
						<div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
							<div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
						</div>
						<span class="text-sm text-muted-foreground">Analytics and insights</span>
					</li>
					<li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
						<div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
							<div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
						</div>
						<span class="text-sm text-muted-foreground">Custom workflows</span>
					</li>
				</ul>
			</CardContent>
		</Card>
	</div>
</main>
