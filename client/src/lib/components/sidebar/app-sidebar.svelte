<script lang="ts">
    import NavUser from '$lib/components/sidebar/nav-user.svelte';
    import NavMain from "$lib/components/sidebar/nav-main.svelte";
    import NavManagement from "$lib/components/sidebar/nav-management.svelte";
    import TeamSwitcher from "$lib/components/sidebar/team-switcher.svelte";
    import {Content, Footer, Header, Rail, Root, Separator} from "$lib/components/ui/sidebar";
    import type {ComponentProps} from "svelte";
    import {user} from "$lib/store/global";

    let {ref = $bindable(null), collapsible = 'icon', ...restProps}: ComponentProps<typeof Root> = $props();
</script>

<Root bind:ref {collapsible} {...restProps}>
    <Header>
        <TeamSwitcher/>
        <Separator/>
        <NavMain/>
        <Separator/>
    </Header>
    <Content>
        {#if user.authorities.includes("ADMIN")}
            <NavManagement/>
        {/if}
    </Content>
    <Footer>
        <NavUser/>
    </Footer>
    <Rail/>
</Root>
