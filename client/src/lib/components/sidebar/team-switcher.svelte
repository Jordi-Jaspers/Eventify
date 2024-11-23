<script lang="ts">
    import { Menu, MenuButton, MenuItem, useSidebar } from '$lib/components/ui/sidebar';
    import * as DropdownMenu from "$lib/components/ui/dropdown-menu";
    import { Building2 } from "lucide-svelte";
    import ChevronsUpDown from "lucide-svelte/icons/chevrons-up-down";
    import {user} from "$lib/store/global";

    const sidebar = useSidebar();
    let activeTeam = $state();

    $effect(() => {
        if (user?.teams?.length) {
            activeTeam = user.teams[0];
        }
    });

    const hasMultipleTeams = user?.teams?.length > 1;

    function selectTeam(team: string) {
        activeTeam = team;
    }
</script>

<Menu>
    <MenuItem>
        <DropdownMenu.Root>
            <DropdownMenu.Trigger>
                {#snippet child({props})}
                    <MenuButton
                            {...props}
                            size="lg"
                            class={hasMultipleTeams
                            ? "data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                            : "cursor-default"}
                    >
                        <!-- Icon -->
                        <div class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                            <Building2 class="size-4" />
                        </div>
                        <!-- Text Content -->
                        <div class="grid flex-1 text-left text-sm leading-tight">
                            <span class="truncate font-semibold">Ilionx - Monitoring</span>
                            <span class="truncate text-xs">
                                {activeTeam ? activeTeam : "No team assigned"}
                            </span>
                        </div>
                        <!-- Dropdown Icon -->
                        {#if hasMultipleTeams}
                            <ChevronsUpDown class="ml-auto" />
                        {/if}
                    </MenuButton>
                {/snippet}
            </DropdownMenu.Trigger>

            {#if hasMultipleTeams}
                <DropdownMenu.Content
                        class="w-[--bits-dropdown-menu-anchor-width] min-w-56 rounded-lg"
                        align="start"
                        side={sidebar.isMobile ? "bottom" : "right"}
                        sideOffset={4}
                >
                    <DropdownMenu.Label class="text-muted-foreground text-xs">Teams</DropdownMenu.Label>
                    {#each user.teams as team, index (team.name)}
                        <DropdownMenu.Item onSelect={() => selectTeam(team.name)} class="gap-2 p-2">
                            <!-- Team Icon -->
                            <div class="flex size-6 items-center justify-center rounded-sm border">
                                <Building2 class="size-4 shrink-0" />
                            </div>
                            {team.name}
                            <!-- Shortcut Key -->
                            <DropdownMenu.Shortcut>⌘{index + 1}</DropdownMenu.Shortcut>
                        </DropdownMenu.Item>
                    {/each}
                </DropdownMenu.Content>
            {/if}
        </DropdownMenu.Root>
    </MenuItem>
</Menu>
