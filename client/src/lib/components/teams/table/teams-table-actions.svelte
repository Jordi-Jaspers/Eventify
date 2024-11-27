<script lang="ts">
    import Ellipsis from "lucide-svelte/icons/ellipsis";
    import {Button} from "$lib/components/ui/button/index.js";
    import DeleteTeamAction from "$lib/components/teams/table/delete-team-action.svelte";
    import UpdateTeamAction from "$lib/components/teams/table/update-team-action.svelte";
    import ManageMembersAction from "$lib/components/teams/table/manage-members-action.svelte";
    import {Content, Item, Root, Trigger} from "$lib/components/ui/dropdown-menu";

    let isDeleteDialogOpen = $state(false);
    let isUpdateDialogOpen = $state(false);
    let isManageMembersDialogOpen = $state(false);
    let isDropdownOpen = $state(false);
    let {id, name, description}: { id: number, name: string, description: string } = $props();
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
        <Item onclick={() => { isManageMembersDialogOpen = true; }}>
            Manage Members
        </Item>
        <Item onclick={() => { isUpdateDialogOpen = true; }}>
            Update Details
        </Item>
        <Item class="data-[highlighted]:bg-red-400/80"
              onclick={() => { isDeleteDialogOpen = true;}}>
            Delete Team
        </Item>
    </Content>
</Root>

<ManageMembersAction {id} {name}
                  bind:isDropdownOpen={isDropdownOpen}
                  bind:isManageMembersDialogOpen={isManageMembersDialogOpen}/>

<UpdateTeamAction {id} {name} {description}
                  bind:isDropdownOpen={isDropdownOpen}
                  bind:isUpdateDialogOpen={isUpdateDialogOpen}/>

<DeleteTeamAction {id} {name}
                  bind:isDropdownOpen={isDropdownOpen}
                  bind:isDeleteDialogOpen={isDeleteDialogOpen}/>


