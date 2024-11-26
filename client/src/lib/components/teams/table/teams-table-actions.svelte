<script lang="ts">
    import Ellipsis from "lucide-svelte/icons/ellipsis";
    import {Button} from "$lib/components/ui/button/index.js";
    import * as DropdownMenu from "$lib/components/ui/dropdown-menu";
    import * as AlertDialog from "$lib/components/ui/alert-dialog";
    import * as Dialog from "$lib/components/ui/dialog/index.js";
    import type {SubmitFunction} from "@sveltejs/kit";
    import {toast} from "svelte-sonner";
    import {enhance} from '$app/forms';
    import {teams} from "$lib/store/global";
    import {
        DialogContent,
        DialogDescription,
        DialogFooter,
        DialogHeader,
        DialogTitle
    } from "$lib/components/ui/dialog/index.js";
    import {Label} from "$lib/components/ui/label";
    import {Input} from "$lib/components/ui/input";
    import {Textarea} from "$lib/components/ui/textarea";
    import {Submit} from "$lib/components/button";

    let isDeleteDialogOpen = $state(false);
    let isEditDialogOpen = $state(false);

    let isDropdownOpen = $state(false);
    let isLoading = $state(false);

    let {id, name, description}: { id: number, name: string, description:string } = $props();
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
                isEditDialogOpen = false;
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }
    const deleteTeam: SubmitFunction = () => {
        return async ({result}) => {
            if (result.type === 'success' && result.data) {
                toast.success('Team deleted successfully');
                teams.removeTeam(id);
                isDeleteDialogOpen = false;
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }
</script>

<DropdownMenu.Root bind:open={isDropdownOpen}>
    <DropdownMenu.Trigger>
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
    </DropdownMenu.Trigger>
    <DropdownMenu.Content side="bottom" align="start">
        <DropdownMenu.Item onclick={() => { isEditDialogOpen = true; isDropdownOpen = false; }}>
            Edit
        </DropdownMenu.Item>
        <DropdownMenu.Item class="data-[highlighted]:bg-red-400/80" onclick={() => { isDeleteDialogOpen = true; isDropdownOpen = false; }}>
            Delete
        </DropdownMenu.Item>
    </DropdownMenu.Content>
</DropdownMenu.Root>

<AlertDialog.Root bind:open={isDeleteDialogOpen} onOpenChange={() => isDropdownOpen = false}>
    <AlertDialog.Content>
        <AlertDialog.Header>
            <AlertDialog.Title>Are you absolutely sure?</AlertDialog.Title>
            <AlertDialog.Description>
                This action cannot be undone. This will permanently delete the team "{name}"
                and remove its data from our servers.
            </AlertDialog.Description>
        </AlertDialog.Header>
        <AlertDialog.Footer>
            <AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
            <form method="POST"
                  action="?/deleteTeam"
                  use:enhance={deleteTeam}
            >
                <AlertDialog.Action type="submit" class="bg-destructive text-destructive-foreground hover:bg-destructive/90">
                    <input type="hidden" name="id" value={id}/>
                    Delete
                </AlertDialog.Action>
            </form>
        </AlertDialog.Footer>
    </AlertDialog.Content>
</AlertDialog.Root>

<Dialog.Root bind:open={isEditDialogOpen} onOpenChange={() => isDropdownOpen = false}>
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
</Dialog.Root>
