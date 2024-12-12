<script lang="ts">
    import Ellipsis from "lucide-svelte/icons/ellipsis";
    import {Button} from "$lib/components/ui/button";
    import {Content, Item, Root, Trigger} from "$lib/components/ui/dropdown-menu";
    import LockUserAction from "$lib/components/user/table/actions/lock-user-action.svelte";
    import UpdateAuthorityAction from "$lib/components/user/table/actions/update-authority-action.svelte";
    import {user} from "$lib/store/global";

    let {id, enabled, authority}: { id: number, enabled: boolean, authority: string } = $props();

    let isUpdateDialogOpen = $state(false);
    let isLockDialogOpen = $state(false);
    let isDropdownOpen = $state(false);
    let isUserSelf = $derived(id === user.id);
</script>

<Root bind:open={isDropdownOpen}>
    <Trigger>
        {#snippet child({props})}
            <Button
                    {...props}
                    variant="ghost"
                    size="icon"
                    class="relative size-8 p-0"
                    disabled={isUserSelf}
            >
                <span class="sr-only">Open menu</span>
                <Ellipsis class="size-4 rotate-90"/>
            </Button>
        {/snippet}
    </Trigger>
    <Content side="bottom" align="start">
        <Item onclick={() => { isUpdateDialogOpen = true; }}>
            Update Authority
        </Item>
        <Item class="data-[highlighted]:bg-red-400/80" onclick={() => { isLockDialogOpen = true; }}>
            {#if enabled}
                Lock User
            {:else}
                Unlock User
            {/if}
        </Item>
    </Content>
</Root>

<LockUserAction {id} {enabled}
                bind:isDropdownOpen={isDropdownOpen}
                bind:isLockDialogOpen={isLockDialogOpen}/>

<UpdateAuthorityAction {id} {authority}
                       bind:isDropdownOpen={isDropdownOpen}
                       bind:isUpdateDialogOpen={isUpdateDialogOpen}/>


