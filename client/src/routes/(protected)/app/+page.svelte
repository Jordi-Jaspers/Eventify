<script lang="ts">
    import {user} from "$lib/store/global";
</script>

{#if user}
    Welcome {user.fullName} to home page.
{/if}
