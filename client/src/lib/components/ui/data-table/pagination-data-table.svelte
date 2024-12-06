<script lang="ts" generics="TData">
    import {type Table} from "@tanstack/table-core";
    import {Button} from "$lib/components/ui/button/index.js";
    import {ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight} from "lucide-svelte";
    import type {PaginationState} from "@tanstack/table-core";

    let {
        total = $bindable(0),
        table = $bindable({} as Table<TData>),
        pagination = $bindable({pageIndex: 0, pageSize: 1})
    }: { total: number, pagination: PaginationState, table: Table<TData> } = $props();

    const pageCount: number = $derived(Math.ceil(total / pagination.pageSize));
</script>

<div class="flex items-center justify-between p-2">
    <div class="flex items-center justify-center text-muted-foreground text-sm">
        Page {pagination.pageIndex + 1} of {pageCount}
    </div>
    <div class="flex items-center space-x-2">
        <Button
                variant="outline"
                class="hidden h-8 w-8 p-0 lg:flex"
                disabled={!table.getCanPreviousPage}
                onclick={() => table.firstPage()}
        >
            <span class="sr-only">Go to first page</span>
            <ChevronsLeft size={15}/>
        </Button>
        <Button
                variant="outline"
                class="h-8 w-8 p-0"
                disabled={!table.getCanPreviousPage}
                onclick={() => table.previousPage()}
        >
            <span class="sr-only">Go to previous page</span>
            <ChevronLeft size={15}/>
        </Button>
        <Button
                variant="outline"
                class="h-8 w-8 p-0"
                onclick={() => table.nextPage()}
                disabled={!table.getCanNextPage()}
        >
            <span class="sr-only">Go to next page</span>
            <ChevronRight size={15}/>
        </Button>
        <Button
                variant="outline"
                class="hidden h-8 w-8 p-0 lg:flex"
                disabled={!table.getCanNextPage}
                onclick={() => table.lastPage()}
        >
            <span class="sr-only">Go to last page</span>
            <ChevronsRight size={15}/>
        </Button>
    </div>
</div>
