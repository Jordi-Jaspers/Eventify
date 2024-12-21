<script lang="ts">
    import {ArrowRight, Plus} from 'lucide-svelte'
    import {Button} from "$lib/components/ui/button"
    import {Input} from "$lib/components/ui/input"
    import {
        Dialog,
        DialogContent,
        DialogDescription,
        DialogFooter,
        DialogHeader,
        DialogTitle,
        DialogTrigger,
    } from "$lib/components/ui/dialog"
    import {Label} from "$lib/components/ui/label"
    import type {SubmitFunction} from "@sveltejs/kit";
    import {applyAction, enhance} from '$app/forms';
    import {toast} from "svelte-sonner";
    import {Submit} from "$lib/components/button";
    import {Textarea} from "$lib/components/ui/textarea";
    import {activeTeam, dashboards, user} from "$lib/store/global.js";
    import type {ServerResponse} from "$lib/models/server-response.svelte.js";
    import {Checkbox} from "$lib/components/ui/checkbox";
    import {Separator} from "$lib/components/ui/separator";
    import {goto} from "$app/navigation";
    import {CLIENT_ROUTES} from "$lib/config/paths";


    let isOpen = $state(false);
    let isLoading = $state(false);
    let formData = $state<DashboardCreationRequest>({
        name: '',
        description: '',
        teamId: activeTeam.value.id,
        global: false
    });

    const createDashboard: SubmitFunction = () => {
        isLoading = true
        return async ({result}) => {
            isLoading = false
            if (result.type === 'failure' && result.data) {
                const apiResponse: ServerResponse = result.data.response;
                toast.error(apiResponse.message);
            }

            if (result.type === 'success' && result.data) {
                const apiResponse: ServerResponse = result.data.response;
                toast.success('Dashboard successfully created.');

                const dashboard: DashboardResponse = apiResponse.data;
                dashboards.addDashboard(dashboard);

                await goto(CLIENT_ROUTES.DASHBOARD_CONFIGURATION_PAGE.path.replace('{id}', dashboard.id.toString()))
            }

            isOpen = false;
            formData = {name: '', description: '', teamId: activeTeam.value.id, global: false} as DashboardCreationRequest;
            await applyAction(result)
        }
    }
</script>

<Dialog bind:open={isOpen}>
    <DialogTrigger>
        {#if user && user.permissions.includes('WRITE_DASHBOARDS')}
            <Button>
                <Plus class="h-4 w-4"/>
            </Button>
        {/if}
    </DialogTrigger>
    <DialogContent>
        <DialogHeader>
            <DialogTitle>Create Dashboard</DialogTitle>
            <DialogDescription>Enter the details to create a dashboard so you can start configuring it.
            </DialogDescription>
        </DialogHeader>
        <form id="create-dashboard" action="?/createDashboard" method="POST" use:enhance={createDashboard}>
            <div class="grid w-full items-center gap-4">
                <Input type="hidden" name="teamId" value={activeTeam.value.id}/>
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
                <Separator/>
                <div class="grid gap-2">
                    <div class="flex flex-row items-center space-x-2">
                        <Label>Visibility</Label>
                        <Checkbox
                                name="global"
                                bind:checked={formData.global}
                        />
                    </div>
                    <p class="text-sm text-gray-500">
                        Make this dashboard visible to everyone, including users outside
                        your team.
                    </p>
                </div>

                <DialogFooter class="mt-4">
                    <Submit {isLoading} isDisabled={isLoading} title="Create Dashboard & Proceed"
                            form="create-dashboard">
                        <ArrowRight class="h-4 w-4"/>
                    </Submit>
                </DialogFooter>
            </div>
        </form>
    </DialogContent>
</Dialog>
