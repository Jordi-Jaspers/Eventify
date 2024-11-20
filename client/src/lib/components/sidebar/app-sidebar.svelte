<script lang="ts">
    import NavUser from '$lib/components/sidebar/nav-user.svelte';
    import NavMain from "$lib/components/sidebar/nav-main.svelte";
    import TeamSwitcher from "$lib/components/sidebar/team-switcher.svelte";
    import {Content, Footer, Header, Rail, Root} from "$lib/components/ui/sidebar";
    import type {ComponentProps} from "svelte";

    let {ref = $bindable(null), collapsible = 'icon', ...restProps}: ComponentProps<typeof Root> = $props();
</script>

<Root bind:ref {collapsible} {...restProps}>
    <Header>
        <TeamSwitcher/>
        <NavMain/>
    </Header>
    <Content>
        <!--Can add more here-->
    </Content>
    <Footer>
        <NavUser/>
    </Footer>
    <Rail/>
</Root>
