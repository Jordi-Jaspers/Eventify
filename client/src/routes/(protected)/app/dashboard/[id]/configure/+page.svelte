<script lang="ts">
    import {Button} from "$lib/components/ui/button";
    import {CLIENT_ROUTES} from "$lib/config/paths";
    import {goto} from "$app/navigation";
    import {toast} from "svelte-sonner";
    import {FolderPlus, Plus} from "lucide-svelte";
    import {Switch} from "$lib/components/ui/switch";
    import {Label} from "$lib/components/ui/label";
    import {Separator} from "$lib/components/ui/separator";
    import ChevronRight from "lucide-svelte/icons/chevron-right";
    import Check from "lucide-svelte/icons/check";
    import X from "lucide-svelte/icons/x";
    import {DashboardConfigurationHeader} from "$lib/configuration";
    import {Submit} from "$lib/components/button";
    import type {SubmitFunction} from "@sveltejs/kit";
    import type {ServerResponse} from "$lib/models/server-response.svelte";
    import {applyAction, enhance} from '$app/forms';

    let {data} = $props();
    let dashboard: DashboardResponse = $state(data.dashboard);
    let isLoading: boolean = $state(false)

    let expandedGroups: string[] = $state([]);

    function toggleGroup(groupName: string) {
        if (expandedGroups.includes(groupName)) {
            expandedGroups = expandedGroups.filter(group => group !== groupName);
        } else {
            expandedGroups = [...expandedGroups, groupName];
        }
    }

    function handleAddCheck(group: null | DashboardGroupResponse = null) {
        group
            ? toast.info("Check has been added to " + group)
            : toast.info("Check has been added to parent");
    }

    let isAddingGroup: boolean = $state(false)
    let newGroupName: string = $state("")
    let newGroupInputRef: HTMLInputElement | undefined = $state();

    function isGroupNameUnique(name: string): boolean {
        return !dashboard.configuration.groups.some(group =>
            group.name.toLowerCase() === name.trim().toLowerCase()
        );
    }

    function handleAddGroup() {
        isAddingGroup = true;
        newGroupName = "";
        // Focus the input on next tick after render
        setTimeout(() => {
            newGroupInputRef?.focus();
        }, 0);
    }

    function handleSaveNewGroup() {
        const trimmedName = newGroupName.trim();
        if (trimmedName) {
            if (isGroupNameUnique(trimmedName)) {
                dashboard.configuration.groups = [
                    ...dashboard.configuration.groups,
                    {
                        name: trimmedName,
                        checks: []
                    }
                ];
                isAddingGroup = false;
                newGroupName = "";
            } else {
                toast.error(`Group "${trimmedName}" already exists`);
                newGroupInputRef?.focus();
            }
        }
    }

    function handleCancelNewGroup() {
        isAddingGroup = false;
        newGroupName = "";
    }

    function handleNewGroupKeydown(event: KeyboardEvent) {
        if (event.key === 'Enter') {
            handleSaveNewGroup();
        } else if (event.key === 'Escape') {
            handleCancelNewGroup();
        }
    }

    const configure: SubmitFunction = () => {
        isLoading = true
        return async ({result}) => {
            isLoading = false
            if (result.type === 'failure' && result.data) {
                const apiResponse: ServerResponse = result.data.response;
                toast.error(apiResponse.message);
            }

            if (result.type === 'success' && result.data) {
                const apiResponse: ServerResponse = result.data.response;
                toast.success('Dashboard configuration successfully updated.');

                dashboard = apiResponse.data as DashboardResponse
                await goto(CLIENT_ROUTES.DASHBOARD_CONFIGURATION_PAGE.path.replace('{id}', dashboard.id.toString()))
            }
            await applyAction(result)
        }
    }
</script>

