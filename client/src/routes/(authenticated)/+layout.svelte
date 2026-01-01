<script lang="ts">
    import { page } from '$app/state';
    import { onMount } from 'svelte';
    import AppBackground from '$lib/components/layout/AppBackground.svelte';
    import AppSidebar from '$lib/components/layout/AppSidebar.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar';
    import { organizationStore } from '$lib/stores/organization.svelte';

    interface Props {
        children: import('svelte').Snippet;
        data: { sidebarDefaultOpen: boolean };
    }

    let { children, data }: Props = $props();

    // Initialize organization store on mount
    onMount(() => {
        organizationStore.initialize();
    });
</script>

<Sidebar.Provider open={data.sidebarDefaultOpen}>
    <AppSidebar currentPath={page.url.pathname} />
    <Sidebar.Inset>
        <AppBackground>
            {@render children()}
        </AppBackground>
    </Sidebar.Inset>
</Sidebar.Provider>
