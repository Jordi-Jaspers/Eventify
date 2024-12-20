<script lang="ts">
    import NavUser from '$lib/components/sidebar/nav-user.svelte';
    import NavMain from "$lib/components/sidebar/nav-main.svelte";
    import NavManagement from "$lib/components/sidebar/nav-management.svelte";
    import TeamSwitcher from "$lib/components/sidebar/team-switcher.svelte";
    import {Content, Footer, Header, Rail, Root, Separator} from "$lib/components/ui/sidebar";
    import type {ComponentProps} from "svelte";
    import {user} from "$lib/store/global.js";

    let {
        sidebarWidth = $bindable(0),
        ref = $bindable(null),
        collapsible = 'icon',
        ...restProps
    }: ComponentProps<typeof Root> = $props();
    $effect(() => {
        if (ref) {
            sidebarWidth = ref.clientWidth;

            // Optional: Add a resize observer to track width changes
            const resizeObserver = new ResizeObserver(entries => {
                for (let entry of entries) {
                    sidebarWidth = entry.target.clientWidth;
                }
            });

            resizeObserver.observe(ref);
            return () => {
                resizeObserver.disconnect();
            };
        }
    });
</script>

{#if user}
    <Root bind:ref {collapsible} {...restProps}>
        <Header>
            <TeamSwitcher/>
            <Separator/>
            <NavMain/>
        </Header>
        <Content>
            <NavManagement/>
        </Content>
        <Footer>
            <NavUser/>
        </Footer>
        <Rail/>
    </Root>
{/if}
