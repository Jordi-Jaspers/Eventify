<script lang="ts">
    import {columns} from "$lib/components/teams/table/columns";
    import {DataTable} from "$lib/components/ui/data-table";
    import {CreateTeam} from "$lib/components/teams";
    import {teams} from "$lib/store/global";

    let {data} = $props();
    let teamsPage: PageResponse<TeamResponse> = $state(data.teams);
    teams.setTeams(teamsPage.content);
</script>

<div class="flex justify-between items-center mb-4">
    <h1 class="text-2xl font-bold">Team Management</h1>
    <CreateTeam/>
</div>

<DataTable {columns} data={teams.getTeams()}/>


