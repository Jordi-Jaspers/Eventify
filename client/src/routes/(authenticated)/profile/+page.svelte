<script lang="ts">
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import Badge from '$lib/components/ui/badge/badge.svelte';
    import {
        Building2,
        Calendar,
        CircleCheckBig,
        CircleX,
        Clock,
        Key,
        Mail,
        Shield,
        User
    } from '@lucide/svelte';
    import { currentUser } from '$lib/stores/auth';
    import { createProfileService } from '$lib/api/user/ProfileService.svelte';
    import EditableField from '$lib/components/user/EditableField.svelte';

    const profileService = createProfileService();

    let userData = $derived($currentUser);

    function formatDate(dateString: string | undefined): string {
        if (!dateString) return 'N/A';

        try {
            const date: Date = new Date(dateString);
            return new Intl.DateTimeFormat('en-US', {
                dateStyle: 'medium',
                timeStyle: 'short'
            }).format(date);
        } catch {
            return 'Invalid date';
        }
    }

    function formatRole(role: string | undefined): string {
        if (!role) return 'N/A';
        return role.charAt(0) + role.slice(1).toLowerCase();
    }

    function getRoleBadgeVariant(role: string | undefined): 'default' | 'secondary' | 'outline' {
        if (!role) return 'outline';
        
        switch (role) {
            case 'OWNER':
                return 'default';
            case 'ADMIN':
                return 'secondary';
            case 'MEMBER':
                return 'outline';
            default:
                return 'outline';
        }
    }
</script>

