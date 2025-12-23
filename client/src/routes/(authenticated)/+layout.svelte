<script lang="ts">
    import { page } from '$app/state';
    import AppBackground from '$lib/components/layout/AppBackground.svelte';
    import AppSidebar from '$lib/components/layout/AppSidebar.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar';

    interface Props {
        children: import('svelte').Snippet;
        data: { sidebarDefaultOpen: boolean };
    }

    let { children, data }: Props = $props();
</script>

<Sidebar.Provider open={data.sidebarDefaultOpen}>
    <AppSidebar currentPath={page.url.pathname} />
    <Sidebar.Inset>
        <AppBackground>
            {@render children()}
        </AppBackground>
    </Sidebar.Inset>
</Sidebar.Provider>
