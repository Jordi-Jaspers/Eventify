<script lang="ts">
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Clock, Shield, User} from '@lucide/svelte';
    import {authStore} from '$lib/stores/auth';
</script>

<svelte:head>
    <title>Dashboard - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
        <!-- Welcome Card -->
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <!-- Gradient overlay -->
            <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>

            <CardHeader class="relative z-10">
                <CardTitle class="text-2xl flex items-center gap-2">
                    <User class="w-6 h-6 text-primary"/>
                    Welcome back!
                </CardTitle>
                <CardDescription>
                    You are successfully logged in and verified
                </CardDescription>
            </CardHeader>
            <CardContent class="space-y-6 relative z-10">
                <!-- User Info Grid -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <!-- Email -->
                    <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                        <p class="text-xs text-muted-foreground mb-1">Email Address</p>
                        <p class="font-medium text-foreground">{$authStore.user?.email || 'N/A'}</p>
                    </div>

                    <!-- Role -->
                    <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                        <div class="flex items-center gap-2 mb-1">
                            <Shield class="w-3 h-3 text-primary"/>
                            <p class="text-xs text-muted-foreground">Role</p>
                        </div>
                        <p class="font-medium text-foreground capitalize">{$authStore.user?.role?.toLowerCase() || 'N/A'}</p>
                    </div>
                </div>

                <!-- Status -->
                <div class="flex items-center justify-between p-4 rounded-lg bg-gradient-to-r from-green-500/10 to-emerald-500/10 border border-green-500/20">
                    <div class="flex items-center gap-3">
                        <div class="relative">
                            <div class="h-3 w-3 rounded-full bg-green-500 animate-pulse"></div>
                            <div class="absolute inset-0 h-3 w-3 rounded-full bg-green-500 animate-ping"></div>
                        </div>
                        <div>
                            <p class="text-sm font-medium text-green-400">Account Active</p>
                            <p class="text-xs text-muted-foreground">All systems operational</p>
                        </div>
                    </div>
                    <Clock class="w-4 h-4 text-green-400"/>
                </div>
            </CardContent>
        </Card>

        <!-- Next Steps Card -->
        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
            <!-- Gradient overlay -->
            <div class="absolute inset-0 bg-gradient-to-br from-accent/10 via-transparent to-primary/10 opacity-50"></div>

            <CardHeader class="relative z-10">
                <CardTitle class="text-xl">Getting Started</CardTitle>
                <CardDescription>
                    Your platform setup status
                </CardDescription>
            </CardHeader>
            <CardContent class="relative z-10">
                <ul class="space-y-3">
                    <li class="flex items-center gap-3 p-3 rounded-lg bg-primary/10 border border-primary/20">
                        <div class="flex-shrink-0 w-5 h-5 rounded-full bg-primary flex items-center justify-center">
                            <svg class="w-3 h-3 text-primary-foreground" fill="none" viewBox="0 0 24 24"
                                 stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                                      d="M5 13l4 4L19 7"/>
                            </svg>
                        </div>
                        <span class="text-sm text-foreground font-medium">Authentication flow is working</span>
                    </li>
                    <li class="flex items-center gap-3 p-3 rounded-lg bg-primary/10 border border-primary/20">
                        <div class="flex-shrink-0 w-5 h-5 rounded-full bg-primary flex items-center justify-center">
                            <svg class="w-3 h-3 text-primary-foreground" fill="none" viewBox="0 0 24 24"
                                 stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                                      d="M5 13l4 4L19 7"/>
                            </svg>
                        </div>
                        <span class="text-sm text-foreground font-medium">Route protection is active</span>
                    </li>
                    <li class="flex items-center gap-3 p-3 rounded-lg bg-primary/10 border border-primary/20">
                        <div class="flex-shrink-0 w-5 h-5 rounded-full bg-primary flex items-center justify-center">
                            <svg class="w-3 h-3 text-primary-foreground" fill="none" viewBox="0 0 24 24"
                                 stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3"
                                      d="M5 13l4 4L19 7"/>
                            </svg>
                        </div>
                        <span class="text-sm text-foreground font-medium">Organization Managment is active</span>
                    </li>
                    <li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
                        <div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
                            <div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
                        </div>
                        <span class="text-sm text-muted-foreground">Events monitoring coming soon</span>
                    </li>
                    <li class="flex items-center gap-3 p-3 rounded-lg bg-muted/30 border border-border/30">
                        <div class="flex-shrink-0 w-5 h-5 rounded-full bg-muted flex items-center justify-center">
                            <div class="w-2 h-2 rounded-full bg-muted-foreground"></div>
                        </div>
                        <span class="text-sm text-muted-foreground">Real-time updates coming soon</span>
                    </li>
                </ul>
            </CardContent>
        </Card>
    </div>
</main>
