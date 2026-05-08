<script lang="ts">
    import Button from '$lib/components/ui/button/button.svelte';
    import {initiateOAuth} from "$lib/api/authentication/AuthenticationController.ts";
    import type {OAuthProvider} from "$lib/api/models.ts";
    import {getProviderMeta} from "$lib/utils/provider-meta";

    interface Props {
        provider: OAuthProvider;
        disabled?: boolean;
        class?: string;
    }

    let {provider, disabled = false, class: className = ''}: Props = $props();

    function handleClick(): void {
        initiateOAuth(provider);
    }

    const meta = $derived(getProviderMeta(provider.toUpperCase() as 'GOOGLE' | 'GITHUB'));
    const label: string = $derived(meta.label);
    const brandIcon: string | undefined = $derived(meta.brandIcon);
</script>

<Button
        type="button"
        variant="outline"
        class="bg-background/50 border-border hover:bg-accent/10 {className}"
        onclick={handleClick}
        {disabled}
>
	{#if brandIcon}
		<span class="mr-2">
			{@html brandIcon}
		</span>
	{/if}
    {label}
</Button>
