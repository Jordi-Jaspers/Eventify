<script lang="ts">
	import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '$lib/components/ui/dialog';

	const statuses = [
		{ name: 'OK', description: 'Service is responding normally', color: 'bg-green-500' },
		{ name: 'DEGRADED', description: 'Service is responding slowly', color: 'bg-orange-500' },
		{ name: 'MAINTENANCE', description: 'Service is under maintenance', color: 'bg-blue-500' },
		{ name: 'WARNING', description: 'Service is responding with errors', color: 'bg-yellow-500' },
		{ name: 'CRITICAL', description: 'Service is not responding', color: 'bg-red-600' },
		{ name: 'DETACHED', description: 'Service is not attached to the monitoring system', color: 'bg-gray-500' },
		{ name: 'UNKNOWN', description: 'The status of the service is unknown', color: 'bg-slate-400' }
	];

	let { children } = $props();
</script>

<Dialog>
	<DialogTrigger asChild>
		{#if children}
			{@render children()}
		{:else}
			<button class="btn btn-primary">Show Legend</button>
		{/if}
	</DialogTrigger>
	<DialogContent class="sm:max-w-[425px]">
		<DialogHeader>
			<DialogTitle>Timeline Status Legend</DialogTitle>
		</DialogHeader>
		<div class="space-y-4 py-4">
			<p class="text-sm text-muted-foreground">The timeline shows the status of services over time using the following color codes:</p>
			<div class="space-y-2">
				{#each statuses as status}
					<div class="flex items-center gap-3">
						<div class={`h-4 w-4 rounded ${status.color}`}></div>
						<div>
							<p class="font-medium">{status.name}</p>
							<p class="text-sm text-muted-foreground">{status.description}</p>
						</div>
					</div>
				{/each}
			</div>
		</div>
	</DialogContent>
</Dialog>
