<script lang="ts" generics="TData, TValue">
    import {type ColumnDef, getCoreRowModel} from "@tanstack/table-core";
    import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "$lib/components/ui/table";
    import {createSvelteTable, FlexRender,} from "$lib/components/ui/data-table/index";

    type DataTableProps<TData, TValue> = {
        columns: ColumnDef<TData, TValue>[];
        data: TData[];
    };

    let {data, columns}: DataTableProps<TData, TValue> = $props();
    const table = createSvelteTable({
        get data() {
            return data;
        },
        columns,
        getCoreRowModel: getCoreRowModel(),
    });
</script>

<div class="rounded-md border">
    <Table>
        <TableHeader>
            {#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
                <TableRow>
                    {#each headerGroup.headers as header (header.id)}
                        <TableHead class={header.column.columnDef.meta?.headerClassName}>
                            {#if !header.isPlaceholder}
                                <FlexRender
                                        content={header.column.columnDef.header}
                                        context={header.getContext()}
                                />
                            {/if}
                        </TableHead>
                    {/each}
                </TableRow>
            {/each}
        </TableHeader>
        <TableBody>
            {#each table.getRowModel().rows as row (row.id)}
                <TableRow data-state={row.getIsSelected() && "selected"}>
                    {#each row.getVisibleCells() as cell (cell.id)}
                        <TableCell class={cell.column.columnDef.meta?.cellClassName}>
                            <FlexRender
                                    content={cell.column.columnDef.cell}
                                    context={cell.getContext()}
                            />
                        </TableCell>
                    {/each}
                </TableRow>
            {:else}
                <TableRow>
                    <TableCell colspan={columns.length} class="h-24 text-center">
                        No results.
                    </TableCell>
                </TableRow>
            {/each}
        </TableBody>
    </Table>
</div>
