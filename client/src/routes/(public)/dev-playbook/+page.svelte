<script lang="ts">
    import { env } from '$env/dynamic/public';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import Button from '$lib/components/ui/button/button.svelte';
    import AppLogo from '$lib/components/layout/AppLogo.svelte';
    import { Sun, Moon, Check, X, Loader2, ArrowLeft, GripVertical, Radio, Folder, ChevronDown, Edit, Trash2, Menu, LayoutList, AlertCircle, Mail, User, Settings, ShieldCheck } from '@lucide/svelte';
    import { Badge } from '$lib/components/ui/badge';
    import { DateTimePicker } from '$lib/components/ui/date-time-picker';
    import { StatCard } from '$lib/components/admin';
    import { Key, Users, Building2, Activity, AlertTriangle, Clock, TrendingUp, Shield } from '@lucide/svelte';
    import { LoadingCard } from '$lib/components/ui/loading-card';
    import { AccessDeniedCard } from '$lib/components/ui/access-denied-card';
    import { StatusIndicator } from '$lib/components/ui/status-indicator';
    import { InfoField } from '$lib/components/ui/info-field';
    import { SectionHeader } from '$lib/components/ui/section-header';
    
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
    let mobileNavOpen = $state(false);
    
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

    // Navigation sections
    const navSections = [
        {
            category: 'Branding',
            items: [
                { id: 'logo', label: 'Logo' }
            ]
        },
        {
            category: 'Foundation',
            items: [
                { id: 'typography', label: 'Typography' },
                { id: 'colors', label: 'Colors & Semantic' }
            ]
        },
        {
            category: 'Components',
            items: [
                { id: 'buttons', label: 'Buttons' },
                { id: 'cards', label: 'Cards' },
                { id: 'stat-card', label: 'Stat Card' },
                { id: 'badges', label: 'Badges' },
                { id: 'date-time-picker', label: 'DateTimePicker' },
                { id: 'live-indicator', label: 'Live Indicator' },
                { id: 'loading-card', label: 'Loading Card' },
                { id: 'access-denied-card', label: 'Access Denied Card' },
                { id: 'status-indicator', label: 'Status Indicator' },
                { id: 'info-field', label: 'Info Field' },
                { id: 'section-header', label: 'Section Header' }
            ]
        },
        {
            category: 'Patterns',
            items: [
                { id: 'empty-state', label: 'Empty State' },
                { id: 'list-items', label: 'List Items' },
                { id: 'data-table', label: 'DataTable' },
                { id: 'forms', label: 'Form Patterns' }
            ]
        },
        {
            category: 'Examples',
            items: [
                { id: 'auth-card', label: 'Auth Card' }
            ]
        }
    ];

    function scrollToSection(id: string) {
        const element = document.getElementById(id);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            mobileNavOpen = false;
        }
    }
</script>

<svelte:head>
    <title>Component Playbook - Eventify Dev</title>
</svelte:head>

