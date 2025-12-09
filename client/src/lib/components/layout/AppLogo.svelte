<!--
  AppLogo Component

  Reusable logo/branding component with gradient styling.

  Props:
  - size: 'small' | 'medium' | 'large' (default: 'medium')
  - subtitle: Optional subtitle text
  - href: Optional link (defaults to '#' if not provided)

  Usage:
  <AppLogo size="large" subtitle="Real-time monitoring and event tracking" />
-->
<script lang="ts">
    import {Activity} from '@lucide/svelte';

    interface Props {
        size?: 'small' | 'medium' | 'large';
        subtitle?: string;
        href?: string;
    }

    let {size = 'medium', subtitle, href = '#'}: Props = $props();

    const sizes = {
        small: {
            container: 'w-10 h-10 rounded-xl',
            icon: 'w-5 h-5',
            title: 'text-xl'
        },
        medium: {
            container: 'w-16 h-16 rounded-2xl',
            icon: 'w-8 h-8',
            title: 'text-3xl'
        },
        large: {
            container: 'w-20 h-20 rounded-3xl',
            icon: 'w-10 h-10',
            title: 'text-4xl'
        }
    };

    const currentSize = $derived(sizes[size]);
</script>

<div class="text-center">
    <a {href} class="inline-block">
        <div
                class="inline-flex items-center justify-center {currentSize.container} mb-4 bg-gradient-to-br from-primary to-accent shadow-lg transition-transform hover:scale-105"
        >
            <Activity class="{currentSize.icon} text-primary-foreground"/>
        </div>
    </a>

    <h1 class="{currentSize.title} font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
        Eventify
    </h1>

    {#if subtitle}
        <p class="text-muted-foreground mt-2">
            {subtitle}
        </p>
    {/if}
</div>
