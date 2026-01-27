<script lang="ts">
    import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
    import Badge from '$lib/components/ui/badge/badge.svelte';
    import { InfoField } from '$lib/components/ui/info-field';
    import { SectionHeader } from '$lib/components/ui/section-header';
    import { StatusIndicator } from '$lib/components/ui/status-indicator';
    import {
        Building2,
        Calendar,
        CircleCheckBig,
        Clock,
        Key,
        Mail,
        Shield,
        User
    } from '@lucide/svelte';
    import { currentUser } from '$lib/stores/auth';
    import { createProfileService } from '$lib/api/user/service/ProfileService.svelte';
    import EditableField from '$lib/components/user/EditableField.svelte';
    import { OrganizationMembershipCard } from '$lib/components/profile';
    import { formatDateTime } from '$lib/utils/date';
    import { getUserRoleLabel } from '$lib/utils/role';
    import { SettingsNav } from '$lib/components/settings';
    import { CLIENT_ROUTES } from '$lib/config/routes';

    const profileService = createProfileService();

    let userData = $derived($currentUser);
</script>

<svelte:head>
    <title>Profile - Eventify</title>
</svelte:head>

<!-- Settings Navigation -->
<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_PAGE.path} />

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
                        <SectionHeader title="Personal Information" icon={User} />
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
                        <SectionHeader title="Account Security" icon={Shield} />
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Role -->
                            <InfoField label="Role" value={getUserRoleLabel(userData.role)} icon={Shield} />

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
                        <SectionHeader title="Account Status" icon={CircleCheckBig} />
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Validated Status -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <p class="text-xs text-muted-foreground mb-2">Email Validation</p>
                                <StatusIndicator 
                                    status={userData.validated} 
                                    activeLabel="Verified" 
                                    inactiveLabel="Not Verified"
                                    inactiveColor="text-yellow-500"
                                />
                            </div>

                            <!-- Enabled Status -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50">
                                <p class="text-xs text-muted-foreground mb-2">Account Status</p>
                                <StatusIndicator 
                                    status={userData.enabled} 
                                    activeLabel="Active" 
                                    inactiveLabel="Disabled"
                                />
                            </div>
                        </div>
                    </div>

                    <!-- Organizations -->
                    <div>
                        <SectionHeader title="Organizations" icon={Building2} />
                        {#if userData.organizations && userData.organizations.length > 0}
                            <div class="space-y-3">
                                {#each userData.organizations as org}
                                    <OrganizationMembershipCard membership={org} />
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
                        <SectionHeader title="Account Timeline" icon={Clock} />
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Created Date -->
                            <InfoField label="Account Created" value={formatDateTime(userData.createdAt)} icon={Calendar} class="text-sm" />

                            <!-- Last Login -->
                            <InfoField label="Last Login" value={formatDateTime(userData.lastLogin)} icon={Clock} class="text-sm" />
                        </div>
                    </div>
                </CardContent>
            </Card>
        {/if}
    </div>
</main>
