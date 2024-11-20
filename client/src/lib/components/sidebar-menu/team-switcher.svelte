<script lang="ts">
    import {Menu, MenuButton, MenuItem} from '$lib/components/ui/sidebar';
    import {user} from "$lib/store/global";
    import {Building} from "lucide-svelte";


</script>

<Menu>
    <MenuItem>
        <MenuButton size="lg">
            <div class="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                <Building class="size-4"/>
            </div>
            <div class="flex flex-col gap-0.5 leading-none">
                <span class="font-semibold">VodafoneZiggo</span>
                <span class="text-sidebar-foreground/70">SMC-DEV</span>
            </div>
        </MenuButton>
    </MenuItem>
</Menu>
