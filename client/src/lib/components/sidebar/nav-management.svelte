<script lang="ts">
    import * as DropdownMenu from "$lib/components/ui/dropdown-menu";
    import * as Sidebar from "$lib/components/ui/sidebar";
    import {HousePlus, MonitorCog, UserRoundCog, UsersRound} from "lucide-svelte";
    import {CLIENT_ROUTES} from "$lib/config/paths";
    import {Separator} from "$lib/components/ui/sidebar";
    import {user} from "$lib/store/global";

    const navigations = [
        {
            title: "User Management",
            url: CLIENT_ROUTES.USER_MANAGEMENT_PAGE.path,
            icon: UserRoundCog,
            permissions: ["READ_USERS", "EDIT_USERS"]
        },
        {
            title: "Team Management",
            url: CLIENT_ROUTES.TEAM_MANAGEMENT_PAGE.path,
            icon: UsersRound,
            permissions: ["READ_TEAMS", "EDIT_TEAMS"]
        },
        {
            title: "Dashboard Management",
            url: CLIENT_ROUTES.DASHBOARD_MANAGEMENT_PAGE.path,
            icon: MonitorCog,
            permissions: ["READ_DASHBOARDS", "EDIT_DASHBOARDS"]
        },
    ];

    const allowedNavigations = $derived(
        user ? navigations.filter(item =>
            item.permissions.some(p => user.permissions.includes(p))
        ) : []
    );
</script>

{#if allowedNavigations.length > 0}
    <Separator/>
    <Sidebar.Group>
        <Sidebar.GroupLabel>Configuration</Sidebar.GroupLabel>
        <Sidebar.Menu>
            {#each allowedNavigations as item (item.title)}
                <Sidebar.MenuItem>
                    <Sidebar.MenuButton>
                        {#snippet child({ props })}
                            <a href={item.url} {...props}>
                                <item.icon />
                                <span>{item.title}</span>
                            </a>
                        {/snippet}
                    </Sidebar.MenuButton>
                </Sidebar.MenuItem>
            {/each}
        </Sidebar.Menu>
    </Sidebar.Group>
{/if}

