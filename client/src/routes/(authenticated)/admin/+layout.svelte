<script lang="ts">
    import {goto} from "$app/navigation";
    import {CLIENT_ROUTES} from "$lib/config/routes.ts";
    import {toast} from "svelte-sonner";
    import {currentUser} from '$lib/stores/auth';
    import {onMount} from "svelte";

    onMount(() => {
        if (!$currentUser || $currentUser.role !== 'ADMIN') {
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
            toast.error("Access denied: Admin privileges required.");
        }
    });

    let {children} = $props();
</script>

{@render children()}