<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <DashboardConfigurationHeader
            name={dashboard.name}
            global={dashboard.global}
            lastUpdated={dashboard.lastUpdated}
            updatedBy={dashboard.updatedBy}
    />

    <div class="w-full h-full min-h-96 grid grid-cols-8 gap-4">
        <div class="border rounded-md p-4 pt-2 w-full h-full col-span-5">
            <div class="flex items-center justify-between mb-4">
                <h4 class="text-lg font-bold">Configuration</h4>
                <div class="space-x-2">
                    <Button variant="outline" size="sm" onclick={() => handleAddGroup()}>
                        <FolderPlus class="h-4 w-4"/>
                        <span>Group</span>
                    </Button>
                    <Button variant="outline" size="sm" onclick={() => handleAddCheck()}>
                        <Plus class="h-4 w-4"/>
                        <span>Check</span>
                    </Button>
                </div>
            </div>

            <div class="space-y-2">
                <div class="px-2 py-1 text-sm text-muted-foreground">Groups</div>
                <Separator/>

                {#each dashboard.configuration.groups as group}
                    <div class="border rounded-md overflow-hidden">
                        <button class="w-full px-4 py-2 flex items-center justify-between hover:bg-muted/50 transition-colors"
                                onclick={() => toggleGroup(group.name)}
                        >
                                <span class="flex items-center space-x-2">
                                    <span class="transition-all duration-200 ease-in-out transform {expandedGroups.includes(group.name) ? 'rotate-90' : ''}">
                                      <ChevronRight class="h-4 w-4"/>
                                    </span>
                                    <span>{group.name}</span>
                                </span>
                            <Button variant="ghost" size="sm" onclick={e => {
                                 e.stopPropagation();
                                handleAddCheck(group);
                            }}>
                                <Plus class="h-4 w-4"/>
                            </Button>
                        </button>

                        {#if expandedGroups.includes(group.name)}
                            <div class="pl-6 pr-4 py-2 bg-muted/20 space-y-1">
                                {#each group.checks as check}
                                    <div class="flex items-center justify-between py-1 px-2 rounded-sm hover:bg-muted/50">
                                        <span class="text-sm">{check.name}</span>
                                    </div>
                                {/each}
                                {#if group.checks.length === 0}
                                    <div class="text-sm text-muted-foreground py-1 px-2">
                                        No checks configured for this group
                                    </div>
                                {/if}
                            </div>
                        {/if}
                    </div>
                {/each}

                {#if isAddingGroup}
                    <div class="border rounded-md overflow-hidden">
                        <div class="px-4 py-2 flex items-center justify-between bg-muted/20">
                            <div class="flex-1 flex items-center">
                                <ChevronRight class="h-4 w-4 mr-2 text-muted-foreground"/>
                                <input
                                        bind:this={newGroupInputRef}
                                        bind:value={newGroupName}
                                        type="text"
                                        placeholder="Enter group name"
                                        onkeydown={handleNewGroupKeydown}
                                        class="flex-1 bg-transparent border-none focus:outline-none text-sm {
                                        newGroupName.trim() && !isGroupNameUnique(newGroupName) ? 'text-red-500' : ''}"
                                />
                            </div>
                            <div class="flex items-center space-x-1">
                                <Button
                                        variant="ghost"
                                        size="sm"
                                        disabled={!newGroupName.trim()}
                                        onclick={handleSaveNewGroup}
                                >
                                    <Check class="h-4 w-4"/>
                                </Button>
                                <Button variant="ghost" size="sm" onclick={handleCancelNewGroup}>
                                    <X class="h-4 w-4"/>
                                </Button>
                            </div>
                        </div>
                    </div>
                {/if}

                <div class="mt-4 space-y-1">
                    <div class="px-2 py-1 text-sm text-muted-foreground">Ungrouped Checks</div>
                    <Separator/>

                    {#each dashboard.configuration.ungroupedChecks as check}
                        <div class="flex items-center justify-between py-1 px-2 rounded-sm hover:bg-muted/50">
                            <span class="text-sm">{check.name}</span>
                        </div>
                    {/each}

                    {#if dashboard.configuration.ungroupedChecks.length === 0}
                        <div class="text-sm text-muted-foreground py-1 px-2">
                            No ungrouped checks configured
                        </div>
                    {/if}
                </div>
            </div>
        </div>
        <div class="border rounded-md p-4 pt-2 w-full h-full col-span-3 flex flex-col">
            <h4 class="text-lg font-bold pb-4">Filters</h4>
            <div class="flex flex-col justify-between h-full">
                <div class="flex flex-col space-y-2">
                    <div class="flex items-center space-x-2">
                        <Switch id="airplane-mode"/>
                        <Label for="airplane-mode">Show Groups</Label>
                    </div>
                    <div class="flex items-center space-x-2">
                        <Switch id="airplane-mode"/>
                        <Label for="airplane-mode">Map Warnings</Label>
                    </div>
                    <div class="flex items-center space-x-2">
                        <Switch id="airplane-mode"/>
                        <Label for="airplane-mode">Critical Only</Label>
                    </div>
                </div>
                <div class="flex flex-col space-y-2">
                    <Separator class="my-4"/>
                    <div class="grid grid-cols-2 gap-2">
                        <Button onclick={() => goto(CLIENT_ROUTES.DASHBOARDS_PAGE.path)}>Back</Button>
                        <form id="configure-dashboard" action="?/configure" method="POST" use:enhance={configure}>
                            <input type="hidden" name="configuration" value={JSON.stringify(dashboard.configuration)}/>
                            <Submit {isLoading} isDisabled={isLoading} title="Save" form="configure-dashboard"/>
                        </form>
                    </div>
                    <form id="configure-dashboard-and-monitor" action="?/configureAndMonitor" method="POST"
                          use:enhance={configure}>
                        <input type="hidden" name="configuration" value={JSON.stringify(dashboard.configuration)}/>
                        <Submit {isLoading} isDisabled={isLoading} title="Save & Monitor"
                                form="configure-dashboard-and-monitor"/>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>


