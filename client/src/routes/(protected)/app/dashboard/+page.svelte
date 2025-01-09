<script lang="ts">
	import { activeTeam, dashboards } from '$lib/store/global.js';
	import { columns } from '$lib/components/dashboards/table/columns';
	import { DataTable } from '$lib/components/ui/data-table';
	import { CreateDashboard } from '$lib/components/dashboards';

	let { data } = $props();
	dashboards.setDashboards(data.dashboards);

	const permittedDashboards: DashboardResponse[] = $derived(
		dashboards.getDashboards().filter((entry) => entry.team.id === activeTeam.value.id || entry.global)
	);
</script>

<div class="mb-4 flex items-center justify-between">
	<h1 class="text-2xl font-bold">Dashboards</h1>
</div>

<DataTable {columns} data={permittedDashboards}>
	<CreateDashboard />
</DataTable>
