<script lang="ts">
    let {data} = $props();

    const dashboard: DashboardResponse = data.dashboard;
</script>

<div class="flex justify-between items-center mb-4">
    <h1 class="text-2xl font-bold">You are configuring "{dashboard.name}"</h1>
</div>

<pre>
{JSON.stringify(dashboard, null, 2)}
</pre>
