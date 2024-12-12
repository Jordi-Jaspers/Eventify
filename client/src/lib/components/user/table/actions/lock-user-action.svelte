<script lang="ts">
    import {Action, Cancel, Content, Description, Footer, Header, Root, Title} from "$lib/components/ui/alert-dialog";
    import type {SubmitFunction} from "@sveltejs/kit";
    import {toast} from "svelte-sonner";
    import {enhance} from '$app/forms';
    import {users} from "$lib/store/global";

    let {
        id = 0,
        enabled = false,
        isDropdownOpen = $bindable(false),
        isLockDialogOpen = $bindable(false)
    } = $props<{
        id: number,
        enabled: boolean,
        isDropdownOpen: boolean,
        isLockDialogOpen: boolean
    }>();

    let isLoading = $state(false);
    const lockUser: SubmitFunction = () => {
        if (isLoading || id === 0) return;

        isLoading = true;
        return async ({result}) => {
            if (result.type === 'success' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                const user: UserDetailsResponse = apiResponse.data;
                users.updateUser(user);

                if (user.enabled) {
                    toast.success(user.email + ' has been unlocked');
                } else {
                    toast.warning(user.email + ' has been locked');
                }
                isLockDialogOpen = false;
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }
</script>

<Root bind:open={isLockDialogOpen} onOpenChange={() => isDropdownOpen = false}>
    <Content>
        <Header>
            <Title>You are about to {enabled ? 'lock' : 'unlock'} a user</Title>
            <Description>
                Are you sure you want to {enabled ? 'lock' : 'unlock'} this user?
            </Description>
        </Header>
        <Footer>
            <Cancel>Cancel</Cancel>
            {#if enabled}
                <form method="POST"
                      id="lock-user"
                      action="?/lockUser"
                      use:enhance={lockUser}
                >
                    <Action type="submit"
                            id="lock-user"
                            class="bg-destructive text-destructive-foreground hover:bg-destructive/90">
                        <input type="hidden" name="id" value={id}/>
                        {enabled ? 'Lock User' : 'Unlock User'}
                    </Action>
                </form>
            {:else}
                <form method="POST"
                      id="unlock-user"
                      action="?/unlockUser"
                      use:enhance={lockUser}
                >
                    <Action type="submit"
                            id="unlock-user"
                            class="bg-primary text-primary-foreground hover:bg-primary/90">
                        <input type="hidden" name="id" value={id}/>
                        {enabled ? 'Lock User' : 'Unlock User'}
                    </Action>
                </form>
            {/if}
        </Footer>
    </Content>
</Root>
