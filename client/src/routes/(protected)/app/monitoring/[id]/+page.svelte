<script lang="ts">
	let { data } = $props();

	const dashboard: DashboardResponse = data.dashboard;
</script>

<div class="mb-4 flex items-center justify-between">
	<h1 class="text-2xl font-bold">You are monitoring "{dashboard.name}"</h1>
</div>

<pre>
{JSON.stringify(dashboard, null, 2)}
</pre>
