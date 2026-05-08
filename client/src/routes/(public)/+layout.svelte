<script lang="ts">
    import AppBackground from '$lib/components/layout/AppBackground.svelte';
    import { page } from '$app/stores';

    let {children} = $props();
    
    // Check if we're on a full-width page (landing, pricing)
    const isFullWidthPage = $derived(
        $page.url.pathname === '/' || $page.url.pathname === '/pricing'
    );
</script>

<AppBackground>
    {#if isFullWidthPage}
        <div class="w-full animate-fade-in">
            {@render children()}
        </div>
    {:else}
        <div class="flex items-center justify-center min-h-screen px-4 py-8">
            <div class="w-full animate-fade-in">
                {@render children()}
            </div>
        </div>
    {/if}
</AppBackground>
