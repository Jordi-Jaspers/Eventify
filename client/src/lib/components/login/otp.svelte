<script>
    import {Button} from '$lib/components/ui/button';
    import {KeyRound, LoaderCircle} from 'lucide-svelte';

    let {isLoading} = $props();
</script>

<Button class="w-full" variant="outline" type="button" disabled={isLoading}>
    {#if isLoading}
        <LoaderCircle class="h-4 w-4 animate-spin"/>
    {/if}
    <KeyRound/>
    One-Time Passcode
</Button>
