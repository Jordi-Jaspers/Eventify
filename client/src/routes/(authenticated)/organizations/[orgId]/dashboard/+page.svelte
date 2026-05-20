<script lang="ts">
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Building2, Users, Clock, Sparkles, Activity, Radio, AlertTriangle } from '@lucide/svelte';
	import { organizationStore } from '$lib/stores/organization.svelte';
	import type { UserOrganizationResponse, DashboardStatsResponse } from '$lib/api/models';
	import { formatDate, formatRelativeTime } from '$lib/utils/date';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import { getOrgDashboardStats, getErrorRateVariant } from '$lib/api/dashboard/DashboardController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { page } from '$app/stores';

	const currentOrganization: UserOrganizationResponse | null = $derived(
		organizationStore.currentOrganization
	);

	let stats: DashboardStatsResponse | null = $state(null);
	let statsLoading: boolean = $state(true);

	$effect(() => {
		const orgId = Number($page.params.orgId);
		if (!isNaN(orgId)) {
			loadStats(orgId);
		}
	});

	async function loadStats(orgId: number) {
		statsLoading = true;
		try {
			stats = await getOrgDashboardStats(orgId);
		} catch (err) {
			const { message } = handleError(err, 'Failed to load organization stats');
			toast.error(message);
		} finally {
			statsLoading = false;
		}
	}
</script>

<svelte:head>
	<title>{currentOrganization?.organizationName || 'Organization'} Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1
				class="text-3xl font-bold text-primary"
			>
				{currentOrganization?.organizationName || 'Organization'} Dashboard
			</h1>
			<p class="text-muted-foreground mt-2">Welcome to your organization workspace</p>
		</div>

		<!-- Stats Grid -->
		<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
			<StatCard
				title="Events Today"
				value={stats?.eventsToday?.toLocaleString() ?? '0'}
				icon={Activity}
				variant="blue"
				loading={statsLoading}
			/>
			<StatCard
				title="Active Channels"
				value={stats?.activeChannels?.toString() ?? '0'}
				icon={Radio}
				variant="purple"
				loading={statsLoading}
			/>
			<StatCard
				title="Error Rate"
				value={`${(stats?.errorRate ?? 0).toFixed(1)}%`}
				icon={AlertTriangle}
				variant={getErrorRateVariant(stats?.errorRate ?? 0)}
				loading={statsLoading}
			/>
			<StatCard
				title="Last Event"
				value={formatRelativeTime(stats?.lastEventAt)}
				icon={Clock}
				variant="primary"
				loading={statsLoading}
			/>
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
