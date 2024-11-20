<script lang="ts">
    import {Menu, MenuButton, MenuItem} from "$lib/components/ui/sidebar/index.js";
    import {Cog, House, Search} from "lucide-svelte";

    const navigations = [
        {
            title: "Search",
            url: "#",
            icon: Search
        },
        {
            title: "Home",
            url: "#",
            icon: House
        },
        {
            title: "Config",
            url: "#",
            icon: Cog
        }
    ];
</script>

<Menu>
    {#each navigations as item}
        <MenuItem>
            <MenuButton>
                {#snippet child({props})}
                    <a href={item.url} {...props}>
                        <item.icon class="size-4"/>
                        <span>{item.title}</span>
                    </a>
                {/snippet}
            </MenuButton>
        </MenuItem>
    {/each}
</Menu>
