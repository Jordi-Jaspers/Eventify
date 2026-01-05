<script lang="ts">
    import { goto } from '$app/navigation';
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import { Badge } from '$lib/components/ui/badge';
    import {Clock, Shield, User, Building2} from '@lucide/svelte';
    import {authStore} from '$lib/stores/auth';
    import { organizationStore } from '$lib/stores/organization.svelte';
    import type { UserOrganizationResponse } from '$lib/api/models';
    import { CLIENT_ROUTES } from '$lib/config/routes';
    import {getOrganizationalRoleBadgeClass, getUserRoleBadgeClass} from '$lib/utils/role';

    const organizations: UserOrganizationResponse[] = $derived(organizationStore.organizations);
    const loading: boolean = $derived(organizationStore.loading);
    const hasOrganizations: boolean = $derived(organizations.length > 0);

    async function handleOrgClick(orgId: number): Promise<void> {
        organizationStore.switchOrganization(orgId);
        await goto(CLIENT_ROUTES.ORGANIZATION_DASHBOARD_PAGE(orgId).path);
    }
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
                        <Badge class="{getUserRoleBadgeClass($authStore.user?.role)} font-medium text-foreground" >
                            {$authStore.user?.role || 'N/A'}
                        </Badge>
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

        <!-- Organizations Section -->
        {#if hasOrganizations && !loading}
            <div class="space-y-4">
                <!-- Section Header -->
                <div class="flex items-center justify-between">
                    <div>
                        <h2 class="text-2xl font-bold flex items-center gap-2">
                            <Building2 class="w-5 h-5 text-primary"/>
                            Your Organizations
                        </h2>
                        <p class="text-sm text-muted-foreground mt-1">
                            {organizations.length} {organizations.length === 1 ? 'organization' : 'organizations'}
                        </p>
                    </div>
                </div>

                <!-- Organization Grid -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {#each organizations as org (org.organizationId)}
                        <button
                            onclick={() => handleOrgClick(org.organizationId!)}
                            class="text-left"
                        >
                            <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg hover:shadow-primary/50 hover:scale-[1.02] transition-all duration-200 cursor-pointer h-full">
                                <CardHeader class="space-y-1">
                                    <CardTitle class="text-lg flex items-center gap-2">
                                        <Building2 class="w-4 h-4 text-primary"/>
                                        {org.organizationName}
                                    </CardTitle>
                                    <CardDescription>
                                        <Badge class={getOrganizationalRoleBadgeClass(org.role)}>
                                            {org.role}
                                        </Badge>
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <div class="flex items-center gap-2 text-xs text-muted-foreground">
                                        <Clock class="w-3 h-3"/>
                                        <span>Member since {new Date(org.joinedAt!).toLocaleDateString()}</span>
                                    </div>
                                </CardContent>
                            </Card>
                        </button>
                    {/each}
                </div>
            </div>
        {:else if loading}
            <!-- Loading Skeletons -->
            <div class="space-y-4">
                <div class="flex items-center justify-between">
                    <div>
                        <h2 class="text-2xl font-bold flex items-center gap-2">
                            <Building2 class="w-5 h-5 text-primary"/>
                            Your Organizations
                        </h2>
                        <div class="h-4 w-32 bg-muted/50 rounded animate-pulse mt-1"></div>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {#each Array(3) as _, i}
                        <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
                            <CardHeader class="space-y-2">
                                <div class="h-5 bg-muted/50 rounded animate-pulse w-3/4"></div>
                                <div class="h-4 bg-muted/50 rounded animate-pulse w-1/2"></div>
                            </CardHeader>
                            <CardContent>
                                <div class="h-3 bg-muted/50 rounded animate-pulse w-full"></div>
                            </CardContent>
                        </Card>
                    {/each}
                </div>
            </div>
        {/if}
    </div>
</main>
