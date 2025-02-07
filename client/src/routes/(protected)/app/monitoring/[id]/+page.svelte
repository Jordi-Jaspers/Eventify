<script lang="ts">
	import { DisconnectedState, Timeline } from '$lib/components/monitoring';
	import { MonitoringService } from '$lib/components/monitoring/service/monitoring.service.ts';
	import { DashboardConfigurationHeader } from '$lib/configuration';
	import { CornerDownRight, EllipsisVertical, Folder, FolderRoot, HardDrive, Info } from 'lucide-svelte';
	import { Handle, Pane, ResizablePaneGroup } from '$lib/components/ui/resizable/index.js';

	let { data } = $props();
	let dashboard: DashboardResponse = $state(data.dashboard);
	let subscription = $state<DashboardSubscriptionResponse | null>(null);
	let connectionStatus = $state<'connecting' | 'connected' | 'error'>('connecting');

	$effect(() => {
		const unsubscribe = MonitoringService.subscribeToDashboard(dashboard, {
			onInitialized: (data) => {
				subscription = data;
				connectionStatus = 'connected';
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
			<button class="btn btn-primary">
				<Info />
			</button>
			<button class="btn btn-primary">
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
						<div class="flex items-center space-x-2">
							<FolderRoot class="h-4 w-4" />
							<p>{dashboard.name}</p>
						</div>
						{#each subscription.groupedChecks as group}
							<div class="ml-2 flex flex-col">
								<div class="flex items-center space-x-2">
									<div class="flex items-center space-x-1">
										<CornerDownRight class="h-4 w-4" />
										<Folder class="h-4 w-4" />
									</div>
									<p>{group.name}</p>
								</div>
								{#each group.checks as check}
									<div class="ml-6 flex flex-col">
										<div class="flex items-center space-x-2">
											<div class="flex items-center space-x-1">
												<CornerDownRight class="h-4 w-4" />
												<HardDrive class="h-4 w-4" />
											</div>
											<p>{check.name}</p>
										</div>
									</div>
								{/each}
							</div>
						{/each}
						{#each subscription.ungroupedChecks as check}
							<div class="flex items-center space-x-2">
								<HardDrive class="h-4 w-4" />
								<p>{check.name}</p>
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
							{#each group.checks as check}
								<div class="flex h-6 items-center">
									<Timeline bind:timeline={check.timeline} bind:window={subscription.window} />
								</div>
							{/each}
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
