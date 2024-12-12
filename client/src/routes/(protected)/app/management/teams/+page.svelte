<script lang="ts">
    import {columns} from "$lib/components/teams/table/columns";
    import {DataTable} from "$lib/components/ui/data-table";
    import {CreateTeam} from "$lib/components/teams";
    import {teams, users} from "$lib/store/global";
    import {LoaderCircle} from "lucide-svelte";

    let {data} = $props();

    teams.setTeams(data.teams);
    users.setUsers(data.users);
</script>

<div class="flex justify-between items-center mb-4">
    <h1 class="text-2xl font-bold">Team Management</h1>
</div>


{#if data}
    <DataTable {columns} data={teams.getTeams()}>
        <CreateTeam/>
    </DataTable>
{:else}
    <div class="flex flex-col items-center space-y-2 mt-32">
        <LoaderCircle class="h-8 w-8 animate-spin"/>
        <span class="text-xs text-muted-foreground"> Loading teams... </span>
    </div>
{/if}

