<script lang="ts">
	import { goto } from '$app/navigation';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import {
		CheckCircle2,
		AlertTriangle,
		Building2,
		Bell,
		ListChecks,
		Activity,
		Radio,
		Mail,
		Shield,
		ChevronRight
	} from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { formatRelativeTime } from '$lib/utils/date';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { getUserDashboard } from '$lib/api/dashboard/UserDashboardController';
	import type { UserDashboardResponse, RecentNotificationResponse, OrganizationStatusResponse, WatchlistHealthResponse } from '$lib/api/models';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';
	import { authStore } from '$lib/stores/auth';
	import { StatCard } from '$lib/components/ui/stat-card';

	let dashboard: UserDashboardResponse | null = $state(null);
	let loading: boolean = $state(true);

	const watchlistHealth: WatchlistHealthResponse[] = $derived((dashboard as UserDashboardResponse | null)?.watchlistHealth ?? []);
	const recentNotifications: RecentNotificationResponse[] = $derived((dashboard as UserDashboardResponse | null)?.recentNotifications ?? []);
	const organizations: OrganizationStatusResponse[] = $derived((dashboard as UserDashboardResponse | null)?.organizations ?? []);
	const alertCount: number = $derived(watchlistHealth.length);
	const notifCount: number = $derived(recentNotifications.length);
	const totalChannelsInAlert: number = $derived(
		organizations.reduce((sum: number, o: OrganizationStatusResponse) => sum + o.channelsInAlertCount, 0)
	);

	$effect(() => {
		loadDashboard();
	});

	async function loadDashboard(): Promise<void> {
		try {
			dashboard = await getUserDashboard();
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load dashboard');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function handleNotificationClick(notification: RecentNotificationResponse): void {
		if (notification.actionUrl) {
			goto(notification.actionUrl);
		}
	}

	function getSeverityClass(severity: string): string {
		return severity === 'CRITICAL'
			? 'bg-destructive/20 text-destructive border-destructive/50'
			: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50';
	}

	function getOrgStatusClass(status: string): string {
		return status === 'ACTIVE'
			? 'bg-green-500/20 text-green-400 border-green-500/50'
			: 'bg-muted/50 text-muted-foreground border-border/50';
	}
</script>

<svelte:head>
	<title>Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in pt-2">

		<h1 class="text-3xl font-bold text-primary">Dashboard</h1>

		<!-- Welcome card -->
		<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
			<CardContent class="py-5 flex flex-col sm:flex-row sm:items-center gap-4">
				<!-- Avatar icon -->
				<div class="shrink-0 w-11 h-11 rounded-full bg-primary/15 border border-primary/30 flex items-center justify-center">
					<Shield class="w-5 h-5 text-primary" />
				</div>

				<!-- User info -->
				<div class="flex-1 min-w-0">
					<p class="text-base font-semibold">
						Welcome back, <span class="text-primary">{$authStore.user?.firstName ?? 'there'}</span>
					</p>
					<div class="flex flex-wrap items-center gap-x-4 gap-y-1 mt-0.5">
						{#if $authStore.user?.email}
							<span class="flex items-center gap-1.5 text-xs text-muted-foreground">
								<Mail class="w-3 h-3" />
								{$authStore.user.email}
							</span>
						{/if}
					</div>
				</div>

				<!-- Role + dynamic status -->
				<div class="flex items-center gap-3 shrink-0">
					{#if $authStore.user?.role}
						<Badge variant="outline" class="text-xs">{$authStore.user.role}</Badge>
					{/if}
					{#if !loading}
						{#if alertCount > 0}
							<div class="flex items-center gap-1.5 text-xs text-destructive">
								<PulseIndicator variant="red" size="sm" />
								<span>{alertCount} {alertCount === 1 ? 'alert needs' : 'alerts need'} attention</span>
							</div>
						{:else}
							<div class="flex items-center gap-1.5 text-xs text-muted-foreground">
								<PulseIndicator variant="green" size="sm" />
								<span>All systems operational</span>
							</div>
						{/if}
					{/if}
				</div>
			</CardContent>
		</Card>

		{#if !loading}
			<!-- Quick stats row -->
			<div class="grid grid-cols-3 gap-4">
				<StatCard title="Notifications" value={notifCount} icon={Bell} variant="blue" />
				<StatCard title="Channels in alert" value={totalChannelsInAlert} icon={Radio} variant="red" />
				<StatCard title="Organizations" value={organizations.length} icon={Building2} variant="purple" />
			</div>
		{/if}

		{#if loading}
			<!-- Loading skeleton -->
			<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
				{#each Array(2) as _}
					<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
						<CardHeader>
							<div class="h-5 w-40 bg-muted/50 rounded animate-pulse"></div>
						</CardHeader>
						<CardContent class="space-y-3">
							{#each Array(3) as __}
								<div class="h-12 bg-muted/50 rounded animate-pulse"></div>
							{/each}
						</CardContent>
					</Card>
				{/each}
			</div>
		{:else}
			<!-- Top row: Watchlist Health + Recent Notifications -->
			<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

				<!-- Watchlist Health -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
					<CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
						<CardTitle class="text-lg flex items-center gap-2">
							<ListChecks class="w-5 h-5 text-primary" />
							Watchlist Health
						</CardTitle>
						<a
							href={CLIENT_ROUTES.MONITOR_PAGE.path}
							class="flex items-center gap-0.5 text-xs text-muted-foreground hover:text-primary transition-colors"
						>
							View all <ChevronRight class="w-3 h-3" />
						</a>
					</CardHeader>
					<CardContent>
						{#if watchlistHealth.length === 0}
							<div class="flex items-center gap-3 py-4 text-green-400">
								<CheckCircle2 class="w-5 h-5 shrink-0" />
								<span class="text-sm font-medium">All watchlists are healthy</span>
							</div>
						{:else}
							<ul class="space-y-2">
								{#each watchlistHealth as item (item.id)}
									<li class="flex items-center justify-between p-3 rounded-lg bg-background/50 border border-border/50">
										<div class="flex items-center gap-2 min-w-0">
											<AlertTriangle class="w-4 h-4 shrink-0 {item.severity === 'CRITICAL' ? 'text-destructive' : 'text-yellow-400'}" />
											<span class="text-sm font-medium truncate">{item.name}</span>
										</div>
										<div class="flex items-center gap-2 shrink-0 ml-2">
											<span class="text-xs text-muted-foreground">{item.channelsInAlert} ch</span>
											<Badge class="text-xs border {getSeverityClass(item.severity)}">
												{item.severity}
											</Badge>
										</div>
									</li>
								{/each}
							</ul>
						{/if}
					</CardContent>
				</Card>

				<!-- Recent Notifications -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
					<CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
						<CardTitle class="text-lg flex items-center gap-2">
							<Bell class="w-5 h-5 text-primary" />
							Recent Notifications
							{#if notifCount > 0}
								<span class="text-sm font-normal text-muted-foreground">({notifCount})</span>
							{/if}
						</CardTitle>
					</CardHeader>
					<CardContent>
						{#if recentNotifications.length === 0}
							<div class="py-4 text-center text-sm text-muted-foreground">
								No recent activity in the last 24h
							</div>
						{:else}
							<ul class="space-y-2">
								{#each recentNotifications as notif (notif.id)}
									<li>
										<button
											onclick={() => handleNotificationClick(notif)}
											class="w-full text-left p-3 rounded-lg bg-background/50 border border-border/50 hover:bg-muted/30 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background {notif.actionUrl ? 'cursor-pointer' : 'cursor-default'}"
											aria-label={notif.title}
										>
											<div class="flex items-start justify-between gap-2">
												<div class="min-w-0 flex-1">
													<div class="flex items-center gap-2">
														{#if notif.urgent}
															<PulseIndicator variant="red" size="sm" />
														{/if}
														<span class="text-sm font-medium truncate">{notif.title}</span>
													</div>
													<p class="text-xs text-muted-foreground mt-0.5 line-clamp-1">{notif.message}</p>
												</div>
												<div class="flex flex-col items-end gap-1 shrink-0">
													<Badge variant="outline" class="text-xs">{notif.category}</Badge>
													<span class="text-xs text-muted-foreground">{formatRelativeTime(notif.createdAt)}</span>
												</div>
												{#if notif.actionUrl}
													<ChevronRight class="w-4 h-4 text-muted-foreground shrink-0 self-center" />
												{/if}
											</div>
										</button>
									</li>
								{/each}
							</ul>
						{/if}
					</CardContent>
				</Card>
			</div>

			<!-- Organizations Grid -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
				<CardHeader>
					<CardTitle class="text-lg flex items-center gap-2">
						<Building2 class="w-5 h-5 text-primary" />
						Organizations
					</CardTitle>
				</CardHeader>
				<CardContent>
					{#if organizations.length === 0}
						<div class="py-8 text-center space-y-2">
							<Building2 class="w-8 h-8 text-muted-foreground mx-auto" />
							<p class="text-sm text-muted-foreground">No organizations yet. Create or join one to get started.</p>
						</div>
					{:else}
						<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
							{#each organizations as org (org.id)}
								<button
									onclick={() => goto(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(org.id).path)}
									class="text-left focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background rounded-lg {org.status === 'SUSPENDED' ? 'opacity-60' : ''}"
									aria-label="Open {org.name} monitor"
								>
									<div class="p-4 rounded-lg border border-border/50 bg-background/50 hover:bg-muted/30 hover:shadow-primary/20 hover:shadow-md transition-all duration-200 space-y-3">
										<div class="flex items-start justify-between gap-2">
											<span class="text-sm font-semibold truncate">{org.name}</span>
											<div class="flex items-center gap-1.5 shrink-0">
												{#if org.role}
													<Badge variant="outline" class="text-xs">{org.role}</Badge>
												{/if}
												<Badge class="text-xs border shrink-0 {getOrgStatusClass(org.status ?? 'ACTIVE')}">
													{org.status ?? 'ACTIVE'}
												</Badge>
											</div>
										</div>
										<div class="flex items-center gap-4 text-xs text-muted-foreground">
											<span class="flex items-center gap-1">
												<Activity class="w-3 h-3" />
												{org.eventVolumeToday.toLocaleString()} events today
											</span>
											<span class="flex items-center gap-1">
												<Radio class="w-3 h-3" />
												{org.channelsInAlertCount} in alert
											</span>
										</div>
									</div>
								</button>
							{/each}
						</div>
					{/if}
				</CardContent>
			</Card>
		{/if}

	</div>
</main>
