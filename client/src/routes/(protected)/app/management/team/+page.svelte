<script lang="ts">
	import { columns } from '$lib/components/teams/table/columns';
	import { DataTable } from '$lib/components/ui/data-table';
	import { CreateTeam } from '$lib/components/teams';
	import { teams, users } from '$lib/store/global.js';

	let { data } = $props();

	teams.setTeams(data.teams);
	users.setUsers(data.users);
</script>

<div class="mb-4 flex items-center justify-between">
	<h1 class="text-2xl font-bold">Team Management</h1>
</div>

<DataTable {columns} data={teams.getTeams()}>
	<CreateTeam />
</DataTable>
