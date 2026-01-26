<script lang="ts">
    import { env } from '$env/dynamic/public';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import Button from '$lib/components/ui/button/button.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import { Sun, Moon, Check, X, Loader2 } from '@lucide/svelte';
    
    // Redirect if not in dev mode
    const isDev = env.PUBLIC_SHOW_DEV_CREDENTIALS === 'true';
    
    onMount(() => {
        if (!isDev) {
            goto('/');
        }
    });
    
    // Theme toggle
    let isDarkMode = $state(true);
    
    function toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
    }
    
    onMount(() => {
        isDarkMode = document.documentElement.classList.contains('dark');
    });
</script>

<svelte:head>
    <title>Component Playbook - Eventify Dev</title>
</svelte:head>

{#if isDev}
<div class="container mx-auto py-12 px-4 max-w-6xl">
    <!-- Header -->
    <div class="flex items-center justify-between mb-12">
        <div>
            <h1 class="text-4xl font-bold mb-2">Component Playbook</h1>
            <p class="text-muted-foreground">Design system for Eventify</p>
        </div>
        <Button variant="outline" onclick={toggleTheme}>
            {#if isDarkMode}
                <Sun class="h-4 w-4 mr-2" />
                Light Mode
            {:else}
                <Moon class="h-4 w-4 mr-2" />
                Dark Mode
            {/if}
        </Button>
    </div>

    <!-- SECTION: Logo -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Logo</h2>
        <p class="text-muted-foreground mb-6">Minimalistic Radar icon with light typography</p>
        
        <!-- Size Variants -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Size Variants</CardTitle>
                <CardDescription>small, medium, large</CardDescription>
            </CardHeader>
            <CardContent>
                <div class="flex flex-wrap items-end justify-center gap-12 py-4">
                    <div class="text-center">
                        <AppLogo size="small" />
                        <p class="text-xs text-muted-foreground mt-4">Small (sidebar)</p>
                    </div>
                    <div class="text-center">
                        <AppLogo size="medium" />
                        <p class="text-xs text-muted-foreground mt-4">Medium (auth pages)</p>
                    </div>
                    <div class="text-center">
                        <AppLogo size="large" />
                        <p class="text-xs text-muted-foreground mt-4">Large (landing)</p>
                    </div>
                </div>
            </CardContent>
        </Card>

        <!-- Display Variants -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Display Variants</CardTitle>
                <CardDescription>full, icon, text</CardDescription>
            </CardHeader>
            <CardContent>
                <div class="flex flex-wrap items-center justify-center gap-12 py-4">
                    <div class="text-center">
                        <AppLogo variant="full" />
                        <p class="text-xs text-muted-foreground mt-4">Full (default)</p>
                    </div>
                    <div class="text-center">
                        <AppLogo variant="icon" />
                        <p class="text-xs text-muted-foreground mt-4">Icon only</p>
                    </div>
                    <div class="text-center">
                        <AppLogo variant="text" />
                        <p class="text-xs text-muted-foreground mt-4">Text only</p>
                    </div>
                </div>
            </CardContent>
        </Card>

        <!-- With Subtitle -->
        <Card class="border-border/50">
            <CardHeader>
                <CardTitle class="text-lg">With Subtitle</CardTitle>
                <CardDescription>Optional subtitle for context</CardDescription>
            </CardHeader>
            <CardContent class="py-8">
                <div class="flex justify-center">
                    <AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />
                </div>
            </CardContent>
        </Card>
    </section>

    <!-- SECTION: Buttons -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Buttons</h2>
        <p class="text-muted-foreground mb-6">Glass variant for CTAs, plus existing variants</p>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Default Variant (Glass) -->
            <Card class="border-border/50 border-primary/30">
                <CardHeader>
                    <CardTitle class="text-lg flex items-center gap-2">
                        Default Variant
                        <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">GLASS</span>
                    </CardTitle>
                    <CardDescription>Primary CTA with glassmorphism effect</CardDescription>
                </CardHeader>
                <CardContent class="space-y-4">
                    <Button variant="default" class="w-full">
                        Sign In
                    </Button>
                    <Button variant="default" class="w-full">
                        <Loader2 class="h-4 w-4 animate-spin" />
                        Loading...
                    </Button>
                    <Button variant="default" class="w-full" disabled>
                        Disabled
                    </Button>
                    <div class="text-xs text-muted-foreground p-3 bg-muted/30 rounded-md font-mono">
                        variant="default"
                    </div>
                </CardContent>
            </Card>

            <!-- All Variants -->
            <Card class="border-border/50">
                <CardHeader>
                    <CardTitle class="text-lg">All Variants</CardTitle>
                    <CardDescription>Complete button variant library</CardDescription>
                </CardHeader>
                <CardContent class="space-y-3">
                    <div class="flex items-center gap-3">
                        <Button variant="default" class="flex-1">Default</Button>
                        <code class="text-xs text-muted-foreground">default (glass)</code>
                    </div>
                    <div class="flex items-center gap-3">
                        <Button variant="secondary" class="flex-1">Secondary</Button>
                        <code class="text-xs text-muted-foreground">secondary</code>
                    </div>
                    <div class="flex items-center gap-3">
                        <Button variant="outline" class="flex-1">Outline</Button>
                        <code class="text-xs text-muted-foreground">outline</code>
                    </div>
                    <div class="flex items-center gap-3">
                        <Button variant="ghost" class="flex-1">Ghost</Button>
                        <code class="text-xs text-muted-foreground">ghost</code>
                    </div>
                    <div class="flex items-center gap-3">
                        <Button variant="link" class="flex-1">Link</Button>
                        <code class="text-xs text-muted-foreground">link</code>
                    </div>
                    <div class="flex items-center gap-3">
                        <Button variant="destructive" class="flex-1">Destructive</Button>
                        <code class="text-xs text-muted-foreground">destructive</code>
                    </div>
                </CardContent>
            </Card>
        </div>

        <!-- Button Sizes -->
        <Card class="border-border/50 mt-6">
            <CardHeader>
                <CardTitle class="text-lg">Button Sizes</CardTitle>
                <CardDescription>sm, default, lg, icon variants</CardDescription>
            </CardHeader>
            <CardContent>
                <div class="flex flex-wrap items-center gap-4">
                    <Button size="sm">Small</Button>
                    <Button size="default">Default</Button>
                    <Button size="lg">Large</Button>
                    <Button size="icon"><Check class="h-4 w-4" /></Button>
                    <Button size="icon-sm"><Check class="h-4 w-4" /></Button>
                    <Button size="icon-lg"><Check class="h-4 w-4" /></Button>
                </div>
            </CardContent>
        </Card>
    </section>

    <!-- SECTION: Cards -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Cards</h2>
        <p class="text-muted-foreground mb-6">Glassmorphism card styles</p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card class="border-border/50 bg-card">
                <CardHeader>
                    <CardTitle class="text-lg">Standard</CardTitle>
                    <CardDescription>Default card</CardDescription>
                </CardHeader>
                <CardContent>
                    <p class="text-sm text-muted-foreground">bg-card</p>
                </CardContent>
            </Card>

            <Card class="border-border/50 bg-card/50 backdrop-blur-xl">
                <CardHeader>
                    <CardTitle class="text-lg">Glass</CardTitle>
                    <CardDescription>Semi-transparent with blur</CardDescription>
                </CardHeader>
                <CardContent>
                    <p class="text-sm text-muted-foreground">bg-card/50 backdrop-blur-xl</p>
                </CardContent>
            </Card>

            <Card class="border-border/30 bg-card/30 backdrop-blur-md">
                <CardHeader>
                    <CardTitle class="text-lg">Light Glass</CardTitle>
                    <CardDescription>More transparency</CardDescription>
                </CardHeader>
                <CardContent>
                    <p class="text-sm text-muted-foreground">bg-card/30 backdrop-blur-md</p>
                </CardContent>
            </Card>
        </div>
    </section>

    <!-- SECTION: Usage Examples -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Usage Examples</h2>
        <p class="text-muted-foreground mb-6">Common component combinations</p>
        
        <!-- Auth Card Example -->
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl max-w-md mx-auto">
            <CardHeader class="text-center pb-2">
                <div class="mb-4">
                    <AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />
                </div>
            </CardHeader>
            <CardContent class="space-y-4">
                <div class="space-y-2">
                    <label class="text-sm font-medium">Email</label>
                    <input 
                        type="email" 
                        placeholder="you@example.com"
                        class="w-full h-10 px-3 rounded-md border border-border/50 bg-muted/30 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                </div>
                <div class="space-y-2">
                    <label class="text-sm font-medium">Password</label>
                    <input 
                        type="password" 
                        placeholder="Enter your password"
                        class="w-full h-10 px-3 rounded-md border border-border/50 bg-muted/30 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                </div>
                <Button class="w-full">
                    Sign In
                </Button>
                <p class="text-center text-sm text-muted-foreground">
                    Don't have an account? <a href="#" class="text-primary hover:underline">Sign up</a>
                </p>
            </CardContent>
        </Card>
    </section>

    <!-- Footer -->
    <div class="text-center text-sm text-muted-foreground border-t border-border/50 pt-8">
        <p>This page is only visible in development mode</p>
        <p class="mt-1">Toggle light/dark mode to verify consistency</p>
    </div>
</div>
{:else}
<div class="flex items-center justify-center min-h-screen">
    <p class="text-muted-foreground">This page is only available in development mode.</p>
</div>
{/if}
