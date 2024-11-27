<script lang="ts">
    import {Menu, MenuButton, MenuItem, useSidebar} from '$lib/components/ui/sidebar';
    import {
        DropdownMenu,
        DropdownMenuContent,
        DropdownMenuItem,
        DropdownMenuLabel,
        DropdownMenuShortcut,
        DropdownMenuTrigger
    } from "$lib/components/ui/dropdown-menu";
    import {Building2} from "lucide-svelte";
    import ChevronsUpDown from "lucide-svelte/icons/chevrons-up-down";
    import {user} from "$lib/store/global";

    const sidebar = useSidebar();
    const hasMultipleTeams = $derived(user.teams.length > 1);
    let activeTeam: TeamResponse | undefined = $state();

    $effect(() => {
        if (!user.teams) return;
        activeTeam = user.teams[0];
    });
</script>

<Menu>
    <MenuItem>
        <DropdownMenu>
            <DropdownMenuTrigger>
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
                            <Building2 class="size-4"/>
                        </div>
                        <!-- Text Content -->
                        <div class="grid flex-1 text-left text-sm leading-tight">
                            <span class="truncate font-semibold">Ilionx - Monitoring</span>
                            <span class="truncate text-xs">
                                {activeTeam ? activeTeam.name : "No team assigned"}
                            </span>
                        </div>
                        <!-- Dropdown Icon -->
                        {#if hasMultipleTeams}
                            <ChevronsUpDown class="ml-auto"/>
                        {/if}
                    </MenuButton>
                {/snippet}
            </DropdownMenuTrigger>

            {#if hasMultipleTeams}
                <DropdownMenuContent
                        class="w-[--bits-dropdown-menu-anchor-width] min-w-56 rounded-lg"
                        align="start"
                        side={sidebar.isMobile ? "bottom" : "right"}
                        sideOffset={4}
                >
                    <DropdownMenuLabel class="text-muted-foreground text-xs">Teams</DropdownMenuLabel>
                    {#each user.teams as team, index (team.name)}
                        <DropdownMenuItem onSelect={() => activeTeam = team} class="gap-2 p-2">
                            <div class="flex size-6 items-center justify-center rounded-sm border">
                                <Building2 class="size-4 shrink-0"/>
                            </div>
                            {team.name}
                            <DropdownMenuShortcut>⌘{index + 1}</DropdownMenuShortcut>
                        </DropdownMenuItem>
                    {/each}
                </DropdownMenuContent>
            {/if}
        </DropdownMenu>
    </MenuItem>
</Menu>
