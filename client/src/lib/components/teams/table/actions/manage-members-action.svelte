<script lang="ts">
    import type {SubmitFunction} from "@sveltejs/kit";
    import {teams, users} from "$lib/store/global.js";
    import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle,} from "$lib/components/ui/dialog";
    import {Input} from "$lib/components/ui/input";
    import {ChevronLeft, Search} from "lucide-svelte";
    import {Button} from "$lib/components/ui/button";
    import ChevronRight from "lucide-svelte/icons/chevron-right";
    import {Separator} from "$lib/components/ui/separator";
    import {enhance} from '$app/forms';
    import {toast} from "svelte-sonner";

    let {
        id = 0,
        name = '',
        isDropdownOpen = $bindable(false),
        isManageMembersDialogOpen = $bindable(false)
    } = $props<{
        id: number,
        name: string,
        isDropdownOpen: boolean,
        isManageMembersDialogOpen: boolean
    }>();

    let searchQuery = $state('');
    const assigned: TeamMemberResponse[] = $derived.by(() => {
        let assignedUsers: TeamMemberResponse[] = teams.getTeams().find(team => team.id === id)?.members || [];
        return searchQuery === ''
            ? assignedUsers
            : assignedUsers.filter(user => {
                return user.firstName.toLowerCase().includes(searchQuery.toLowerCase())
                    || user.lastName.toLowerCase().includes(searchQuery.toLowerCase())
                    || user.email.toLowerCase().includes(searchQuery.toLowerCase());
            });
    });

    const unassigned = $derived.by(() => {
        let unassignedUsers = users.getUsers()
            .filter(user => !assigned.map(assignedUser => assignedUser.id).includes(user.id));

        return searchQuery === ''
            ? unassignedUsers
            : unassignedUsers.filter(user => {
                return user.firstName.toLowerCase().includes(searchQuery.toLowerCase())
                    || user.lastName.toLowerCase().includes(searchQuery.toLowerCase())
                    || user.email.toLowerCase().includes(searchQuery.toLowerCase());
            });
    });

    const assignMember: SubmitFunction = () => {
        return async ({result}) => {
            if (result.type === 'success' && result.data) {
                const updatedTeam: TeamResponse = result.data.response.data;
                teams.updateTeam(id, updatedTeam);
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }

    const unassignMember: SubmitFunction = () => {
        return async ({result}) => {
            if (result.type === 'success' && result.data) {
                const updatedTeam: TeamResponse = result.data.response.data;
                teams.updateTeam(id, updatedTeam);
            }

            if (result.type === 'failure' && result.data) {
                const apiResponse: ApiResponse = result.data.response;
                toast.error(apiResponse.message);
            }
        }
    }
</script>

<Dialog bind:open={isManageMembersDialogOpen} onOpenChange={() => isDropdownOpen = false}>
    <DialogContent class="sm:max-w-[700px] h-[500px] flex flex-col space-y-4">
        <DialogHeader>
            <DialogTitle>Manage Users for {name}</DialogTitle>
            <DialogDescription>Add or remove users by clicking on the arrows next to their name.</DialogDescription>
        </DialogHeader>
        <div class="flex flex-col space-y-4">
            <div class="flex items-center space-x-2">
                <Search class="w-4 h-4 text-gray-500"/>
                <Input
                        placeholder="Search users..."
                        bind:value={searchQuery}
                        class="flex-grow"
                />
            </div>
            <Separator/>

            <div class="flex space-x-4">
                <div class="w-1/2 h-[300px]">
                    <h3 class="font-semibold mb-2">Unassigned Users</h3>
                    <ul class="space-y-2 max-h-60 overflow-y-auto">
                        {#each unassigned as user (user.id)}
                            <li class="flex justify-between items-center">
                                <div class="flex flex-col w-full">
                                    <span>{user.firstName} {user.lastName}</span>
                                    <span class="text-xs text-muted-foreground">{user.email}</span>
                                </div>
                                <form method="POST"
                                      action="?/assignMember"
                                      use:enhance={assignMember}
                                      id="assign-member"
                                >
                                    <Button type="submit">
                                        <ChevronRight class="w-2 h-2"/>
                                        <input type="hidden" name="team_id" value={id}/>
                                        <input type="hidden" name="user_id" value={user.id}/>
                                    </Button>
                                </form>
                            </li>
                        {/each}
                    </ul>
                </div>
                <div class="w-1/2">
                    <h3 class="font-semibold mb-2">Assigned Users</h3>
                    <ul class="space-y-2 max-h-60 overflow-y-auto">
                        {#each assigned as user (user.id)}
                            <li class="flex justify-between items-center">
                                <form method="POST"
                                      action="?/unassignMember"
                                      use:enhance={unassignMember}
                                      id="unassign-member"
                                >
                                    <Button type="submit">
                                        <ChevronLeft class="w-2 h-2"/>
                                        <input type="hidden" name="team_id" value={id}/>
                                        <input type="hidden" name="user_id" value={user.id}/>
                                    </Button>
                                </form>
                                <div class="flex flex-col w-full px-4">
                                    <span>{user.firstName} {user.lastName}</span>
                                    <span class="text-xs text-muted-foreground">{user.email}</span>
                                </div>
                            </li>
                        {/each}
                    </ul>
                </div>
            </div>
        </div>
    </DialogContent>
</Dialog>
