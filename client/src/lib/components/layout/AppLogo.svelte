<!--
  AppLogo Component

  Minimalistic logo with Radar icon - modern, secure, monitoring-focused.

  Props:
  - size: 'small' | 'medium' | 'large' (default: 'medium')
  - variant: 'full' | 'icon' | 'text' (default: 'full')
  - subtitle: Optional subtitle text
  - href: Optional link (defaults to undefined for no link)
  - showEnvBadge: Show environment badge on non-production (default: true)
  - forceEnvironment: Override detected environment (for playbook demos)
  - class: Optional additional classes

  Usage:
  <AppLogo size="large" subtitle="Real-time monitoring" />
  <AppLogo size="small" variant="icon" />
  <AppLogo variant="text" showEnvBadge={false} />
  <AppLogo forceEnvironment="local" />
-->
<script lang="ts">
    import { Radar } from '@lucide/svelte';
    import { cn } from '$lib/utils';
    import { getEnvironment } from '$lib/config/env';
    import type { Environment } from '$lib/config/env';

    interface Props {
        size?: 'small' | 'medium' | 'large';
        variant?: 'full' | 'icon' | 'text';
        subtitle?: string;
        href?: string;
        showEnvBadge?: boolean;
        /** Override environment for demo/playbook purposes */
        forceEnvironment?: Environment;
        class?: string;
    }

    let { 
        size = 'medium', 
        variant = 'full', 
        subtitle, 
        href, 
        showEnvBadge = true,
        forceEnvironment,
        class: className 
    }: Props = $props();

    const sizes = {
        small: {
            icon: 'h-5 w-5',
            text: 'text-lg',
            gap: 'gap-2',
            subtitle: 'text-xs',
            badge: 'text-[6px] px-0.5 py-px'
        },
        medium: {
            icon: 'h-8 w-8',
            text: 'text-3xl',
            gap: 'gap-3',
            subtitle: 'text-sm',
            badge: 'text-[7px] px-1 py-px'
        },
        large: {
            icon: 'h-12 w-12',
            text: 'text-5xl',
            gap: 'gap-4',
            subtitle: 'text-base',
            badge: 'text-[8px] px-1 py-0.5'
        }
    };

    const currentSize = $derived(sizes[size]);
    const showIcon = $derived(variant === 'full' || variant === 'icon');
    const showText = $derived(variant === 'full' || variant === 'text');
    
    const environment: Environment = $derived(forceEnvironment ?? getEnvironment());
    const shouldShowBadge: boolean = $derived(showEnvBadge && environment !== 'production' && showIcon);
    
    // Badge config per environment
    const badgeConfig = $derived(() => {
        if (environment === 'local') {
            return { bg: 'bg-red-500', text: 'DEV' };
        }
        if (environment === 'test') {
            return { bg: 'bg-green-500', text: 'TST' };
        }
        return { bg: '', text: '' };
    });
</script>

{#snippet logoContent()}
    <div class={cn("flex items-center", currentSize.gap)}>
        {#if showIcon}
            <div class="relative">
                <Radar class={cn(currentSize.icon, "text-primary")} />
                {#if shouldShowBadge}
                    <span class={cn(
                        "absolute -top-1 -right-2 rounded font-bold text-white leading-none",
                        currentSize.badge,
                        badgeConfig().bg
                    )}>{badgeConfig().text}</span>
                {/if}
            </div>
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

{#if href}
    <a {href} class={cn("inline-flex flex-col items-center hover:opacity-80 transition-opacity", className)}>
        {@render logoContent()}
    </a>
{:else}
    <div class={cn("inline-flex flex-col items-center", className)}>
        {@render logoContent()}
    </div>
{/if}
