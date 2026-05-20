<!--
  AppNavbar Component

  Navigation bar for authenticated pages with logo, navigation links, and logout functionality.

  Props:
  - None (uses authStore and navigation context)

  Usage:
  <AppNavbar />
-->
<script lang="ts">
    import {goto} from '$app/navigation';
    import {authStore, currentUser} from '$lib/stores/auth';
    import {CLIENT_ROUTES} from '$lib/config/routes';
    import Button from '$lib/components/ui/button/button.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import {LogOut, User, LayoutDashboard} from '@lucide/svelte';
    import {toast} from 'svelte-sonner';
    import {handleError} from '$lib/utils/error-handler';

    async function handleLogout(): Promise<void> {
        try {
            await authStore.logout();
            await goto(CLIENT_ROUTES.LANDING_PAGE.path);
        } catch (error: unknown) {
            const {message}: { message: string } = handleError(error, 'Logout failed. Please try again.');
            toast.error(message);
        }
    }

    const isAdmin: boolean = $derived($currentUser?.role === 'ADMIN');
</script>

<!-- Header -->
<header class="border-b border-border/50 bg-card/50 backdrop-blur-xl">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
        <AppLogo size="small" href={CLIENT_ROUTES.DASHBOARD_PAGE.path} />
        <div class="flex items-center gap-2">
            {#if isAdmin}
                <Button
                        onclick={() => goto(CLIENT_ROUTES.ADMIN_STATISTICS_PAGE.path)}
                        variant="outline"
                        class="bg-background/50 border-border/50 hover:bg-accent/20 hover:border-accent hover:shadow-lg hover:shadow-accent/20 hover:scale-105 transition-all duration-200"
                        title="Admin Statistics"
                >
                    <LayoutDashboard class="h-4 w-4"/>
                </Button>
            {/if}
            <Button
                    onclick={() => goto(CLIENT_ROUTES.PROFILE_PAGE.path)}
                    variant="outline"
                    class="bg-background/50 border-border/50 hover:bg-primary/20 hover:border-primary hover:shadow-lg hover:shadow-primary/20 hover:scale-105 transition-all duration-200"
                    title="Profile"
            >
                <User class="h-4 w-4"/>
            </Button>
            <Button
                    onclick={handleLogout}
                    variant="outline"
                    class="bg-background/50 border-border/50 hover:bg-destructive/20 hover:border-destructive hover:text-destructive hover:shadow-lg hover:shadow-destructive/20 hover:scale-105 transition-all duration-200"
                    title="Logout"
            >
                <LogOut class="h-4 w-4"/>
            </Button>
        </div>
    </div>
</header>
