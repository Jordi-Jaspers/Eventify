<script lang="ts">
	import { DisconnectedState, Legend, Timeline } from '$lib/components/monitoring';
	import { MonitoringService } from '$lib/components/monitoring/service/monitoring.service.ts';
	import { DashboardConfigurationHeader } from '$lib/configuration';
	import { ChevronRight, CornerDownRight, EllipsisVertical, Folder, FolderRoot, HardDrive, Info } from 'lucide-svelte';
	import { Handle, Pane, ResizablePaneGroup } from '$lib/components/ui/resizable';
	import { activeTeam } from '$lib/store/global.ts';
	import { toast } from 'svelte-sonner';
	import { goto } from '$app/navigation';
	import { CLIENT_ROUTES } from '$lib/config/paths.ts';

	let { data } = $props();
	let dashboard: DashboardResponse = $state(data.dashboard);
	let dashboards: DashboardResponse[] = $state(data.dashboards);
	let subscription = $state<DashboardSubscriptionResponse | null>(null);
	let connectionStatus = $state<'connecting' | 'connected' | 'error'>('connecting');

	let collapsedGroups = $state<Record<string, boolean>>({});

	function toggleGroup(groupId: number) {
		collapsedGroups[groupId] = !collapsedGroups[groupId];
	}

	const isAllowed: boolean = $derived.by(() => {
		if (dashboard.global) return true;
		const permittedDashboards: DashboardResponse[] = dashboards.filter((entry) => entry.team.id === activeTeam.value.id);
		return permittedDashboards.find((d) => d.id === dashboard.id) !== undefined;
	});

	$effect(() => {
		if (!isAllowed) {
			toast.error("Access Denied: Please verify that you're in the correct team or request viewing permissions from your administrator.");
			goto(CLIENT_ROUTES.DASHBOARDS_PAGE.path);
			return;
		}

		const unsubscribe = MonitoringService.subscribeToDashboard(dashboard, {
			onInitialized: (data) => {
				subscription = data;
				connectionStatus = 'connected';
				if (subscription) {
					subscription.groupedChecks.forEach((group) => {
						collapsedGroups[group.id] = true;
					});
				}
			},
			onUpdate: (data) => {
				subscription = data;
			},
			onError: () => {
				connectionStatus = 'error';
			}
		});

		return () => {
			unsubscribe();
			connectionStatus = 'connecting';
		};
	});
</script>

<div class="container mx-auto flex h-screen flex-col space-y-6 py-6">
	<div class="flex flex-none items-center justify-between">
		<DashboardConfigurationHeader
			name={dashboard.name}
			global={dashboard.global}
			lastUpdated={dashboard.lastUpdated}
			description={dashboard.description}
		/>
		<div class="space-x-2">
			<Legend>
				<button class="btn btn-primary">
					<Info />
				</button>
			</Legend>
			<button class="btn btn-primary" disabled>
				<EllipsisVertical />
			</button>
		</div>
	</div>
	<div class="min-h-0 flex-1">
		{#if connectionStatus === 'error'}
			<DisconnectedState />
		{:else if !subscription}
			<div class="flex h-64 items-center justify-center">
				<div class="text-muted-foreground">
					{connectionStatus === 'connecting' ? 'Connecting to monitoring stream...' : 'Receiving initial data...'}
				</div>
			</div>
		{:else}
			<div class="rounded-lg border border-muted/50 shadow-lg">
				<ResizablePaneGroup direction="horizontal">
					<Pane defaultSize={25} class="mx-2 p-4 px-1">
						<div class="flex min-w-0 items-center space-x-2">
							<FolderRoot class="h-4 w-4 flex-shrink-0" />
							<p class="truncate">{dashboard.name}</p>
						</div>
						{#each subscription.groupedChecks as group}
							<div class="flex flex-col">
								<button class="flex w-full min-w-0 items-center space-x-2 rounded hover:bg-muted/10" onclick={() => toggleGroup(group.id)}>
									<ChevronRight
										class="h-4 w-4 flex-shrink-0 transition-transform"
										style="transform: rotate({collapsedGroups[group.id] ? 0 : 90}deg)"
									/>
									<Folder class="h-4 w-4 flex-shrink-0" />
									<span class="truncate">{group.name}</span>
								</button>
								{#if !collapsedGroups[group.id]}
									{#each group.checks as check}
										<div class="ml-8 flex min-w-0 items-center space-x-2">
											<div class="flex flex-shrink-0 items-center space-x-1">
												<CornerDownRight class="h-4 w-4" />
												<HardDrive class="h-4 w-4" />
											</div>
											<p class="truncate">{check.name}</p>
										</div>
									{/each}
								{/if}
							</div>
						{/each}
						{#each subscription.ungroupedChecks as check}
							<div class="flex min-w-0 items-center space-x-2">
								<HardDrive class="h-4 w-4 flex-shrink-0" />
								<p class="truncate">{check.name}</p>
							</div>
						{/each}
					</Pane>
					<Handle withHandle />
					<Pane defaultSize={75} class="mx-2 p-4 px-1">
						<div class="flex h-6 items-center">
							<Timeline bind:timeline={subscription.timeline} bind:window={subscription.window} />
						</div>
						{#each subscription.groupedChecks as group}
							<div class="flex h-6 items-center">
								<Timeline bind:timeline={group.timeline} bind:window={subscription.window} />
							</div>
							{#if !collapsedGroups[group.id]}
								{#each group.checks as check}
									<div class="flex h-6 items-center">
										<Timeline bind:timeline={check.timeline} bind:window={subscription.window} />
									</div>
								{/each}
							{/if}
						{/each}
						{#each subscription.ungroupedChecks as check}
							<div class="flex h-6 items-center">
								<Timeline bind:timeline={check.timeline} bind:window={subscription.window} />
							</div>
						{/each}
					</Pane>
				</ResizablePaneGroup>
			</div>
		{/if}
	</div>
</div>
