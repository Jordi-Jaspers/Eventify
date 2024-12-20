<script lang="ts">
    import type {SubmitFunction} from "@sveltejs/kit";
    import {toast} from "svelte-sonner";
    import {enhance} from '$app/forms';
    import {teams} from "$lib/store/global.js";
    import {
        DialogContent,
        DialogDescription,
        DialogFooter,
        DialogHeader,
        DialogTitle,
        Root
    } from "$lib/components/ui/dialog";
    import {Label} from "$lib/components/ui/label";
    import {Input} from "$lib/components/ui/input";
    import {Textarea} from "$lib/components/ui/textarea";
    import {Submit} from "$lib/components/button";

    let {
        id = 0,
        name = '',
        description = '',
        isDropdownOpen = $bindable(false),
        isUpdateDialogOpen = $bindable(false)
    } = $props<{
        id: number,
        name: string,
        description: string,
        isDropdownOpen: boolean,
        isUpdateDialogOpen: boolean
    }>();

    let isLoading = $state(false);
    let formData = $state({name: name, description: description});
    const updateTeam: SubmitFunction = () => {
        isLoading = true;

        return async ({result}) => {
            isLoading = false;
            if (result.type === 'success' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                const team: TeamResponse = apiResponse.data;

                toast.success('Team updated successfully');
                teams.updateTeam(id, team);
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
            <DialogTitle>Update Team</DialogTitle>
            <DialogDescription>Update the name and description for the team.</DialogDescription>
        </DialogHeader>
        <form id="update-team" action="?/updateTeam" method="POST" use:enhance={updateTeam}>
            <input type="hidden" name="id" value={id}/>
            <div class="grid w-full items-center gap-4">
                <div class="grid gap-2">
                    <div class="flex flex-row items-center justify-between">
                        <Label>Name</Label>
                    </div>
                    <Input
                            name="name"
                            bind:value={formData.name}
                            required
                    />
                </div>

                <div class="grid gap-2">
                    <div class="flex flex-row items-center justify-between">
                        <Label>Description</Label>
                    </div>
                    <Textarea
                            name="description"
                            placeholder="Type your description here."
                            bind:value={formData.description}
                            required
                    />
                </div>
            </div>
            <DialogFooter class="mt-4">
                <Submit {isLoading} isDisabled={isLoading} title="Update" form="update-team"/>
            </DialogFooter>
        </form>
    </DialogContent>
</Root>

