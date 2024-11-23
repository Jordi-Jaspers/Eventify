<script lang="ts">
    import * as DropdownMenu from "$lib/components/ui/dropdown-menu";
    import * as Sidebar from "$lib/components/ui/sidebar";
    import {HousePlug, MonitorCog, UserRoundCog} from "lucide-svelte";
    import {CLIENT_ROUTES} from "$lib/config/paths";

    const navigations = [
        {
            title: "User Management",
            url: CLIENT_ROUTES.USER_MANAGEMENT_PAGE.path,
            icon: UserRoundCog
        },
        {
            title: "Team Management",
            url: CLIENT_ROUTES.TEAM_MANAGEMENT_PAGE.path,
            icon: HousePlug
        },
        {
            title: "Dashboard Management",
            url: CLIENT_ROUTES.DASHBOARD_MANAGEMENT_PAGE.path,
            icon: MonitorCog,
        },
    ];
</script>

<Sidebar.Group>
    <Sidebar.GroupLabel>Configuration</Sidebar.GroupLabel>
    <Sidebar.Menu>
        {#each navigations as item (item.title)}
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