{#if isDev}
<div class="flex min-h-screen">
    <!-- Sidebar Navigation -->
    <aside class="hidden lg:flex w-64 flex-col fixed left-0 top-0 bottom-0 border-r border-border/50 bg-card/50 backdrop-blur-xl z-40">
        <div class="p-6 border-b border-border/50">
            <h1 class="text-lg font-semibold">Component Playbook</h1>
            <p class="text-xs text-muted-foreground mt-1">Eventify Design System</p>
        </div>
        
        <nav class="flex-1 overflow-y-auto p-4 space-y-6">
            {#each navSections as section}
                <div>
                    <p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2 px-2">
                        {section.category}
                    </p>
                    <ul class="space-y-1">
                        {#each section.items as item}
                            <li>
                                <button
                                    onclick={() => scrollToSection(item.id)}
                                    class="w-full text-left px-3 py-1.5 text-sm rounded-md hover:bg-muted/50 text-foreground/80 hover:text-foreground transition-colors"
                                >
                                    {item.label}
                                </button>
                            </li>
                        {/each}
                    </ul>
                </div>
            {/each}
        </nav>
        
        <div class="p-4 border-t border-border/50 space-y-2">
            <Button variant="outline" class="w-full justify-start" onclick={toggleTheme}>
                {#if isDarkMode}
                    <Sun class="h-4 w-4 mr-2" />
                    Light Mode
                {:else}
                    <Moon class="h-4 w-4 mr-2" />
                    Dark Mode
                {/if}
            </Button>
            <Button variant="ghost" class="w-full justify-start" href="/">
                <ArrowLeft class="h-4 w-4 mr-2" />
                Back to App
            </Button>
        </div>
    </aside>

    <!-- Mobile Header -->
    <div class="lg:hidden fixed top-0 left-0 right-0 h-14 border-b border-border/50 bg-card/95 backdrop-blur-xl z-50 flex items-center justify-between px-4">
        <h1 class="text-sm font-semibold">Component Playbook</h1>
        <div class="flex items-center gap-2">
            <Button variant="ghost" size="icon" onclick={toggleTheme}>
                {#if isDarkMode}
                    <Sun class="h-4 w-4" />
                {:else}
                    <Moon class="h-4 w-4" />
                {/if}
            </Button>
            <Button variant="ghost" size="icon" onclick={() => mobileNavOpen = !mobileNavOpen}>
                <Menu class="h-4 w-4" />
            </Button>
        </div>
    </div>

    <!-- Mobile Navigation Dropdown -->
    {#if mobileNavOpen}
        <div class="lg:hidden fixed top-14 left-0 right-0 bottom-0 bg-background/95 backdrop-blur-xl z-40 overflow-y-auto">
            <nav class="p-4 space-y-4">
                {#each navSections as section}
                    <div>
                        <p class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                            {section.category}
                        </p>
                        <ul class="space-y-1">
                            {#each section.items as item}
                                <li>
                                    <button
                                        onclick={() => scrollToSection(item.id)}
                                        class="w-full text-left px-3 py-2 text-sm rounded-md hover:bg-muted/50 transition-colors"
                                    >
                                        {item.label}
                                    </button>
                                </li>
                            {/each}
                        </ul>
                    </div>
                {/each}
                <div class="pt-4 border-t border-border/50">
                    <Button variant="ghost" class="w-full justify-start" href="/">
                        <ArrowLeft class="h-4 w-4 mr-2" />
                        Back to App
                    </Button>
                </div>
            </nav>
        </div>
    {/if}

    <!-- Main Content -->
    <main class="flex-1 lg:ml-64 pt-14 lg:pt-0">
        <div class="container mx-auto py-12 px-4 max-w-5xl">
            
            <!-- ==================== BRANDING ==================== -->
            
            <!-- Logo -->
            <section id="logo" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Logo</h2>
                <p class="text-muted-foreground mb-6">Minimalistic Radar icon with light typography</p>
                
                <div class="grid gap-6">
                    <!-- Size Variants -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Size Variants</CardTitle>
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
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Display Variants</CardTitle>
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
                            <CardTitle class="text-base">With Subtitle</CardTitle>
                            <CardDescription>Optional subtitle for context</CardDescription>
                        </CardHeader>
                        <CardContent class="py-8">
                            <div class="flex justify-center">
                                <AppLogo size="medium" subtitle="Real-time monitoring and event tracking" />
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- ==================== FOUNDATION ==================== -->

            <!-- Typography -->
            <section id="typography" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Typography</h2>
                <p class="text-muted-foreground mb-6">Consistent text styles across the application</p>
                
                <div class="grid gap-6">
                    <!-- Headings -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Headings</CardTitle>
                            <CardDescription>Page titles and section headers</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <h1 class="text-4xl font-bold">Heading 1</h1>
                                <code class="text-xs text-muted-foreground shrink-0">text-4xl font-bold</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <h2 class="text-3xl font-bold">Heading 2</h2>
                                <code class="text-xs text-muted-foreground shrink-0">text-3xl font-bold</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <h3 class="text-2xl font-semibold">Heading 3</h3>
                                <code class="text-xs text-muted-foreground shrink-0">text-2xl font-semibold</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <h4 class="text-xl font-semibold">Heading 4</h4>
                                <code class="text-xs text-muted-foreground shrink-0">text-xl font-semibold</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <h5 class="text-lg font-semibold">Heading 5</h5>
                                <code class="text-xs text-muted-foreground shrink-0">text-lg font-semibold</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2">
                                <h6 class="text-base font-medium">Heading 6</h6>
                                <code class="text-xs text-muted-foreground shrink-0">text-base font-medium</code>
                            </div>
                        </CardContent>
                    </Card>

                    <!-- Body Text -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Body Text</CardTitle>
                            <CardDescription>Paragraph and content styles</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <p class="text-lg">Large body text</p>
                                <code class="text-xs text-muted-foreground shrink-0">text-lg</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <p class="text-base">Default body text</p>
                                <code class="text-xs text-muted-foreground shrink-0">text-base</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2 border-b border-border/30">
                                <p class="text-sm">Small text</p>
                                <code class="text-xs text-muted-foreground shrink-0">text-sm</code>
                            </div>
                            <div class="flex items-baseline justify-between gap-4 py-2">
                                <p class="text-xs">Extra small text</p>
                                <code class="text-xs text-muted-foreground shrink-0">text-xs</code>
                            </div>
                        </CardContent>
                    </Card>

                    <!-- Font Weights -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Font Weights</CardTitle>
                            <CardDescription>Available weight variants</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-3">
                            <div class="flex items-center justify-between gap-4">
                                <p class="text-base font-light">Light weight</p>
                                <code class="text-xs text-muted-foreground">font-light</code>
                            </div>
                            <div class="flex items-center justify-between gap-4">
                                <p class="text-base font-normal">Normal weight</p>
                                <code class="text-xs text-muted-foreground">font-normal</code>
                            </div>
                            <div class="flex items-center justify-between gap-4">
                                <p class="text-base font-medium">Medium weight</p>
                                <code class="text-xs text-muted-foreground">font-medium</code>
                            </div>
                            <div class="flex items-center justify-between gap-4">
                                <p class="text-base font-semibold">Semibold weight</p>
                                <code class="text-xs text-muted-foreground">font-semibold</code>
                            </div>
                            <div class="flex items-center justify-between gap-4">
                                <p class="text-base font-bold">Bold weight</p>
                                <code class="text-xs text-muted-foreground">font-bold</code>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- Colors -->
            <section id="colors" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Colors & Semantic</h2>
                <p class="text-muted-foreground mb-6">Semantic color usage for text and backgrounds</p>
                
                <div class="grid gap-6">
                    <!-- Text Colors -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Text Colors</CardTitle>
                            <CardDescription>Semantic color tokens</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-3">
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-foreground">Primary foreground</p>
                                <code class="text-xs text-muted-foreground">text-foreground</code>
                            </div>
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-muted-foreground">Muted foreground</p>
                                <code class="text-xs text-muted-foreground">text-muted-foreground</code>
                            </div>
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-primary">Primary accent</p>
                                <code class="text-xs text-muted-foreground">text-primary</code>
                            </div>
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-destructive">Destructive / Error</p>
                                <code class="text-xs text-muted-foreground">text-destructive</code>
                            </div>
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-green-500">Success</p>
                                <code class="text-xs text-muted-foreground">text-green-500</code>
                            </div>
                            <div class="flex items-center justify-between gap-4 py-2">
                                <p class="text-amber-500">Warning</p>
                                <code class="text-xs text-muted-foreground">text-amber-500</code>
                            </div>
                        </CardContent>
                    </Card>

                    <!-- Status Colors -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Status Indicators</CardTitle>
                            <CardDescription>Consistent status color patterns</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div class="p-3 rounded-md bg-green-500/10 border border-green-500/30">
                                <p class="text-sm font-medium text-green-500">Success</p>
                                <p class="text-sm text-muted-foreground">bg-green-500/10 border-green-500/30</p>
                            </div>
                            <div class="p-3 rounded-md bg-amber-500/10 border border-amber-500/30">
                                <p class="text-sm font-medium text-amber-500">Warning</p>
                                <p class="text-sm text-muted-foreground">bg-amber-500/10 border-amber-500/30</p>
                            </div>
                            <div class="p-3 rounded-md bg-destructive/10 border border-destructive/30">
                                <p class="text-sm font-medium text-destructive">Error</p>
                                <p class="text-sm text-muted-foreground">bg-destructive/10 border-destructive/30</p>
                            </div>
                            <div class="p-3 rounded-md bg-primary/10 border border-primary/30">
                                <p class="text-sm font-medium text-primary">Info / Primary</p>
                                <p class="text-sm text-muted-foreground">bg-primary/10 border-primary/30</p>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- ==================== COMPONENTS ==================== -->

            <!-- Buttons -->
            <section id="buttons" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Buttons</h2>
                <p class="text-muted-foreground mb-6">Glass variant for CTAs, plus existing variants</p>
                
                <div class="grid gap-6 md:grid-cols-2">
                    <!-- Default Variant -->
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Default Variant
                                <Badge variant="outline" class="text-xs">GLASS</Badge>
                            </CardTitle>
                            <CardDescription>Primary CTA with glassmorphism</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-3">
                            <Button variant="default" class="w-full">Sign In</Button>
                            <Button variant="default" class="w-full">
                                <Loader2 class="h-4 w-4 animate-spin" />
                                Loading...
                            </Button>
                            <Button variant="default" class="w-full" disabled>Disabled</Button>
                        </CardContent>
                    </Card>

                    <!-- All Variants -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">All Variants</CardTitle>
                            <CardDescription>Complete button library</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-2">
                            <div class="flex items-center gap-3">
                                <Button variant="default" size="sm" class="w-24">Default</Button>
                                <code class="text-xs text-muted-foreground">default</code>
                            </div>
                            <div class="flex items-center gap-3">
                                <Button variant="secondary" size="sm" class="w-24">Secondary</Button>
                                <code class="text-xs text-muted-foreground">secondary</code>
                            </div>
                            <div class="flex items-center gap-3">
                                <Button variant="outline" size="sm" class="w-24">Outline</Button>
                                <code class="text-xs text-muted-foreground">outline</code>
                            </div>
                            <div class="flex items-center gap-3">
                                <Button variant="ghost" size="sm" class="w-24">Ghost</Button>
                                <code class="text-xs text-muted-foreground">ghost</code>
                            </div>
                            <div class="flex items-center gap-3">
                                <Button variant="link" size="sm" class="w-24">Link</Button>
                                <code class="text-xs text-muted-foreground">link</code>
                            </div>
                            <div class="flex items-center gap-3">
                                <Button variant="destructive" size="sm" class="w-24">Destructive</Button>
                                <code class="text-xs text-muted-foreground">destructive</code>
                            </div>
                        </CardContent>
                    </Card>
                </div>

                <!-- Button Sizes -->
                <Card class="border-border/50 mt-6">
                    <CardHeader>
                        <CardTitle class="text-base">Button Sizes</CardTitle>
                        <CardDescription>sm, default, lg, icon</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div class="flex flex-wrap items-center gap-4">
                            <Button size="sm">Small</Button>
                            <Button size="default">Default</Button>
                            <Button size="lg">Large</Button>
                            <Button size="icon"><Check class="h-4 w-4" /></Button>
                            <Button size="icon-sm"><Check class="h-4 w-4" /></Button>
                        </div>
                    </CardContent>
                </Card>
            </section>

            <!-- Cards -->
            <section id="cards" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Cards</h2>
                <p class="text-muted-foreground mb-6">Glassmorphism card styles</p>
                
                <div class="grid gap-6 md:grid-cols-3">
                    <Card class="border-border/50 bg-card">
                        <CardHeader>
                            <CardTitle class="text-base">Standard</CardTitle>
                            <CardDescription>Default card</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <code class="text-xs text-muted-foreground">bg-card</code>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50 bg-card/50 backdrop-blur-xl">
                        <CardHeader>
                            <CardTitle class="text-base">Glass</CardTitle>
                            <CardDescription>Semi-transparent</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <code class="text-xs text-muted-foreground">bg-card/50 backdrop-blur-xl</code>
                        </CardContent>
                    </Card>

                    <Card class="border-border/30 bg-card/30 backdrop-blur-md">
                        <CardHeader>
                            <CardTitle class="text-base">Light Glass</CardTitle>
                            <CardDescription>More transparency</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <code class="text-xs text-muted-foreground">bg-card/30 backdrop-blur-md</code>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- Stat Card -->
            <section id="stat-card" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Stat Card</h2>
                <p class="text-muted-foreground mb-6">Dashboard statistics with color variants and gradients</p>
                
                <div class="grid gap-6">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Color Variants
                                <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                            </CardTitle>
                            <CardDescription>Import from $lib/components/admin</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                                <StatCard title="Primary" value="1,234" icon={Key} variant="primary" />
                                <StatCard title="Blue" value="567" icon={Users} variant="blue" />
                                <StatCard title="Purple" value="89" icon={Building2} variant="purple" />
                                <StatCard title="Green" value="42" icon={Activity} variant="green" />
                                <StatCard title="Yellow" value="15" icon={AlertTriangle} variant="yellow" />
                                <StatCard title="Orange" value="7" icon={Clock} variant="orange" />
                                <StatCard title="Red" value="3" icon={Shield} variant="red" />
                                <StatCard title="Accent" value="99" icon={TrendingUp} variant="accent" />
                            </div>
                        </CardContent>
                    </Card>

                    <div class="grid gap-6 md:grid-cols-2">
                        <Card class="border-border/50">
                            <CardHeader>
                                <CardTitle class="text-base">With Subtitle</CardTitle>
                                <CardDescription>Additional context text</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div class="grid grid-cols-2 gap-4">
                                    <StatCard 
                                        title="Total Users" 
                                        value="1,234" 
                                        icon={Users} 
                                        variant="blue" 
                                        subtitle="Active this month"
                                    />
                                    <StatCard 
                                        title="API Keys" 
                                        value="89" 
                                        icon={Key} 
                                        variant="green" 
                                        subtitle="12 expiring soon"
                                    />
                                </div>
                            </CardContent>
                        </Card>

                        <Card class="border-border/50">
                            <CardHeader>
                                <CardTitle class="text-base">Loading State</CardTitle>
                                <CardDescription>Skeleton while fetching data</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div class="grid grid-cols-2 gap-4">
                                    <StatCard title="Loading..." value="" icon={Activity} variant="primary" loading />
                                    <StatCard title="Ready" value="Done" icon={Activity} variant="green" />
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </div>
            </section>

            <!-- Badges -->
            <section id="badges" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Badges</h2>
                <p class="text-muted-foreground mb-6">Status indicators and labels</p>
                
                <Card class="border-border/50">
                    <CardHeader>
                        <CardTitle class="text-base">Badge Variants</CardTitle>
                        <CardDescription>All available badge styles</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div class="flex flex-wrap gap-3">
                            <Badge variant="default">Default</Badge>
                            <Badge variant="secondary">Secondary</Badge>
                            <Badge variant="outline">Outline</Badge>
                            <Badge variant="destructive">Destructive</Badge>
                        </div>
                        <div class="flex flex-wrap gap-3 mt-4">
                            <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-green-500/20 text-green-500">Success</span>
                            <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-500">Warning</span>
                            <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-primary/20 text-primary">Info</span>
                        </div>
                    </CardContent>
                </Card>
            </section>

            <!-- DateTimePicker -->
            <section id="date-time-picker" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">DateTimePicker</h2>
                <p class="text-muted-foreground mb-6">Calendar with 24h time selection</p>
                
                <div class="grid gap-6 md:grid-cols-3">
                    <Card class="border-border/50">
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
                            <p class="text-xs text-muted-foreground mt-3">
                                Value: {dateTimeValue1 || '(empty)'}
                            </p>
                        </CardContent>
                    </Card>
                    
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">With Value</CardTitle>
                            <CardDescription>Pre-populated</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <DateTimePicker 
                                value={dateTimeValue2}
                                onValueChange={(v) => dateTimeValue2 = v}
                                label="Event Time"
                            />
                            <p class="text-xs text-muted-foreground mt-3">
                                {dateTimeValue2 ? new Date(dateTimeValue2).toLocaleString('en-GB', { hour12: false }) : '(empty)'}
                            </p>
                        </CardContent>
                    </Card>
                    
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Disabled</CardTitle>
                            <CardDescription>Non-interactive</CardDescription>
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

            <!-- Live Indicator -->
            <section id="live-indicator" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Live Indicator</h2>
                <p class="text-muted-foreground mb-6">Pulsing animation for real-time features</p>
                
                <div class="grid gap-6 md:grid-cols-2">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Live Badge
                                <Badge variant="outline" class="text-xs">PREFERRED</Badge>
                            </CardTitle>
                            <CardDescription>Pulsing dot with label</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-6">
                            <div class="flex items-center gap-4">
                                <span class="text-sm font-medium text-green-500 flex items-center gap-1.5">
                                    <span class="relative flex h-2 w-2">
                                        <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                                        <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                                    </span>
                                    Live
                                </span>
                                <code class="text-xs text-muted-foreground">With label</code>
                            </div>
                            <div class="flex items-center gap-4">
                                <span class="relative flex h-2.5 w-2.5">
                                    <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                                    <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-green-500"></span>
                                </span>
                                <code class="text-xs text-muted-foreground">Dot only</code>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Usage Example</CardTitle>
                            <CardDescription>In context</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="p-3 rounded-md bg-muted/30 border border-border/50 space-y-2">
                                <div class="flex items-center justify-between">
                                    <span class="text-sm font-medium">Monitor Status</span>
                                    <span class="text-xs font-medium text-green-500 flex items-center gap-1.5">
                                        <span class="relative flex h-1.5 w-1.5">
                                            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                                            <span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-500"></span>
                                        </span>
                                        Live
                                    </span>
                                </div>
                                <p class="text-xs text-muted-foreground">Last updated 14:32</p>
                            </div>
                            <p class="text-xs text-muted-foreground mt-3">
                                Use in headers, status bars, real-time dashboards
                            </p>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- Loading Card -->
            <section id="loading-card" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Loading Card</h2>
                <p class="text-muted-foreground mb-6">Generic loading state with spinner</p>
                
                <Card class="border-border/50 border-primary/30">
                    <CardHeader>
                        <CardTitle class="text-base flex items-center gap-2">
                            Usage
                            <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                        </CardTitle>
                        <CardDescription>Import from $lib/components/ui/loading-card</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div class="max-w-md mx-auto">
                            <LoadingCard />
                        </div>
                        <div class="mt-4 p-4 bg-muted/20 rounded-md">
                            <code class="text-xs">
                                &lt;LoadingCard /&gt;
                            </code>
                        </div>
                        <p class="text-xs text-muted-foreground mt-3">
                            Use for full-page loading states or while fetching data.
                        </p>
                    </CardContent>
                </Card>
            </section>

            <!-- Access Denied Card -->
            <section id="access-denied-card" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Access Denied Card</h2>
                <p class="text-muted-foreground mb-6">Error state for unauthorized access</p>
                
                <Card class="border-border/50 border-primary/30">
                    <CardHeader>
                        <CardTitle class="text-base flex items-center gap-2">
                            Usage
                            <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                        </CardTitle>
                        <CardDescription>Import from $lib/components/ui/access-denied-card</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div class="max-w-md mx-auto">
                            <AccessDeniedCard />
                        </div>
                        <div class="mt-4 p-4 bg-muted/20 rounded-md">
                            <code class="text-xs">
                                &lt;AccessDeniedCard /&gt;
                            </code>
                        </div>
                        <p class="text-xs text-muted-foreground mt-3">
                            Use when user doesn't have permission to view a resource.
                        </p>
                    </CardContent>
                </Card>
            </section>

            <!-- Status Indicator -->
            <section id="status-indicator" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Status Indicator</h2>
                <p class="text-muted-foreground mb-6">Display boolean status with colored icon and text</p>
                
                <div class="grid gap-6">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Usage
                                <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                            </CardTitle>
                            <CardDescription>Import from $lib/components/ui/status-indicator</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-4">
                                <div class="p-4 rounded-lg border border-border/50 bg-card/30">
                                    <div class="space-y-3">
                                        <StatusIndicator 
                                            status={true}
                                            activeLabel="Email Verified"
                                            inactiveLabel="Email Not Verified"
                                        />
                                        <StatusIndicator 
                                            status={false}
                                            activeLabel="Email Verified"
                                            inactiveLabel="Email Not Verified"
                                        />
                                    </div>
                                </div>
                                <div class="p-4 bg-muted/20 rounded-md">
                                    <code class="text-xs block">
                                        &lt;StatusIndicator<br/>
                                        &nbsp;&nbsp;status=&#123;true&#125;<br/>
                                        &nbsp;&nbsp;activeLabel="Email Verified"<br/>
                                        &nbsp;&nbsp;inactiveLabel="Email Not Verified"<br/>
                                        /&gt;
                                    </code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Props</CardTitle>
                            <CardDescription>Available properties</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-2 text-sm">
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30 font-medium text-muted-foreground">
                                    <div>Prop</div>
                                    <div>Type</div>
                                    <div>Default</div>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">status</code>
                                    <code class="text-xs">boolean | undefined</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">activeLabel</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">inactiveLabel</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">activeColor</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">'text-green-500'</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2">
                                    <code class="text-xs">inactiveColor</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">'text-red-500'</code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Custom Colors</CardTitle>
                            <CardDescription>Override default colors</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-3 p-4 rounded-lg border border-border/50 bg-card/30">
                                <StatusIndicator 
                                    status={true}
                                    activeLabel="Account Active"
                                    inactiveLabel="Account Disabled"
                                />
                                <StatusIndicator 
                                    status={false}
                                    activeLabel="Feature Enabled"
                                    inactiveLabel="Feature Disabled"
                                    activeColor="text-primary"
                                    inactiveColor="text-muted-foreground"
                                />
                            </div>
                            <p class="text-xs text-muted-foreground mt-3">
                                Use cases: Email verified/not verified, Account active/disabled, Feature enabled/disabled
                            </p>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- Info Field -->
            <section id="info-field" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Info Field</h2>
                <p class="text-muted-foreground mb-6">Read-only info display box with icon and label</p>
                
                <div class="grid gap-6">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Usage
                                <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                            </CardTitle>
                            <CardDescription>Import from $lib/components/ui/info-field</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-4">
                                <div class="grid gap-3 md:grid-cols-2">
                                    <InfoField 
                                        label="Email Address"
                                        value="user@example.com"
                                        icon={Mail}
                                    />
                                    <InfoField 
                                        label="Username"
                                        value="johndoe"
                                        icon={User}
                                    />
                                    <InfoField 
                                        label="Role"
                                        value="Administrator"
                                        icon={ShieldCheck}
                                    />
                                    <InfoField 
                                        label="Account Status"
                                        value="Active"
                                        icon={Settings}
                                    />
                                </div>
                                <div class="p-4 bg-muted/20 rounded-md">
                                    <code class="text-xs block">
                                        &lt;InfoField<br/>
                                        &nbsp;&nbsp;label="Email Address"<br/>
                                        &nbsp;&nbsp;value="user@example.com"<br/>
                                        &nbsp;&nbsp;icon=&#123;Mail&#125;<br/>
                                        /&gt;
                                    </code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Props</CardTitle>
                            <CardDescription>Available properties</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-2 text-sm">
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30 font-medium text-muted-foreground">
                                    <div>Prop</div>
                                    <div>Type</div>
                                    <div>Default</div>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">label</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">value</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">icon</code>
                                    <code class="text-xs">Component</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2">
                                    <code class="text-xs">class</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">''</code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Use Cases</CardTitle>
                            <CardDescription>When to use InfoField</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-2 text-sm">
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Profile information display</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Settings pages with read-only fields</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Detail views showing entity information</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Any page requiring structured info display with icons</p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- Section Header -->
            <section id="section-header" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Section Header</h2>
                <p class="text-muted-foreground mb-6">Consistent section title with icon</p>
                
                <div class="grid gap-6">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Usage
                                <Badge variant="outline" class="text-xs">COMPONENT</Badge>
                            </CardTitle>
                            <CardDescription>Import from $lib/components/ui/section-header</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-6">
                                <div class="space-y-4">
                                    <SectionHeader title="Personal Information" icon={User} />
                                    <div class="grid gap-3 md:grid-cols-2">
                                        <InfoField label="Name" value="John Doe" icon={User} />
                                        <InfoField label="Email" value="john@example.com" icon={Mail} />
                                    </div>
                                </div>
                                
                                <div class="space-y-4">
                                    <SectionHeader title="Security Settings" icon={ShieldCheck} />
                                    <div class="space-y-3">
                                        <StatusIndicator 
                                            status={true}
                                            activeLabel="Two-Factor Authentication Enabled"
                                            inactiveLabel="Two-Factor Authentication Disabled"
                                        />
                                    </div>
                                </div>

                                <div class="p-4 bg-muted/20 rounded-md">
                                    <code class="text-xs block">
                                        &lt;SectionHeader<br/>
                                        &nbsp;&nbsp;title="Personal Information"<br/>
                                        &nbsp;&nbsp;icon=&#123;User&#125;<br/>
                                        /&gt;
                                    </code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Props</CardTitle>
                            <CardDescription>Available properties</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-2 text-sm">
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30 font-medium text-muted-foreground">
                                    <div>Prop</div>
                                    <div>Type</div>
                                    <div>Default</div>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">title</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2 border-b border-border/30">
                                    <code class="text-xs">icon</code>
                                    <code class="text-xs">Component</code>
                                    <code class="text-xs">-</code>
                                </div>
                                <div class="grid grid-cols-3 gap-4 py-2">
                                    <code class="text-xs">class</code>
                                    <code class="text-xs">string</code>
                                    <code class="text-xs">''</code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Use Cases</CardTitle>
                            <CardDescription>When to use SectionHeader</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="space-y-2 text-sm">
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Profile page sections (Personal Info, Security, Preferences)</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Settings pages with multiple grouped sections</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Any page with multiple content groups requiring visual separation</p>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Check class="h-4 w-4 text-green-500 shrink-0 mt-0.5" />
                                    <p>Forms with multiple logical sections</p>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- ==================== PATTERNS ==================== -->

            <!-- Empty State -->
            <section id="empty-state" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Empty State</h2>
                <p class="text-muted-foreground mb-6">Centered card for when no data is available</p>
                
                <div class="grid gap-6">
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Standard Empty State
                                <Badge variant="outline" class="text-xs">PREFERRED</Badge>
                            </CardTitle>
                            <CardDescription>Use MonitorEmptyState component or this pattern</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
                                <CardContent class="p-12">
                                    <div class="flex flex-col items-center justify-center text-center">
                                        <div class="h-20 w-20 rounded-full bg-primary/10 flex items-center justify-center mb-6">
                                            <LayoutList class="h-10 w-10 text-primary" />
                                        </div>
                                        <h3 class="text-xl font-semibold mb-2">No Items Found</h3>
                                        <p class="text-muted-foreground max-w-md mb-6">
                                            There are no items to display. Create one to get started.
                                        </p>
                                        <Button variant="outline">Create Item</Button>
                                    </div>
                                </CardContent>
                            </Card>
                        </CardContent>
                    </Card>

                    <div class="grid gap-6 md:grid-cols-2">
                        <Card class="border-border/50">
                            <CardHeader>
                                <CardTitle class="text-base">Error Variant</CardTitle>
                                <CardDescription>Destructive icon color</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div class="p-8 rounded-lg border border-border/50 bg-card/50">
                                    <div class="flex flex-col items-center justify-center text-center">
                                        <div class="h-16 w-16 rounded-full bg-destructive/10 flex items-center justify-center mb-4">
                                            <AlertCircle class="h-8 w-8 text-destructive" />
                                        </div>
                                        <h3 class="text-lg font-semibold mb-1">Not Found</h3>
                                        <p class="text-sm text-muted-foreground max-w-sm">
                                            The resource you're looking for doesn't exist.
                                        </p>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>

                        <Card class="border-border/50">
                            <CardHeader>
                                <CardTitle class="text-base">Compact Variant</CardTitle>
                                <CardDescription>For inline/smaller spaces</CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div class="p-6 rounded-lg border border-dashed border-border/50 text-center">
                                    <p class="text-lg font-medium">No events found</p>
                                    <p class="text-sm text-muted-foreground mt-1">
                                        Create your first channel to start receiving events.
                                    </p>
                                    <Button variant="outline" size="sm" class="mt-3">Create Channel</Button>
                                </div>
                            </CardContent>
                        </Card>
                    </div>

                    <!-- Severity Colors Reference -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Severity Color Reference</CardTitle>
                            <CardDescription>Standard colors for status/severity indicators</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                                <div class="space-y-2">
                                    <div class="h-3 rounded-sm bg-green-500"></div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-2.5 h-2.5 rounded-full bg-green-500"></div>
                                        <span class="text-sm font-medium">OK</span>
                                    </div>
                                    <code class="text-xs text-muted-foreground">bg-green-500</code>
                                </div>
                                <div class="space-y-2">
                                    <div class="h-3 rounded-sm bg-amber-500"></div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-2.5 h-2.5 rounded-full bg-amber-500"></div>
                                        <span class="text-sm font-medium">WARNING</span>
                                    </div>
                                    <code class="text-xs text-muted-foreground">bg-amber-500</code>
                                </div>
                                <div class="space-y-2">
                                    <div class="h-3 rounded-sm bg-red-500"></div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-2.5 h-2.5 rounded-full bg-red-500"></div>
                                        <span class="text-sm font-medium">CRITICAL</span>
                                    </div>
                                    <code class="text-xs text-muted-foreground">bg-red-500</code>
                                </div>
                                <div class="space-y-2">
                                    <div class="h-3 rounded-sm bg-gray-400"></div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-2.5 h-2.5 rounded-full bg-gray-400"></div>
                                        <span class="text-sm font-medium">NO DATA</span>
                                    </div>
                                    <code class="text-xs text-muted-foreground">bg-gray-400</code>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- List Items -->
            <section id="list-items" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">List Items</h2>
                <p class="text-muted-foreground mb-6">Preferred patterns for rows and list items</p>
                
                <div class="grid gap-6">
                    <!-- Minimal List Item -->
                    <Card class="border-border/50 border-primary/30">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Minimal Row
                                <Badge variant="outline" class="text-xs">PREFERRED</Badge>
                            </CardTitle>
                            <CardDescription>For DataTable rows, config panels, dense lists</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-1">
                            <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all">
                                <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium truncate">Production API</p>
                                    <p class="text-xs text-muted-foreground truncate">Main production channel</p>
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
                                    <p class="text-sm font-medium truncate">Staging Environment</p>
                                    <p class="text-xs text-muted-foreground truncate">Pre-production testing</p>
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
                        </CardContent>
                    </Card>

                    <!-- Standard Card Row -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base flex items-center gap-2">
                                Standard Row
                                <Badge variant="outline" class="text-xs">OVERVIEWS</Badge>
                            </CardTitle>
                            <CardDescription>For main list pages, dashboards</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-2">
                            <div class="group flex items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/30 hover:bg-card/50 hover:border-border transition-all">
                                <Radio class="h-5 w-5 text-primary shrink-0" />
                                <div class="flex-1 min-w-0">
                                    <p class="font-medium truncate">Production API</p>
                                    <p class="text-sm text-muted-foreground truncate">Main production channel</p>
                                </div>
                                <Badge variant="default">Active</Badge>
                                <span class="text-sm text-muted-foreground">Jan 15</span>
                                <div class="flex items-center gap-1">
                                    <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                        <Edit class="h-4 w-4" />
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <!-- Draggable -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Draggable Row</CardTitle>
                            <CardDescription>With hover-reveal drag handle</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-1">
                            <div class="group flex items-center gap-3 px-3 py-2.5 rounded-md hover:bg-muted/50 transition-all cursor-move">
                                <div class="shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <GripVertical class="h-4 w-4 text-muted-foreground/50" />
                                </div>
                                <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium truncate">Production API</p>
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
                                </div>
                                <Button variant="ghost" size="icon" class="h-7 w-7 opacity-0 group-hover:opacity-100 text-muted-foreground hover:text-destructive transition-all">
                                    <X class="h-4 w-4" />
                                </Button>
                            </div>
                        </CardContent>
                    </Card>

                    <!-- Collapsible Group -->
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Collapsible Group</CardTitle>
                            <CardDescription>Nested/grouped items</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="rounded-md border border-border/30">
                                <div class="group flex items-center gap-3 px-3 py-2.5 bg-muted/20 hover:bg-muted/40 transition-all cursor-pointer">
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
                                </div>
                                <div class="pl-10 pr-3 pb-3 pt-2 space-y-1">
                                    <div class="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-muted/50 transition-all">
                                        <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                                        <p class="text-sm font-medium truncate">API Gateway</p>
                                    </div>
                                    <div class="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-muted/50 transition-all">
                                        <Radio class="h-4 w-4 text-primary/70 shrink-0" />
                                        <p class="text-sm font-medium truncate">Auth Service</p>
                                    </div>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- DataTable -->
            <section id="data-table" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">DataTable</h2>
                <p class="text-muted-foreground mb-6">Server-side paginated tables with sorting and filtering</p>
                
                <Card class="border-border/50">
                    <CardHeader>
                        <CardTitle class="text-base flex items-center gap-2">
                            DataTable Row Pattern
                            <Badge variant="outline" class="text-xs">STANDARD</Badge>
                        </CardTitle>
                        <CardDescription>Use this styling for all DataTable row snippets</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div class="rounded-lg border border-border/50 overflow-hidden">
                            <!-- Header -->
                            <div class="grid grid-cols-12 gap-4 px-4 py-3 bg-muted/30 text-sm font-medium text-muted-foreground border-b border-border/50">
                                <div class="col-span-3">Channel</div>
                                <div class="col-span-4">Description</div>
                                <div class="col-span-2">Status</div>
                                <div class="col-span-2">Created</div>
                                <div class="col-span-1"></div>
                            </div>
                            
                            <!-- Rows -->
                            <div class="divide-y divide-border/30">
                                <div class="grid grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
                                    <div class="col-span-3 flex items-center gap-3">
                                        <Radio class="h-5 w-5 text-primary shrink-0" />
                                        <span class="font-medium truncate">Production API</span>
                                    </div>
                                    <div class="col-span-4 text-sm text-muted-foreground truncate">Main production channel</div>
                                    <div class="col-span-2"><Badge variant="default">Active</Badge></div>
                                    <div class="col-span-2 text-sm text-muted-foreground">Jan 15, 2026</div>
                                    <div class="col-span-1 flex justify-end">
                                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                            <Edit class="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                                <div class="grid grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
                                    <div class="col-span-3 flex items-center gap-3">
                                        <Radio class="h-5 w-5 text-primary shrink-0" />
                                        <span class="font-medium truncate">Staging</span>
                                    </div>
                                    <div class="col-span-4 text-sm text-muted-foreground truncate">Pre-production testing</div>
                                    <div class="col-span-2"><Badge variant="secondary">Paused</Badge></div>
                                    <div class="col-span-2 text-sm text-muted-foreground">Jan 10, 2026</div>
                                    <div class="col-span-1 flex justify-end">
                                        <Button variant="ghost" size="icon" class="h-8 w-8 text-muted-foreground hover:text-primary">
                                            <Edit class="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </section>

            <!-- Forms -->
            <section id="forms" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Form Patterns</h2>
                <p class="text-muted-foreground mb-6">Labels, inputs, hints, and validation</p>
                
                <div class="grid gap-6 md:grid-cols-2">
                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Form Labels</CardTitle>
                            <CardDescription>Standard label patterns</CardDescription>
                        </CardHeader>
                        <CardContent class="space-y-4">
                            <div class="space-y-1.5">
                                <label class="text-sm font-medium">Email Address</label>
                                <input 
                                    type="email" 
                                    placeholder="you@example.com"
                                    class="w-full h-9 px-3 rounded-md border border-border/50 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                                />
                                <p class="text-xs text-muted-foreground">We'll never share your email.</p>
                            </div>
                            <div class="space-y-1.5">
                                <label class="text-sm font-medium">Password <span class="text-destructive">*</span></label>
                                <input 
                                    type="password" 
                                    placeholder="Enter password"
                                    class="w-full h-9 px-3 rounded-md border border-destructive/50 bg-background text-sm focus:outline-none focus:ring-2 focus:ring-destructive"
                                />
                                <p class="text-xs text-destructive">Password is required.</p>
                            </div>
                        </CardContent>
                    </Card>

                    <Card class="border-border/50">
                        <CardHeader>
                            <CardTitle class="text-base">Empty States</CardTitle>
                            <CardDescription>When no data is available</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <div class="text-center py-8 px-4 rounded-lg border border-dashed border-border/50">
                                <p class="text-lg font-medium">No events found</p>
                                <p class="text-sm text-muted-foreground mt-1">Create your first channel to start receiving events.</p>
                                <Button variant="outline" class="mt-4">Create Channel</Button>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </section>

            <!-- ==================== EXAMPLES ==================== -->

            <!-- Auth Card -->
            <section id="auth-card" class="mb-20 scroll-mt-20">
                <h2 class="text-2xl font-semibold mb-2">Auth Card</h2>
                <p class="text-muted-foreground mb-6">Login/registration card example</p>
                
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
                        <Button class="w-full">Sign In</Button>
                        <p class="text-center text-sm text-muted-foreground">
                            Don't have an account? <a href="/register" class="text-primary hover:underline">Sign up</a>
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
    </main>
</div>
{:else}
<div class="flex items-center justify-center min-h-screen">
    <p class="text-muted-foreground">This page is only available in development mode.</p>
</div>
{/if}
