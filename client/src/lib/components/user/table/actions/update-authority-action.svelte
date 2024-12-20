<script lang="ts">
    import type {SubmitFunction} from "@sveltejs/kit";
    import {toast} from "svelte-sonner";
    import {enhance} from '$app/forms';
    import {options, users} from "$lib/store/global.js";
    import {
        DialogContent,
        DialogDescription,
        DialogFooter,
        DialogHeader,
        DialogTitle,
        Root
    } from "$lib/components/ui/dialog";
    import {Submit} from "$lib/components/button";
    import {Select, SelectContent, SelectItem, SelectTrigger} from "$lib/components/ui/select/index.js";

    let {
        id = 0,
        authority = '',
        isDropdownOpen = $bindable(false),
        isUpdateDialogOpen = $bindable(false)
    } = $props<{
        id: number,
        authority: string,
        isDropdownOpen: boolean,
        isUpdateDialogOpen: boolean
    }>();

    let value = $state(authority[0].toUpperCase() + authority.slice(1).toLowerCase());
    const authorities: string[] = options.getAuthorityOptions().map((a) => a[0].toUpperCase() + a.slice(1).toLowerCase());

    const triggerContent = $derived(
        authorities.find((f) => f.toUpperCase() === value.toUpperCase()) ?? "Select an authority"
    );

    let isLoading = $state(false);
    let formData = $state({id: id, authority: ''});
    const updateAuthority: SubmitFunction = () => {
        isLoading = true;
        return async ({result}) => {
            isLoading = false;
            if (result.type === 'success' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                const user: UserDetailsResponse = apiResponse.data;
                users.updateUser(user);

                toast.success('Successfully updated authority for ' + user.email);
                isUpdateDialogOpen = false;
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }
</script>

<Root bind:open={isUpdateDialogOpen} onOpenChange={() => isDropdownOpen = false}>
    <DialogContent>
        <DialogHeader>
            <DialogTitle>Update Authority</DialogTitle>
            <DialogDescription>Update the authority for the user.</DialogDescription>
        </DialogHeader>
        <form id="update-authority" action="?/updateAuthority" method="POST" use:enhance={updateAuthority}>
            <input type="hidden" name="id" value={id}/>
            <input type="hidden" name="authority" value={value}/>

            <Select type="single" name="favoriteFruit" bind:value>
                <SelectTrigger class="w-[180px]">
                    {triggerContent}
                </SelectTrigger>
                <SelectContent>
                    {#each authorities as auth}
                        <SelectItem value={auth} label={auth}>
                            {auth[0].toUpperCase() + auth.slice(1).toLowerCase()}
                        </SelectItem>
                    {/each}
                </SelectContent>
            </Select>

            <DialogFooter class="mt-4">
                <Submit {isLoading} isDisabled={isLoading} title="Update authority" form="update-authority"/>
            </DialogFooter>
        </form>
    </DialogContent>
</Root>

