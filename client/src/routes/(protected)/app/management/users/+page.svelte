<script lang="ts">
    import {columns} from "$lib/components/user/table/columns";
    import {DataTable} from "$lib/components/ui/data-table";
    import {options, users} from "$lib/store/global";
    import {LoaderCircle} from "lucide-svelte";

    let {data} = $props();

    users.setUsers(data.users);
    options.setOptions(data.options);
</script>

<div class="flex justify-between items-center mb-4">
    <h1 class="text-2xl font-bold">User Management</h1>
</div>

{#if data}
    <DataTable {columns} data={users.getUsers()}/>
{:else}
    <div class="flex flex-col items-center space-y-2 mt-32">
        <LoaderCircle class="h-8 w-8 animate-spin"/>
        <span class="text-xs text-muted-foreground"> Loading users... </span>
    </div>
{/if}

