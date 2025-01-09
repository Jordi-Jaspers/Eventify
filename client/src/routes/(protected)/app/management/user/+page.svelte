<script lang="ts">
	import { columns } from '$lib/components/user/table/columns';
	import { DataTable } from '$lib/components/ui/data-table';
	import { options, users } from '$lib/store/global.js';
	import { toMap } from '$lib/utils.ts';

	let { data } = $props();

	users.setUsers(data.users);
	options.setOptions(toMap(data.options));
</script>

<div class="mb-4 flex items-center justify-between">
	<h1 class="text-2xl font-bold">User Management</h1>
</div>

<DataTable {columns} data={users.getUsers()} />
