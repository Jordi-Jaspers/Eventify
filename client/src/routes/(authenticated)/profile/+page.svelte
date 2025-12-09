<script lang="ts">
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Input} from '$lib/components/ui/input';
    import Badge from '$lib/components/ui/badge/badge.svelte';
    import {
        Calendar,
        CircleCheckBig,
        CircleX,
        Clock,
        Key,
        Mail,
        Pencil,
        Shield,
        User
    } from '@lucide/svelte';
    import {updateUserDetails} from '$lib/api/user/UserController';
    import type {UserDetailsResponse} from '$lib/api/models';
    import {handleError} from '$lib/utils/error-handler';
    import {toast} from 'svelte-sonner';
    import {currentUser, authStore} from '$lib/stores/auth';

    let userData = $derived($currentUser);

    // Edit state for each field
    let editingFirstName: boolean = $state(false);
    let editingLastName: boolean = $state(false);

    let savingFirstName: boolean = $state(false);
    let savingLastName: boolean = $state(false);

    let tempFirstName: string = $state('');
    let tempLastName: string = $state('');

    function startEditFirstName(): void {
        tempFirstName = userData?.firstName || '';
        editingFirstName = true;
    }

    function startEditLastName(): void {
        tempLastName = userData?.lastName || '';
        editingLastName = true;
    }

    async function saveFirstName(): Promise<void> {
        if (!userData || savingFirstName) return;

        const originalValue: string = userData.firstName || '';
        if (tempFirstName.trim() === originalValue) {
            editingFirstName = false;
            return;
        }

        savingFirstName = true;

        try {
            const updatedUser: UserDetailsResponse = await updateUserDetails(
                tempFirstName.trim(),
                userData.lastName || ''
            );
            authStore.setUser(updatedUser);
            editingFirstName = false;
            toast.success('First name updated');
        } catch (err: unknown) {
            const {message}: { message: string } = handleError(err, 'Failed to update first name');
            toast.error(message);
            tempFirstName = originalValue;
        } finally {
            savingFirstName = false;
        }
    }

    async function saveLastName(): Promise<void> {
        if (!userData || savingLastName) return;

        const originalValue: string = userData.lastName || '';
        if (tempLastName.trim() === originalValue) {
            editingLastName = false;
            return;
        }

        savingLastName = true;

        try {
            const updatedUser: UserDetailsResponse = await updateUserDetails(
                userData.firstName || '',
                tempLastName.trim()
            );
            authStore.setUser(updatedUser);
            editingLastName = false;
            toast.success('Last name updated');
        } catch (err: unknown) {
            const {message}: { message: string } = handleError(err, 'Failed to update last name');
            toast.error(message);
            tempLastName = originalValue;
        } finally {
            savingLastName = false;
        }
    }

    function cancelEditFirstName(): void {
        editingFirstName = false;
        tempFirstName = '';
    }

    function cancelEditLastName(): void {
        editingLastName = false;
        tempLastName = '';
    }

    function handleFirstNameKeydown(event: KeyboardEvent): void {
        if (event.key === 'Enter') {
            event.preventDefault();
            saveFirstName();
        } else if (event.key === 'Escape') {
            cancelEditFirstName();
        }
    }

    function handleLastNameKeydown(event: KeyboardEvent): void {
        if (event.key === 'Enter') {
            event.preventDefault();
            saveLastName();
        } else if (event.key === 'Escape') {
            cancelEditLastName();
        }
    }

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
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50 group relative">
                                <p class="text-xs text-muted-foreground mb-1">First Name</p>
                                {#if editingFirstName}
                                    <div class="flex items-center gap-2">
                                        <Input
                                            bind:value={tempFirstName}
                                            onkeydown={handleFirstNameKeydown}
                                            onblur={saveFirstName}
                                            disabled={savingFirstName}
                                            class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                                            autofocus
                                        />
                                        {#if savingFirstName}
                                            <div class="h-4 w-4 rounded-full border-2 border-primary border-t-transparent animate-spin"></div>
                                        {/if}
                                    </div>
                                {:else}
                                    <button
                                        onclick={startEditFirstName}
                                        class="w-full text-left font-medium text-foreground hover:text-primary transition-colors flex items-center justify-between"
                                    >
                                        <span>{userData.firstName || 'N/A'}</span>
                                        <Pencil class="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground" />
                                    </button>
                                {/if}
                            </div>

                            <!-- Last Name -->
                            <div class="p-4 rounded-lg bg-background/50 border border-border/50 group relative">
                                <p class="text-xs text-muted-foreground mb-1">Last Name</p>
                                {#if editingLastName}
                                    <div class="flex items-center gap-2">
                                        <Input
                                            bind:value={tempLastName}
                                            onkeydown={handleLastNameKeydown}
                                            onblur={saveLastName}
                                            disabled={savingLastName}
                                            class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                                            autofocus
                                        />
                                        {#if savingLastName}
                                            <div class="h-4 w-4 rounded-full border-2 border-primary border-t-transparent animate-spin"></div>
                                        {/if}
                                    </div>
                                {:else}
                                    <button
                                        onclick={startEditLastName}
                                        class="w-full text-left font-medium text-foreground hover:text-primary transition-colors flex items-center justify-between"
                                    >
                                        <span>{userData.lastName || 'N/A'}</span>
                                        <Pencil class="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground" />
                                    </button>
                                {/if}
                            </div>

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
                                <p class="font-medium text-foreground text-sm">{formatDate(userData.created)}</p>
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
