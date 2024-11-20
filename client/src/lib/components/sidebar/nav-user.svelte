<script lang="ts">
    import * as Avatar from '$lib/components/ui/avatar';
    import {Root, Trigger, Content, Label, Separator, Group, Item} from '$lib/components/ui/dropdown-menu';
    import {Menu, MenuItem, MenuButton} from '$lib/components/ui/sidebar';
    import {useSidebar} from '$lib/components/ui/sidebar';
    import {toggleMode} from 'mode-watcher';
    import BadgeCheck from 'lucide-svelte/icons/badge-check';
    import ChevronsUpDown from 'lucide-svelte/icons/chevrons-up-down';
    import LogOut from 'lucide-svelte/icons/log-out';
    import Sun from 'lucide-svelte/icons/sun';
    import Moon from 'lucide-svelte/icons/moon';
    import {user} from "$lib/store/global";
    import {goto} from "$app/navigation";
    import {CLIENT_ROUTES} from "$lib/config/paths";
    import {Coffee, Send} from "lucide-svelte";
    import Settings2 from "lucide-svelte/icons/settings-2";

    const sidebar = useSidebar();
    const navigations =[
        {
            title: "Account",
            url: "/account",
            icon: BadgeCheck,
        },
        {
            title: "Settings",
            url: "settings",
            icon: Settings2,
        },
        {
            title: "Donate Coffee",
            url: "buymeacoffee.com/jaspers",
            icon: Coffee
        },
        {
            title: "Feedback",
            url: "/feedback",
            icon: Send,
        },
    ]

</script>

<Menu>
    <MenuItem>
        <Root>
            <Trigger>
                {#snippet child({props})}
                    <MenuButton size="lg"
                                class="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                                {...props}>
                        <Avatar.Root class="h-8 w-8 rounded-lg">
                            <Avatar.Fallback class="rounded-lg">
                                {user.initials}
                            </Avatar.Fallback>
                        </Avatar.Root>
                        <div class="grid flex-1 text-left text-sm leading-tight">
                            <span class="truncate font-semibold">{user.fullName}</span>
                            <span class="truncate text-xs">{user.email}</span>
                        </div>
                        <ChevronsUpDown class="ml-auto size-4"/>
                    </MenuButton>
                {/snippet}
            </Trigger>
            <Content
                    class="w-[--bits-dropdown-menu-anchor-width] min-w-56 rounded-lg"
                    side={sidebar.isMobile ? 'bottom' : 'right'}
                    align="end"
                    sideOffset={4}
            >
                <Label class="p-0 font-normal">
                    <div class="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                        <Avatar.Root class="h-8 w-8 rounded-lg">
                            <Avatar.Fallback class="rounded-lg">
                                {user.initials}
                            </Avatar.Fallback>
                        </Avatar.Root>
                        <div class="grid flex-1 text-left text-sm leading-tight">
                            <span class="truncate font-semibold">{user.fullName}</span>
                            <span class="truncate text-xs">{user.email}</span>
                        </div>
                    </div>
                </Label>
                <Separator/>
                <Group>
                    {#each navigations as item}
                        <Item onclick={() => goto(item.url)}>
                            <item.icon/>
                            {item.title}
                        </Item>
                    {/each}
                </Group>
                <Separator/>
                <Group>
                    <Item onclick={toggleMode}>
						<span class="flex items-center justify-center gap-2">
							<Moon class="hidden h-[1.2rem] w-[1.2rem] transition-all dark:flex dark:scale-100"/>
							<Sun class="flex h-[1.2rem] w-[1.2rem] scale-100 transition-all dark:hidden"/>
							<span>Switch Mode</span>
						</span>
                    </Item>
                </Group>
                <Separator/>
                <Item class="data-[highlighted]:bg-red-400/80" onclick={() => goto(CLIENT_ROUTES.LOGOUT_PAGE.path)}>
                    <LogOut/>
                    Log out
                </Item>
            </Content>
        </Root>
    </MenuItem>
</Menu>
