<script lang="ts">
    import * as Breadcrumb from '$lib/components/ui/breadcrumb';
    import {Separator} from '$lib/components/ui/separator';
    import * as Sidebar from '$lib/components/ui/sidebar';
    import {SidebarMenu} from '$lib/components/sidebar';
    import {breadcrumbs} from "$lib/store/global";

    let {children} = $props();
    let sidebarWidth = $state(0);
</script>

<head>
    <title>Eventify</title>
    <meta name="description" content="The application page of Eventify.io for all your monitoring / management needs." />
    <meta name="keywords" content="eventify, monitoring, checks, analyze, management, application, webapp" />
</head>

<Sidebar.Provider>
    <SidebarMenu bind:sidebarWidth={sidebarWidth}/>
    <Sidebar.Inset>
            <header class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
                <div class="flex items-center gap-2 px-4">
                    <Sidebar.Trigger class="-ml-1"/>
                    <Separator orientation="vertical" class="mr-2 h-4"/>
                    {#if breadcrumbs._locations.length > 0}
                    <Breadcrumb.Root>
                        <Breadcrumb.List>
                            {#each breadcrumbs._locations as location, index}
                                    <Breadcrumb.Item>
                                        {location}
                                    </Breadcrumb.Item>
                                    {#if index < breadcrumbs._locations.length - 1}
                                        <Breadcrumb.Separator/>
                                    {/if}
                            {/each}
                        </Breadcrumb.List>
                    </Breadcrumb.Root>
                    {:else}
                        <Breadcrumb.Root>
                            <Breadcrumb.List>
                                    <Breadcrumb.Item>
                                        Home
                                    </Breadcrumb.Item>
                            </Breadcrumb.List>
                        </Breadcrumb.Root>
                    {/if}
                </div>
            </header>
        <main class="p-4 w-full" style:max-width="calc(100vw - {sidebarWidth}px)">
            {@render children()}
        </main>
    </Sidebar.Inset>
</Sidebar.Provider>
