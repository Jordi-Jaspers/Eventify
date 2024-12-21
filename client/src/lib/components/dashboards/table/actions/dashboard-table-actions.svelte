<script lang="ts">
    import Ellipsis from "lucide-svelte/icons/ellipsis";
    import {Button} from "$lib/components/ui/button";
    import {Content, Item, Root, Trigger} from "$lib/components/ui/dropdown-menu";
    import DeleteDashboardAction from "$lib/components/dashboards/table/actions/delete-dashboard-action.svelte";
    import UpdateDashboardAction from "$lib/components/dashboards/table/actions/update-dashboard-action.svelte";
    import {user} from "$lib/store/global.js";
    import {goto} from "$app/navigation";
    import {CLIENT_ROUTES} from "$lib/config/paths";

    let {id, name, description, global}: { id: number, name: string, description: string, global: boolean } = $props();
    let isUpdateDialogOpen = $state(false);
    let isDeleteDialogOpen = $state(false);
    let isDropdownOpen = $state(false);
</script>

<Root bind:open={isDropdownOpen}>
    <Trigger>
        {#snippet child({props})}
            <Button
                    {...props}
                    variant="ghost"
                    size="icon"
                    class="relative size-8 p-0"
            >
                <span class="sr-only">Open menu</span>
                <Ellipsis class="size-4 rotate-90"/>
            </Button>
        {/snippet}
    </Trigger>
    <Content side="bottom" align="start">
        <Item onclick={() => goto(CLIENT_ROUTES.DASHBOARD_MONITORING_PAGE.path.replace('{id}', id.toString()))}>
            Monitor
        </Item>
        <Item onclick={() => goto(CLIENT_ROUTES.DASHBOARD_CONFIGURATION_PAGE.path.replace('{id}', id.toString()))}>
            Configure
        </Item>
        <Item onclick={() => { isUpdateDialogOpen = true; }}>
            {#if user.permissions.includes('WRITE_DASHBOARDS')}
                Update
            {:else}
                View
            {/if}
        </Item>
        <Item class="data-[highlighted]:bg-red-400/80" onclick={() => { isDeleteDialogOpen = true; }}>
            Delete
        </Item>
    </Content>
</Root>

<DeleteDashboardAction {id} {name}
                       bind:isDropdownOpen={isDropdownOpen}
                       bind:isDeleteDialogOpen={isDeleteDialogOpen}/>

<UpdateDashboardAction {id} {name} {description} {global}
                       bind:isDropdownOpen={isDropdownOpen}
                       bind:isUpdateDialogOpen={isUpdateDialogOpen}/>


