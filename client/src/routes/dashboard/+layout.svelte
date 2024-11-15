<script lang="ts">
  import * as Breadcrumb from "$lib/components/ui/breadcrumb";
  import { Separator } from "$lib/components/ui/separator";
  import * as Sidebar from "$lib/components/ui/sidebar";
  import { SidebarMenu } from '$lib/components/sidebar-menu';


  let { children } = $props();
</script>

<Sidebar.Provider>
  <SidebarMenu />
  <Sidebar.Inset>
    <header
      class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12"
    >
      <div class="flex items-center gap-2 px-4">
        <Sidebar.Trigger class="-ml-1" />
        <Separator orientation="vertical" class="mr-2 h-4" />
        <Breadcrumb.Root>
          test
        </Breadcrumb.Root>
      </div>
    </header>
    <main>
      {@render children()}
    </main>
  </Sidebar.Inset>
</Sidebar.Provider>
