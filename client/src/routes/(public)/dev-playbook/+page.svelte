<script lang="ts">
    import { env } from '$env/dynamic/public';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import Button from '$lib/components/ui/button/button.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import { Sun, Moon, Check, X, Loader2, ArrowLeft, GripVertical, Radio, Folder, ChevronDown, Edit, Trash2 } from '@lucide/svelte';
    import { Badge } from '$lib/components/ui/badge';
    import { DateTimePicker } from '$lib/components/ui/date-time-picker';
    
    // DateTimePicker state
    let dateTimeValue1: string = $state('');
    let dateTimeValue2: string = $state(new Date().toISOString());
    let dateTimeValue3: string = $state('');
    
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
        <div class="flex items-center gap-3">
            <Button variant="ghost" href="/">
                <ArrowLeft class="h-4 w-4 mr-2" />
                Back to App
            </Button>
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

    <!-- SECTION: Typography -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Typography</h2>
        <p class="text-muted-foreground mb-6">Consistent text styles across the application</p>
        
        <!-- Headings -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Headings</CardTitle>
                <CardDescription>Page titles and section headers</CardDescription>
            </CardHeader>
            <CardContent class="space-y-6">
                <div class="space-y-1">
                    <h1 class="text-5xl font-bold">Heading 1</h1>
                    <code class="text-xs text-muted-foreground">text-5xl font-bold</code>
                </div>
                <div class="space-y-1">
                    <h2 class="text-4xl font-bold">Heading 2</h2>
                    <code class="text-xs text-muted-foreground">text-4xl font-bold</code>
                </div>
                <div class="space-y-1">
                    <h3 class="text-3xl font-semibold">Heading 3</h3>
                    <code class="text-xs text-muted-foreground">text-3xl font-semibold</code>
                </div>
                <div class="space-y-1">
                    <h4 class="text-2xl font-semibold">Heading 4</h4>
                    <code class="text-xs text-muted-foreground">text-2xl font-semibold</code>
                </div>
                <div class="space-y-1">
                    <h5 class="text-xl font-semibold">Heading 5</h5>
                    <code class="text-xs text-muted-foreground">text-xl font-semibold</code>
                </div>
                <div class="space-y-1">
                    <h6 class="text-lg font-medium">Heading 6</h6>
                    <code class="text-xs text-muted-foreground">text-lg font-medium</code>
                </div>
            </CardContent>
        </Card>

        <!-- Body Text -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Body Text</CardTitle>
                <CardDescription>Paragraph and content styles</CardDescription>
            </CardHeader>
            <CardContent class="space-y-6">
                <div class="space-y-1">
                    <p class="text-lg">Large body text for emphasis or introductions.</p>
                    <code class="text-xs text-muted-foreground">text-lg</code>
                </div>
                <div class="space-y-1">
                    <p class="text-base">Default body text for general content. This is the standard size for most paragraphs and descriptions throughout the application.</p>
                    <code class="text-xs text-muted-foreground">text-base (default)</code>
                </div>
                <div class="space-y-1">
                    <p class="text-sm">Small text for secondary information and supporting content.</p>
                    <code class="text-xs text-muted-foreground">text-sm</code>
                </div>
                <div class="space-y-1">
                    <p class="text-xs">Extra small text for captions, labels, and metadata.</p>
                    <code class="text-xs text-muted-foreground">text-xs</code>
                </div>
            </CardContent>
        </Card>

        <!-- Text Colors -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Text Colors</CardTitle>
                <CardDescription>Semantic color usage</CardDescription>
            </CardHeader>
            <CardContent class="space-y-4">
                <div class="flex items-center gap-4">
                    <p class="text-foreground flex-1">Primary foreground text</p>
                    <code class="text-xs text-muted-foreground">text-foreground</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-muted-foreground flex-1">Muted text for secondary content</p>
                    <code class="text-xs text-muted-foreground">text-muted-foreground</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-primary flex-1">Primary color for links and accents</p>
                    <code class="text-xs text-muted-foreground">text-primary</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-destructive flex-1">Destructive for errors and warnings</p>
                    <code class="text-xs text-muted-foreground">text-destructive</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-green-500 flex-1">Success for confirmations</p>
                    <code class="text-xs text-muted-foreground">text-green-500</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-amber-500 flex-1">Warning for cautions</p>
                    <code class="text-xs text-muted-foreground">text-amber-500</code>
                </div>
            </CardContent>
        </Card>

        <!-- Font Weights -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Font Weights</CardTitle>
                <CardDescription>Available weight variants</CardDescription>
            </CardHeader>
            <CardContent class="space-y-4">
                <div class="flex items-center gap-4">
                    <p class="text-lg font-light flex-1">Light weight (logo text)</p>
                    <code class="text-xs text-muted-foreground">font-light</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-lg font-normal flex-1">Normal weight (body)</p>
                    <code class="text-xs text-muted-foreground">font-normal</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-lg font-medium flex-1">Medium weight (labels)</p>
                    <code class="text-xs text-muted-foreground">font-medium</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-lg font-semibold flex-1">Semibold weight (subheadings)</p>
                    <code class="text-xs text-muted-foreground">font-semibold</code>
                </div>
                <div class="flex items-center gap-4">
                    <p class="text-lg font-bold flex-1">Bold weight (headings)</p>
                    <code class="text-xs text-muted-foreground">font-bold</code>
                </div>
            </CardContent>
        </Card>

        <!-- Special Text Styles -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Special Styles</CardTitle>
                <CardDescription>Decorative and utility text styles</CardDescription>
            </CardHeader>
            <CardContent class="space-y-6">
                <div class="space-y-1">
                    <p class="text-2xl font-light tracking-wide">eventify</p>
                    <code class="text-xs text-muted-foreground">font-light tracking-wide (logo style)</code>
                </div>
                <div class="space-y-1">
                    <p class="text-lg uppercase tracking-widest text-muted-foreground">Section Label</p>
                    <code class="text-xs text-muted-foreground">uppercase tracking-widest</code>
                </div>
                <div class="space-y-1">
                    <p class="font-mono text-sm bg-muted/30 px-2 py-1 rounded inline-block">code.example()</p>
                    <code class="text-xs text-muted-foreground ml-2">font-mono (code/technical)</code>
                </div>
                <div class="space-y-1">
                    <p class="text-sm leading-relaxed max-w-md">Long form content with relaxed line height for better readability. Use this for paragraphs that span multiple lines.</p>
                    <code class="text-xs text-muted-foreground">leading-relaxed</code>
                </div>
                <div class="space-y-1">
                    <p class="text-base"><span class="text-primary hover:underline cursor-pointer">Inline link style</span> within body text.</p>
                    <code class="text-xs text-muted-foreground">text-primary hover:underline</code>
                </div>
            </CardContent>
        </Card>

        <!-- Use Cases -->
        <Card class="border-border/50 border-primary/30 mb-6">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    Use Cases
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">PATTERNS</span>
                </CardTitle>
                <CardDescription>Real-world typography patterns used throughout the application</CardDescription>
            </CardHeader>
            <CardContent class="space-y-8">
                <!-- Page Title Pattern -->
                <div class="p-4 rounded-lg border border-primary/30 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Page Title</p>
                    <div class="space-y-1">
                        <h1 class="text-3xl font-bold text-primary">
                            Organization Channels
                        </h1>
                        <p class="text-muted-foreground mt-2">Manage channels for your organization</p>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">h1: text-3xl font-bold text-primary | p: text-muted-foreground mt-2</code>
                </div>

                <!-- Page Header Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Page Header (with icon)</p>
                    <div class="flex items-center gap-3">
                        <div class="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
                            <Sun class="h-5 w-5 text-primary" />
                        </div>
                        <div>
                            <h1 class="text-2xl font-semibold">Dashboard</h1>
                            <p class="text-sm text-muted-foreground">Monitor your events in real-time.</p>
                        </div>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">icon: bg-primary/10 text-primary | h1: text-2xl font-semibold | p: text-sm text-muted-foreground</code>
                </div>

                <!-- Card Title Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Card Header</p>
                    <div class="space-y-1">
                        <h3 class="text-lg font-semibold">Event Statistics</h3>
                        <p class="text-sm text-muted-foreground">Overview of events in the last 24 hours.</p>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">h3: text-lg font-semibold | p: text-sm text-muted-foreground</code>
                </div>

                <!-- Section Title Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Section Title</p>
                    <div class="space-y-1">
                        <h2 class="text-xl font-semibold">Recent Activity</h2>
                        <p class="text-sm text-muted-foreground">Your latest events and updates.</p>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">h2: text-xl font-semibold | p: text-sm text-muted-foreground</code>
                </div>

                <!-- Stat Card Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Stat/Metric Card</p>
                    <div class="space-y-1">
                        <p class="text-sm font-medium text-muted-foreground">Total Events</p>
                        <p class="text-3xl font-bold">12,456</p>
                        <p class="text-xs text-green-500">+12.5% from last week</p>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">label: text-sm font-medium text-muted-foreground | value: text-3xl font-bold | change: text-xs text-green-500</code>
                </div>

                <!-- Form Label Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Form Labels</p>
                    <div class="space-y-3">
                        <div class="space-y-1">
                            <label class="text-sm font-medium">Email Address</label>
                            <p class="text-xs text-muted-foreground">We'll never share your email.</p>
                        </div>
                        <div class="space-y-1">
                            <label class="text-sm font-medium">Password <span class="text-destructive">*</span></label>
                            <p class="text-xs text-destructive">Password is required.</p>
                        </div>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">label: text-sm font-medium | hint: text-xs text-muted-foreground | error: text-xs text-destructive | required: text-destructive</code>
                </div>

                <!-- Empty State Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Empty State</p>
                    <div class="text-center py-4 space-y-2">
                        <p class="text-lg font-medium">No events found</p>
                        <p class="text-sm text-muted-foreground">Start by creating your first channel to receive events.</p>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">title: text-lg font-medium | description: text-sm text-muted-foreground</code>
                </div>

                <!-- Alert/Toast Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Alerts & Toasts</p>
                    <div class="space-y-3">
                        <div class="p-3 rounded-md bg-green-500/10 border border-green-500/30">
                            <p class="text-sm font-medium text-green-500">Success!</p>
                            <p class="text-sm text-muted-foreground">Your changes have been saved.</p>
                        </div>
                        <div class="p-3 rounded-md bg-destructive/10 border border-destructive/30">
                            <p class="text-sm font-medium text-destructive">Error</p>
                            <p class="text-sm text-muted-foreground">Failed to save changes. Please try again.</p>
                        </div>
                        <div class="p-3 rounded-md bg-amber-500/10 border border-amber-500/30">
                            <p class="text-sm font-medium text-amber-500">Warning</p>
                            <p class="text-sm text-muted-foreground">This action cannot be undone.</p>
                        </div>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">title: text-sm font-medium text-[color] | message: text-sm text-muted-foreground</code>
                </div>

                <!-- Table Header Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Table Headers & Cells</p>
                    <div class="overflow-hidden rounded-md border border-border/50">
                        <table class="w-full">
                            <thead class="bg-muted/30">
                                <tr>
                                    <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-3 py-2">Name</th>
                                    <th class="text-left text-xs font-medium text-muted-foreground uppercase tracking-wider px-3 py-2">Status</th>
                                    <th class="text-right text-xs font-medium text-muted-foreground uppercase tracking-wider px-3 py-2">Events</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr class="border-t border-border/50">
                                    <td class="text-sm font-medium px-3 py-2">Production</td>
                                    <td class="text-sm text-green-500 px-3 py-2">Active</td>
                                    <td class="text-sm text-muted-foreground text-right px-3 py-2">1,234</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">th: text-xs font-medium text-muted-foreground uppercase tracking-wider | td: text-sm</code>
                </div>

                <!-- Badge/Tag Pattern -->
                <div class="p-4 rounded-lg border border-border/50 bg-muted/20">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground mb-2">Badges & Tags</p>
                    <div class="flex flex-wrap gap-2">
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-primary/20 text-primary">Primary</span>
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-green-500/20 text-green-500">Success</span>
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-500">Warning</span>
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-destructive/20 text-destructive">Error</span>
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-muted text-muted-foreground">Muted</span>
                    </div>
                    <code class="text-xs text-muted-foreground mt-3 block">text-xs font-medium px-2 py-0.5 rounded-full bg-[color]/20 text-[color]</code>
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

    <!-- SECTION: List Item Rows -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">List Item Rows</h2>
        <p class="text-muted-foreground mb-6">Preferred pattern for DataTable rows and list items - minimal, scannable, hover-reveal actions</p>
        
        <!-- Basic List Item -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    Basic List Item
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">PREFERRED</span>
                </CardTitle>
                <CardDescription>Minimal row with hover actions - use this for DataTable rows</CardDescription>
            </CardHeader>
            <CardContent class="space-y-1">
                <!-- Example items -->
                <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
                    <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium truncate text-foreground">Production API</p>
                        <p class="text-xs text-muted-foreground truncate">Main production channel for API events</p>
                    </div>
                    <Badge variant="default" class="shrink-0">Active</Badge>
                    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-primary">
                            <Edit class="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-destructive">
                            <Trash2 class="h-4 w-4" />
                        </Button>
                    </div>
                </div>
                <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
                    <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium truncate text-foreground">Staging Environment</p>
                        <p class="text-xs text-muted-foreground truncate">Pre-production testing channel</p>
                    </div>
                    <Badge variant="secondary" class="shrink-0">Paused</Badge>
                    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-primary">
                            <Edit class="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-destructive">
                            <Trash2 class="h-4 w-4" />
                        </Button>
                    </div>
                </div>
                <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
                    <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium truncate text-foreground">Development</p>
                        <p class="text-xs text-muted-foreground truncate">Local development events</p>
                    </div>
                    <Badge variant="default" class="shrink-0">Active</Badge>
                    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-primary">
                            <Edit class="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-destructive">
                            <Trash2 class="h-4 w-4" />
                        </Button>
                    </div>
                </div>
            </CardContent>
        </Card>

        <!-- Draggable List Item -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Draggable List Item</CardTitle>
                <CardDescription>With drag handle that appears on hover</CardDescription>
            </CardHeader>
            <CardContent class="space-y-1">
                <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move">
                    <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                        <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                    </div>
                    <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium truncate">Production API</p>
                        <p class="text-xs text-muted-foreground truncate">Main production channel</p>
                    </div>
                    <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                        <X class="h-4 w-4" />
                    </Button>
                </div>
                <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move">
                    <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                        <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                    </div>
                    <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium truncate">Staging Environment</p>
                        <p class="text-xs text-muted-foreground truncate">Pre-production testing</p>
                    </div>
                    <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                        <X class="h-4 w-4" />
                    </Button>
                </div>
            </CardContent>
        </Card>

        <!-- Collapsible Group -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg">Collapsible Group</CardTitle>
                <CardDescription>For nested/grouped items with expand/collapse</CardDescription>
            </CardHeader>
            <CardContent>
                <div class="rounded-md border border-border/30">
                    <div class="group flex items-center gap-3 px-3 py-2.5 bg-muted/20 hover:bg-muted/40 transition-all cursor-move">
                        <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                            <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                        </div>
                        <button class="shrink-0 text-muted-foreground hover:text-foreground">
                            <ChevronDown class="h-4 w-4" />
                        </button>
                        <Folder class="h-4 w-4 text-primary/70 shrink-0" />
                        <div class="flex-1 min-w-0">
                            <p class="font-medium text-sm truncate">Backend Services</p>
                            <p class="text-xs text-muted-foreground">3 channels</p>
                        </div>
                        <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                            <X class="h-4 w-4" />
                        </Button>
                    </div>
                    <div class="pl-10 pr-3 pb-3 pt-2 space-y-1">
                        <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move">
                            <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                                <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                            </div>
                            <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                            <div class="flex-1 min-w-0">
                                <p class="text-sm font-medium truncate">API Gateway</p>
                            </div>
                            <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                                <X class="h-4 w-4" />
                            </Button>
                        </div>
                        <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move">
                            <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                                <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                            </div>
                            <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                            <div class="flex-1 min-w-0">
                                <p class="text-sm font-medium truncate">Auth Service</p>
                            </div>
                            <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                                <X class="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>

        <!-- Standard Card Row -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    Standard Card Row
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">FOR OVERVIEWS</span>
                </CardTitle>
                <CardDescription>More visual weight for main list pages - subtle border, larger icon, visible actions</CardDescription>
            </CardHeader>
            <CardContent class="space-y-2">
                <div class="group flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/30 hover:bg-card/50 hover:border-border transition-all">
                    <Radio class="h-5 w-5 text-primary shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="font-medium truncate">Production API</p>
                        <p class="text-sm text-muted-foreground truncate">Main production channel for API events</p>
                    </div>
                    <Badge variant="default">Active</Badge>
                    <span class="text-sm text-muted-foreground whitespace-nowrap">Jan 15, 2026</span>
                    <div class="flex items-center gap-1">
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                            <Edit class="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-destructive">
                            <Trash2 class="h-4 w-4" />
                        </Button>
                    </div>
                </div>
                <div class="group flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/30 hover:bg-card/50 hover:border-border transition-all">
                    <Radio class="h-5 w-5 text-primary shrink-0" />
                    <div class="flex-1 min-w-0">
                        <p class="font-medium truncate">Staging Environment</p>
                        <p class="text-sm text-muted-foreground truncate">Pre-production testing channel</p>
                    </div>
                    <Badge variant="secondary">Paused</Badge>
                    <span class="text-sm text-muted-foreground whitespace-nowrap">Jan 10, 2026</span>
                    <div class="flex items-center gap-1">
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                            <Edit class="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-destructive">
                            <Trash2 class="h-4 w-4" />
                        </Button>
                    </div>
                </div>
                <code class="text-xs text-muted-foreground block p-2 bg-muted/30 rounded mt-4">
                    border-border/50, bg-card/30, h-5 w-5 icon (no container), visible actions
                </code>
            </CardContent>
        </Card>

        <!-- Comparison: Minimal vs Standard -->
        <Card class="border-border/50 border-primary/30">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    When to Use Each Style
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">REFERENCE</span>
                </CardTitle>
                <CardDescription>Choose based on context and data density</CardDescription>
            </CardHeader>
            <CardContent class="space-y-6">
                <!-- Minimal -->
                <div class="space-y-2">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground flex items-center gap-2">
                        Minimal - Dense lists, builders, config panels
                    </p>
                    <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
                        <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                        <div class="flex-1 min-w-0">
                            <p class="text-sm font-medium truncate">Production API</p>
                            <p class="text-xs text-muted-foreground truncate">Main production channel</p>
                        </div>
                        <Badge variant="default">Active</Badge>
                        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                            <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground hover:text-primary">
                                <Edit class="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                    <code class="text-xs text-muted-foreground block p-2 bg-muted/30 rounded">
                        No border, hover:bg-muted/50, h-4 w-4 icon, hover-reveal actions
                    </code>
                </div>

                <!-- Standard -->
                <div class="space-y-2">
                    <p class="text-xs uppercase tracking-widest text-muted-foreground flex items-center gap-2">
                        Standard - Main overviews, dashboards
                    </p>
                    <div class="group flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/30 hover:bg-card/50 hover:border-border transition-all">
                        <Radio class="h-5 w-5 text-primary shrink-0" />
                        <div class="flex-1 min-w-0">
                            <p class="font-medium truncate">Production API</p>
                            <p class="text-sm text-muted-foreground truncate">Main production channel</p>
                        </div>
                        <Badge variant="default">Active</Badge>
                        <div class="flex items-center gap-1">
                            <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                <Edit class="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                    <code class="text-xs text-muted-foreground block p-2 bg-muted/30 rounded">
                        border-border/50, bg-card/30, h-5 w-5 icon, visible actions
                    </code>
                </div>

                <!-- Avoid -->
                <div class="space-y-2">
                    <p class="text-xs uppercase tracking-widest text-destructive flex items-center gap-2">
                        <X class="h-3 w-3" /> Avoid - Icon containers, gradients, blur
                    </p>
                    <div class="flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/50 backdrop-blur-sm opacity-60">
                        <div class="h-10 w-10 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center shrink-0">
                            <Radio class="h-5 w-5 text-primary" />
                        </div>
                        <div class="flex-1 min-w-0">
                            <p class="font-medium truncate">Production API</p>
                            <p class="text-sm text-muted-foreground truncate">Main production channel</p>
                        </div>
                    </div>
                    <code class="text-xs text-destructive block p-2 bg-destructive/10 rounded">
                        Don't use: icon containers, gradients, backdrop-blur on rows
                    </code>
                </div>
            </CardContent>
        </Card>
    </section>

    <!-- SECTION: DataTable -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">DataTable</h2>
        <p class="text-muted-foreground mb-6">Server-side paginated tables with filtering, sorting, and custom row rendering</p>
        
        <!-- DataTable Structure -->
        <Card class="border-border/50 mb-6">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    DataTable Row Pattern
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">STANDARD</span>
                </CardTitle>
                <CardDescription>Use this exact styling for all DataTable row snippets</CardDescription>
            </CardHeader>
            <CardContent>
                <!-- Mock DataTable header -->
                <div class="rounded-lg border border-border/50 overflow-hidden">
                    <!-- Header row (simulated) -->
                    <div class="grid grid-cols-12 gap-4 px-4 py-3 bg-muted/30 text-sm font-medium text-muted-foreground border-b border-border/50">
                        <div class="col-span-3">Channel</div>
                        <div class="col-span-5">Description</div>
                        <div class="col-span-1">Status</div>
                        <div class="col-span-2">Created</div>
                        <div class="col-span-1"></div>
                    </div>
                    
                    <!-- Data rows -->
                    <div class="divide-y divide-border/30">
                        <div class="grid grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
                            <div class="col-span-3">
                                <div class="flex items-center gap-3">
                                    <Radio class="h-5 w-5 text-primary shrink-0" />
                                    <div class="min-w-0">
                                        <div class="font-medium truncate">Production API</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-span-5">
                                <span class="text-sm text-muted-foreground truncate">Main production channel for API events</span>
                            </div>
                            <div class="col-span-1">
                                <Badge variant="default">Active</Badge>
                            </div>
                            <div class="col-span-2">
                                <span class="text-sm text-muted-foreground">Jan 15, 2026</span>
                            </div>
                            <div class="col-span-1 flex items-center justify-end gap-1">
                                <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                    <Edit class="h-4 w-4" />
                                </Button>
                                <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-destructive">
                                    <Trash2 class="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                        
                        <div class="grid grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
                            <div class="col-span-3">
                                <div class="flex items-center gap-3">
                                    <Radio class="h-5 w-5 text-primary shrink-0" />
                                    <div class="min-w-0">
                                        <div class="font-medium truncate">Staging Environment</div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-span-5">
                                <span class="text-sm text-muted-foreground truncate">Pre-production testing channel</span>
                            </div>
                            <div class="col-span-1">
                                <Badge variant="secondary">Paused</Badge>
                            </div>
                            <div class="col-span-2">
                                <span class="text-sm text-muted-foreground">Jan 10, 2026</span>
                            </div>
                            <div class="col-span-1 flex items-center justify-end gap-1">
                                <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                    <Edit class="h-4 w-4" />
                                </Button>
                                <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-destructive">
                                    <Trash2 class="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>

        <!-- Code Example -->
        <Card class="border-border/50 border-primary/30 mb-6">
            <CardHeader>
                <CardTitle class="text-lg flex items-center gap-2">
                    Row Snippet Code
                    <span class="text-xs bg-primary/20 text-primary px-2 py-0.5 rounded">COPY THIS</span>
                </CardTitle>
                <CardDescription>Standard row template for DataTable components</CardDescription>
            </CardHeader>
            <CardContent>
                <pre class="text-xs bg-muted/30 p-4 rounded-lg overflow-x-auto font-mono leading-relaxed">
{`<DataTable {columns} service={dataTableService} title="All Channels" icon={Radio}>
  {#snippet row(channel: ChannelDetailsResponse)}
    <div
      class="grid grid-cols-1 md:grid-cols-12 items-center gap-4 px-4 py-3 
             hover:bg-muted/30 transition-all text-left w-full"
    >
      <!-- Icon + Name -->
      <div class="col-span-1 md:col-span-3">
        <div class="flex items-center gap-3">
          <Radio class="h-5 w-5 text-primary shrink-0" />
          <div class="min-w-0">
            <div class="font-medium truncate">{channel.name}</div>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="hidden md:flex md:col-span-5 items-center">
        <span class="text-sm text-muted-foreground truncate">
          {channel.description}
        </span>
      </div>

      <!-- Status -->
      <div class="col-span-1 md:col-span-1 flex items-center">
        <Badge variant={channel.status === 'ACTIVE' ? 'success' : 'secondary'}>
          {channel.status}
        </Badge>
      </div>

      <!-- Date -->
      <div class="col-span-1 md:col-span-2 flex items-center">
        <span class="text-sm text-muted-foreground">
          {formatDate(channel.createdAt)}
        </span>
      </div>

      <!-- Actions -->
      <div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
        <Button variant="ghost" size="icon" 
                class="h-8 w-8 text-muted-foreground hover:text-primary">
          <Edit class="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="icon" 
                class="h-8 w-8 text-muted-foreground hover:text-destructive">
          <Trash2 class="h-4 w-4" />
        </Button>
      </div>
    </div>
  {/snippet}
</DataTable>`}
                </pre>
            </CardContent>
        </Card>

        <!-- Key Rules -->
        <Card class="border-border/50">
            <CardHeader>
                <CardTitle class="text-lg">DataTable Row Rules</CardTitle>
                <CardDescription>Follow these conventions for consistent styling</CardDescription>
            </CardHeader>
            <CardContent>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <!-- Do's -->
                    <div class="space-y-3">
                        <p class="text-sm font-medium text-green-500 flex items-center gap-2">
                            <Check class="h-4 w-4" /> Do
                        </p>
                        <ul class="space-y-2 text-sm text-muted-foreground">
                            <li class="flex items-start gap-2">
                                <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                <span><code class="text-xs bg-muted/50 px-1 rounded">hover:bg-muted/30</code> for row hover</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                <span>Let DataTable component handle borders and dividers</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                <span>Icons: <code class="text-xs bg-muted/50 px-1 rounded">h-5 w-5 text-primary</code> directly (no container)</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                <span>Actions: <code class="text-xs bg-muted/50 px-1 rounded">text-muted-foreground hover:text-primary</code></span>
                            </li>
                            <li class="flex items-start gap-2">
                                <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                <span>Use <code class="text-xs bg-muted/50 px-1 rounded">transition-all</code> for smooth hover</span>
                            </li>
                        </ul>
                    </div>

                    <!-- Don'ts -->
                    <div class="space-y-3">
                        <p class="text-sm font-medium text-destructive flex items-center gap-2">
                            <X class="h-4 w-4" /> Don't
                        </p>
                        <ul class="space-y-2 text-sm text-muted-foreground">
                            <li class="flex items-start gap-2">
                                <X class="h-4 w-4 text-destructive shrink-0 mt-0.5" />
                                <span>No <code class="text-xs bg-muted/50 px-1 rounded">rounded-lg border</code> on individual rows</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <X class="h-4 w-4 text-destructive shrink-0 mt-0.5" />
                                <span>No <code class="text-xs bg-muted/50 px-1 rounded">bg-card/30</code> on rows (container handles it)</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <X class="h-4 w-4 text-destructive shrink-0 mt-0.5" />
                                <span>No <code class="text-xs bg-muted/50 px-1 rounded">backdrop-blur-sm</code> on rows</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <X class="h-4 w-4 text-destructive shrink-0 mt-0.5" />
                                <span>No <code class="text-xs bg-muted/50 px-1 rounded">shadow-sm hover:shadow-md</code></span>
                            </li>
                            <li class="flex items-start gap-2">
                                <X class="h-4 w-4 text-destructive shrink-0 mt-0.5" />
                                <span>No gradient icon containers</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </CardContent>
        </Card>
    </section>

    <!-- SECTION: Date Time Picker -->
    <section class="mb-16">
        <h2 class="text-2xl font-semibold mb-2">Date Time Picker</h2>
        <p class="text-muted-foreground mb-6">Calendar with time selection</p>
        
        <div class="grid gap-6 md:grid-cols-3">
            <!-- Empty State -->
            <Card class="border-border/50 bg-card/50 backdrop-blur-xl">
                <CardHeader>
                    <CardTitle class="text-base">Empty State</CardTitle>
                    <CardDescription>No date selected</CardDescription>
                </CardHeader>
                <CardContent>
                    <DateTimePicker 
                        value={dateTimeValue1}
                        onValueChange={(v) => dateTimeValue1 = v}
                        label="Start Date"
                        placeholder="Choose a date..."
                    />
                    <p class="text-xs text-muted-foreground mt-2">
                        Value: {dateTimeValue1 || '(empty)'}
                    </p>
                </CardContent>
            </Card>
            
            <!-- With Value -->
            <Card class="border-border/50 bg-card/50 backdrop-blur-xl">
                <CardHeader>
                    <CardTitle class="text-base">With Value</CardTitle>
                    <CardDescription>Pre-populated date</CardDescription>
                </CardHeader>
                <CardContent>
                    <DateTimePicker 
                        value={dateTimeValue2}
                        onValueChange={(v) => dateTimeValue2 = v}
                        label="Event Time"
                    />
                    <p class="text-xs text-muted-foreground mt-2">
                        Value: {dateTimeValue2 ? new Date(dateTimeValue2).toLocaleString() : '(empty)'}
                    </p>
                </CardContent>
            </Card>
            
            <!-- Disabled -->
            <Card class="border-border/50 bg-card/50 backdrop-blur-xl">
                <CardHeader>
                    <CardTitle class="text-base">Disabled</CardTitle>
                    <CardDescription>Non-interactive state</CardDescription>
                </CardHeader>
                <CardContent>
                    <DateTimePicker 
                        value={dateTimeValue3}
                        onValueChange={(v) => dateTimeValue3 = v}
                        label="Locked Date"
                        placeholder="Cannot edit..."
                        disabled
                    />
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
