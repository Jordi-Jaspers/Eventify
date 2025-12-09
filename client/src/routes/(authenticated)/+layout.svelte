<script lang="ts">
    import AppBackground from '$lib/components/layout/AppBackground.svelte';
    import AppNavbar from '$lib/components/layout/AppNavbar.svelte';

    let {children} = $props();
</script>

<AppBackground>
    <AppNavbar />
    {@render children()}
</AppBackground>