<svelte:head>
    <title>Profile - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
        {#if userData}
            <!-- Profile Header Card -->
            <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
                <!-- Gradient overlay -->
                <div class="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-accent/10 opacity-50"></div>

                <CardHeader class="relative z-10">
                    <CardTitle class="text-2xl flex items-center gap-2">
                        <User class="w-6 h-6 text-primary"/>
                        User Profile
                    </CardTitle>
                    <CardDescription>
                        Complete account information and settings
                    </CardDescription>
                </CardHeader>

                <CardContent class="space-y-6 relative z-10">
                    <!-- Personal Information -->
                    <div>
                        <h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2">
                            <User class="w-4 h-4"/>
                            Personal Information
                        </h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- First Name -->
                            <EditableField
                                label="First Name"
                                value={userData.firstName || ''}
                                editing={profileService.firstNameState.editing}
                                saving={profileService.firstNameState.saving}
                                tempValue={profileService.firstNameState.tempValue}
                                onStartEdit={() => profileService.startEdit('firstName')}
                                onSave={() => profileService.saveField('firstName')}
                                onTempValueChange={(v) => profileService.updateTempValue('firstName', v)}
                                onKeydown={(e) => profileService.handleKeydown('firstName', e)}
                            />

                            <!-- Last Name -->
                            <EditableField
                                label="Last Name"
                                value={userData.lastName || ''}
                                editing={profileService.lastNameState.editing}
                                saving={profileService.lastNameState.saving}
                                tempValue={profileService.lastNameState.tempValue}
                                onStartEdit={() => profileService.startEdit('lastName')}
                                onSave={() => profileService.saveField('lastName')}
                                onTempValueChange={(v) => profileService.updateTempValue('lastName', v)}
                                onKeydown={(e) => profileService.handleKeydown('lastName', e)}
                            />

                            <!-- Email (Read-Only) -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50 md:col-span-2 opacity-70 cursor-not-allowed">
                                <div class="flex items-center gap-2 mb-1">
                                    <Mail class="w-3 h-3 text-primary"/>
                                    <p class="text-xs text-muted-foreground">Email Address <span class="text-muted-foreground/70">(read-only)</span></p>
                                </div>
                                <p class="font-medium text-foreground">{userData.email || 'N/A'}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Account Security -->
                    <div>
                        <h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2">
                            <Shield class="w-4 h-4"/>
                            Account Security
                        </h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Role -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <div class="flex items-center gap-2 mb-1">
                                    <Shield class="w-3 h-3 text-primary"/>
                                    <p class="text-xs text-muted-foreground">Role</p>
                                </div>
                                <p class="font-medium text-foreground">{formatRole(userData.role)}</p>
                            </div>

                            <!-- Permissions -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <div class="flex items-center gap-2 mb-1">
                                    <Key class="w-3 h-3 text-primary"/>
                                    <p class="text-xs text-muted-foreground">Permissions</p>
                                </div>
                                {#if userData.permissions && userData.permissions.length > 0}
                                    <div class="flex flex-wrap gap-1 mt-2">
                                        {#each userData.permissions as permission}
                                            <Badge variant="secondary" class="text-xs">
                                                {permission}
                                            </Badge>
                                        {/each}
                                    </div>
                                {:else}
                                    <p class="text-sm text-muted-foreground">No permissions assigned</p>
                                {/if}
                            </div>
                        </div>
                    </div>

                    <!-- Account Status -->
                    <div>
                        <h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2">
                            <CircleCheckBig class="w-4 h-4"/>
                            Account Status
                        </h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Validated Status -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <p class="text-xs text-muted-foreground mb-2">Email Validation</p>
                                <div class="flex items-center gap-2">
                                    {#if userData.validated}
                                        <CircleCheckBig class="w-4 h-4 text-green-500"/>
                                        <span class="text-sm font-medium text-green-500">Verified</span>
                                    {:else}
                                        <CircleX class="w-4 h-4 text-yellow-500"/>
                                        <span class="text-sm font-medium text-yellow-500">Not Verified</span>
                                    {/if}
                                </div>
                            </div>

                            <!-- Enabled Status -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <p class="text-xs text-muted-foreground mb-2">Account Status</p>
                                <div class="flex items-center gap-2">
                                    {#if userData.enabled}
                                        <CircleCheckBig class="w-4 h-4 text-green-500"/>
                                        <span class="text-sm font-medium text-green-500">Active</span>
                                    {:else}
                                        <CircleX class="w-4 h-4 text-red-500"/>
                                        <span class="text-sm font-medium text-red-500">Disabled</span>
                                    {/if}
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Organizations -->
                    <div>
                        <h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2">
                            <Building2 class="w-4 h-4"/>
                            Organizations
                        </h3>
                        {#if userData.organizations && userData.organizations.length > 0}
                            <div class="space-y-3">
                                {#each userData.organizations as org}
                                    <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                        <div class="flex items-center justify-between gap-4">
                                            <div class="flex-1 min-w-0">
                                                <a 
                                                    href="/organizations/{org.organizationSlug}"
                                                    class="text-sm font-medium text-foreground hover:text-primary transition-colors"
                                                >
                                                    {org.organizationName}
                                                </a>
                                                <p class="text-xs text-muted-foreground mt-1">
                                                    Joined {formatDate(org.joinedAt)}
                                                </p>
                                            </div>
                                            <Badge variant={getRoleBadgeVariant(org.role)}>
                                                {org.role}
                                            </Badge>
                                        </div>
                                    </div>
                                {/each}
                            </div>
                        {:else}
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <p class="text-sm text-muted-foreground">You are not a member of any organization</p>
                            </div>
                        {/if}
                    </div>

                    <!-- Account Timestamps -->
                    <div>
                        <h3 class="text-sm font-semibold text-muted-foreground mb-3 flex items-center gap-2">
                            <Clock class="w-4 h-4"/>
                            Account Timeline
                        </h3>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Created Date -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <div class="flex items-center gap-2 mb-1">
                                    <Calendar class="w-3 h-3 text-primary"/>
                                    <p class="text-xs text-muted-foreground">Account Created</p>
                                </div>
                                <p class="font-medium text-foreground text-sm">{formatDate(userData.createdAt)}</p>
                            </div>

                            <!-- Last Login -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <div class="flex items-center gap-2 mb-1">
                                    <Clock class="w-3 h-3 text-primary"/>
                                    <p class="text-xs text-muted-foreground">Last Login</p>
                                </div>
                                <p class="font-medium text-foreground text-sm">{formatDate(userData.lastLogin)}</p>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        {/if}
    </div>
</main>
