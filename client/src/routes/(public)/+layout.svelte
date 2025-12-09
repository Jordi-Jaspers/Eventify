<script lang="ts">
    import AppBackground from '$lib/components/layout/AppBackground.svelte';

    let {children} = $props();
</script>

<AppBackground>
    <div class="flex items-center justify-center min-h-screen px-4 py-8">
        <div class="w-full animate-fade-in">
            {@render children()}
        </div>
    </div>
</AppBackground>
