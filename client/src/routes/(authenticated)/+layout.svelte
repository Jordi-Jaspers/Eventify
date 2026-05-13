<script lang="ts">
    import { page } from '$app/state';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import { browser } from '$app/environment';
    import AppBackground from '$lib/components/layout/AppBackground.svelte';
    import AppSidebar from '$lib/components/layout/AppSidebar.svelte';
    import * as Sidebar from '$lib/components/ui/sidebar';
    import { organizationStore } from '$lib/stores/organization.svelte';
    import { authStore } from '$lib/stores/auth';
    import { notificationStore } from '$lib/stores/notification.svelte';
    import { CLIENT_ROUTES } from '$lib/config/routes';
    import { LoaderCircle } from '@lucide/svelte';

    interface Props {
        children: import('svelte').Snippet;
        data: { sidebarDefaultOpen: boolean };
    }

    let { children, data }: Props = $props();

    let isValidating: boolean = $state(true);
    let sessionValid: boolean = $state(false);

    onMount((): (() => void) => {
        if (!browser) return (): void => {};

        notificationStore.init();
        isValidating = true;
        
        (async (): Promise<void> => {
            const valid: boolean = await authStore.validateSession();
            sessionValid = valid;
            isValidating = false;

            if (!valid) {
                await goto(CLIENT_ROUTES.LOGIN_PAGE.path);
            } else {
                organizationStore.initialize();
            }
        })();

        const refreshInterval: ReturnType<typeof setInterval> = setInterval(async (): Promise<void> => {
            await authStore.validateSession();
        }, 10 * 60 * 1000);

        return (): void => {
            clearInterval(refreshInterval);
            notificationStore.destroy();
        };
    });
</script>

{#if isValidating}
    <!-- Loading skeleton overlay while validating session -->
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-background/95 backdrop-blur-sm">
        <div class="flex flex-col items-center gap-4">
            <LoaderCircle class="h-12 w-12 animate-spin text-primary" />
            <p class="text-sm text-muted-foreground">Validating session...</p>
        </div>
    </div>
{:else if sessionValid}
    <Sidebar.Provider open={data.sidebarDefaultOpen}>
        <AppSidebar currentPath={page.url.pathname} />
        <Sidebar.Inset>
            <AppBackground>
                {@render children()}
            </AppBackground>
        </Sidebar.Inset>
    </Sidebar.Provider>
{/if}
