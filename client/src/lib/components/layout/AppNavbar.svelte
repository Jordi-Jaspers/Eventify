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
    import {authStore} from '$lib/stores/auth';
    import {CLIENT_ROUTES} from '$lib/config/routes';
    import Button from '$lib/components/ui/button/button.svelte';
    import {Activity, LogOut, User} from '@lucide/svelte';
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
</script>

<!-- Header -->
<header class="border-b border-border/50 bg-card/50 backdrop-blur-xl">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
        <a href={CLIENT_ROUTES.DASHBOARD_PAGE.path} class="flex items-center gap-3 hover:opacity-80 transition-opacity">
            <div class="inline-flex items-center justify-center w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-accent shadow-lg">
                <Activity class="w-5 h-5 text-primary-foreground"/>
            </div>
            <h1 class="text-2xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
                Eventify
            </h1>
        </a>
        <div class="flex items-center gap-2">
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
