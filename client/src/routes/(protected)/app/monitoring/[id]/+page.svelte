<script lang="ts">
	import { DisconnectedState } from '$lib/components/monitoring';
	import { MonitoringService } from '$lib/components/monitoring/service/monitoring.service.ts';

	let { data } = $props();
	let dashboard = $state(data.dashboard);
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

<div class="container mx-auto space-y-6 py-6">
	<div class="flex items-center justify-between">
		<h1 class="text-2xl font-bold">{dashboard.name}</h1>
		{#if dashboard.description}
			<p class="text-muted-foreground">{dashboard.description}</p>
		{/if}
	</div>

	{#if connectionStatus === 'error'}
		<DisconnectedState />
	{:else if !subscription}
		<div class="flex h-64 items-center justify-center">
			<div class="text-muted-foreground">
				{connectionStatus === 'connecting' ? 'Connecting to monitoring stream...' : 'Receiving initial data...'}
			</div>
		</div>
	{:else}
		<pre>{JSON.stringify(subscription, null, 2)}</pre>
	{/if}
</div>
