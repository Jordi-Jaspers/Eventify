<script lang="ts">
    import {columns} from "$lib/components/user/table/columns";
    import {DataTable} from "$lib/components/ui/data-table";
    import {options, users} from "$lib/store/global.js";

    let {data} = $props();

    users.setUsers(data.users);
    options.setOptions(data.options);
</script>

<div class="flex justify-between items-center mb-4">
    <h1 class="text-2xl font-bold">User Management</h1>
</div>

<DataTable {columns} data={users.getUsers()}/>
