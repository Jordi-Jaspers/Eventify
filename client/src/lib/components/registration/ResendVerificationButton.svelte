<script lang="ts">
    import {authStore} from '$lib/stores/auth';
    import {Button} from '$lib/components/ui/button';
    import {LoaderCircle} from '@lucide/svelte';

    interface Props {
        variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
        size?: 'default' | 'sm' | 'lg' | 'icon';
        class?: string;
    }

    let {variant = 'outline', size = 'default', class: className = ''}: Props = $props();

    const COOLDOWN_SECONDS = 60;
    let loading = $state(false);
    let cooldown = $state(0);

    async function handleResend(): Promise<void> {
        if (loading || cooldown > 0) return;

        loading = true;
        try {
            await authStore.resendVerification();
            cooldown = COOLDOWN_SECONDS;
        } finally {
            loading = false;
        }
    }

    // Cooldown countdown effect
    $effect(() => {
        if (cooldown <= 0) return;
        const id = setInterval(() => {
            cooldown--;
        }, 1000);

        return () => clearInterval(id);
    });
</script>

<Button
        {variant}
        {size}
        class={`${className} transition-all duration-300 relative overflow-hidden group`}
        disabled={loading || cooldown > 0}
        onclick={handleResend}
>
    <span class="relative z-10 flex items-center">
        {#if loading}
            <LoaderCircle class="mr-2 h-4 w-4 animate-spin"/>
        {/if}

        {#if cooldown > 0}
            Resend in {cooldown}s
        {:else}
            Resend Verification Email
        {/if}
    </span>
</Button>
