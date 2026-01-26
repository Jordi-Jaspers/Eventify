<!--
  AppLogo Component

  Minimalistic logo with Radar icon - modern, secure, monitoring-focused.

  Props:
  - size: 'small' | 'medium' | 'large' (default: 'medium')
  - variant: 'full' | 'icon' | 'text' (default: 'full')
  - subtitle: Optional subtitle text
  - href: Optional link (defaults to undefined for no link)
  - class: Optional additional classes

  Usage:
  <AppLogo size="large" subtitle="Real-time monitoring" />
  <AppLogo size="small" variant="icon" />
  <AppLogo variant="text" />
-->
<script lang="ts">
    import { Radar } from '@lucide/svelte';
    import { cn } from '$lib/utils';

    interface Props {
        size?: 'small' | 'medium' | 'large';
        variant?: 'full' | 'icon' | 'text';
        subtitle?: string;
        href?: string;
        class?: string;
    }

    let { size = 'medium', variant = 'full', subtitle, href, class: className }: Props = $props();

    const sizes = {
        small: {
            icon: 'h-5 w-5',
            text: 'text-lg',
            gap: 'gap-2',
            subtitle: 'text-xs'
        },
        medium: {
            icon: 'h-8 w-8',
            text: 'text-3xl',
            gap: 'gap-3',
            subtitle: 'text-sm'
        },
        large: {
            icon: 'h-12 w-12',
            text: 'text-5xl',
            gap: 'gap-4',
            subtitle: 'text-base'
        }
    };

    const currentSize = $derived(sizes[size]);
    const showIcon = $derived(variant === 'full' || variant === 'icon');
    const showText = $derived(variant === 'full' || variant === 'text');
</script>

{#snippet logoContent()}
    <div class={cn("flex items-center", currentSize.gap, className)}>
        {#if showIcon}
            <Radar class={cn(currentSize.icon, "text-primary")} />
        {/if}
        {#if showText}
            <span class={cn(currentSize.text, "font-light tracking-wide")}>eventify</span>
        {/if}
    </div>
    {#if subtitle}
        <p class={cn("text-muted-foreground mt-2", currentSize.subtitle)}>
            {subtitle}
        </p>
    {/if}
{/snippet}

<div class="text-center">
    {#if href}
        <a {href} class="inline-flex flex-col items-center hover:opacity-80 transition-opacity">
            {@render logoContent()}
        </a>
    {:else}
        <div class="inline-flex flex-col items-center">
            {@render logoContent()}
        </div>
    {/if}
</div>
