<script lang="ts">
import { LoadingScreen } from '$lib/components/general';
import {toast} from "svelte-sonner";
import {goto} from "$app/navigation";
import {CLIENT_ROUTES} from "$lib/config/paths";

let { data } = $props();
$effect(() => {
    if (!data.response) {
        return;
    }

    if (data.response.error) {
        toast.error(data.response.error);
        goto(CLIENT_ROUTES.LOGIN_PAGE.path);
    } else {
        toast.success('Email verified successfully! You can now login.');
        goto(CLIENT_ROUTES.HOME_PAGE.path);
    }
});
</script>

{#if !data}
    <LoadingScreen />
{/if}
