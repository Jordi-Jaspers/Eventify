<script lang="ts">
    import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '$lib/components/ui/card';
    import {Input} from '$lib/components/ui/input';
    import {Label} from '$lib/components/ui/label';
    import {Button} from '$lib/components/ui/button';
    import {Alert, AlertDescription} from '$lib/components/ui/alert';
    import UserSearchCombobox from '$lib/components/user/UserSearchCombobox.svelte';
    import {Building2, CircleAlert, LoaderCircle} from '@lucide/svelte';
    import {createOrganization} from '$lib/api/organization/OrganizationController';
    import type {OrganizationResponse, UserResponse} from '$lib/api/models';
    import {handleError} from '$lib/utils/error-handler';
    import {toast} from 'svelte-sonner';
    import {goto} from '$app/navigation';
    import {CLIENT_ROUTES} from '$lib/config/routes';

    let organizationName: string = $state('');
    let selectedOwner: UserResponse | undefined = $state(undefined);
    let isSubmitting: boolean = $state(false);
    let errors: Record<string, string> = $state({});

    function validateForm(): boolean {
        errors = {};

        const trimmedName: string = organizationName.trim();

        if (!trimmedName) {
            errors.name = 'Organization name is required';
            return false;
        }

        if (trimmedName.length < 3) {
            errors.name = 'Organization name must be at least 3 characters';
            return false;
        }

        if (trimmedName.length > 100) {
            errors.name = 'Organization name must not exceed 100 characters';
            return false;
        }

        if (!selectedOwner) {
            errors.owner = 'Please select an organization owner';
            return false;
        }

        return true;
    }

    async function handleSubmit(event: SubmitEvent): Promise<void> {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        isSubmitting = true;
        errors = {};

        try {
            if (!selectedOwner || !selectedOwner.email) {
                throw new Error('Owner must be selected');
            }
            
            const ownerEmail: string = selectedOwner.email;
            const response: OrganizationResponse = await createOrganization(
                organizationName.trim(),
                ownerEmail
            );
            toast.success(`Organization "${response.name}" created successfully`);
            // Navigate to dashboard or organizations list
            goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
        } catch (err: unknown) {
            const {message}: {message: string} = handleError(err, 'Failed to create organization');
            errors.general = message;
            toast.error(message);
        } finally {
            isSubmitting = false;
        }
    }

    function handleCancel(): void {
        goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
    }
</script>

<div class="max-w-2xl mx-auto px-4 py-8">
    <!-- Page Header -->
    <div class="mb-8">
        <h1 class="text-3xl font-bold text-primary mb-2">
            Create Organization
        </h1>
        <p class="text-muted-foreground">
            Provision a new organization for multi-tenant management
        </p>
    </div>

    <!-- Form Card -->
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/10 transition-shadow duration-300">
        <CardHeader class="space-y-1">
            <CardTitle class="text-2xl flex items-center gap-2">
                <Building2 class="w-5 h-5 text-primary" />
                Organization Details
            </CardTitle>
            <CardDescription>
                Enter the organization name to create a new tenant
            </CardDescription>
        </CardHeader>

        <CardContent>
            <!-- General Error Alert -->
            {#if errors.general}
                <Alert variant="destructive" class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm">
                    <CircleAlert class="h-4 w-4" />
                    <AlertDescription>{errors.general}</AlertDescription>
                </Alert>
            {/if}

            <form onsubmit={handleSubmit} class="space-y-6">
                <!-- Organization Name Field -->
                <div class="space-y-2">
                    <Label for="organizationName">Organization Name</Label>
                    <Input
                        id="organizationName"
                        type="text"
                        placeholder="Enter organization name (e.g., Acme Corporation)"
                        bind:value={organizationName}
                        disabled={isSubmitting}
                        class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                        aria-invalid={!!errors.name}
                        required
                    />
                    {#if errors.name}
                        <p class="text-sm text-destructive mt-1">{errors.name}</p>
                    {/if}
                    <p class="text-xs text-muted-foreground">
                        Must be between 3 and 100 characters
                    </p>
                </div>

                <!-- Owner Selection Field -->
                <div class="space-y-2">
                    <Label for="ownerSearch">Organization Owner</Label>
                    <UserSearchCombobox
                        onSelect={(user: UserResponse) => { selectedOwner = user }}
                        selectedUser={selectedOwner}
                        placeholder="Search for organization owner..."
                        disabled={isSubmitting}
                    />
                    {#if errors.owner}
                        <p class="text-sm text-destructive mt-1">{errors.owner}</p>
                    {/if}
                    <p class="text-xs text-muted-foreground">
                        The owner will have full control of the organization
                    </p>
                </div>

                <!-- Action Buttons -->
                <div class="flex gap-3">
                    <Button
                        type="submit"
                        class="flex-1 bg-gradient-to-r from-primary to-accent hover:from-primary/90 hover:to-accent/90 transition-all shadow-lg hover:shadow-primary/50 focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
                        disabled={isSubmitting}
                    >
                        {#if isSubmitting}
                            <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                            Creating...
                        {:else}
                            <Building2 class="mr-2 h-4 w-4" />
                            Create Organization
                        {/if}
                    </Button>

                    <Button
                        type="button"
                        variant="outline"
                        class="bg-background/50 border-border/50 hover:bg-destructive/10 hover:border-destructive/50 transition-all focus-visible:ring-2 focus-visible:ring-destructive focus-visible:ring-offset-2"
                        onclick={handleCancel}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </Button>
                </div>
            </form>
        </CardContent>
    </Card>

    <!-- Info Box -->
    <div class="mt-6 p-4 rounded-lg border border-border/50 bg-primary/5 backdrop-blur-sm">
        <p class="text-sm text-muted-foreground">
            <strong class="text-foreground">Note:</strong> Creating an organization will generate a unique slug
            and set the status to TRIAL. You can manage organization settings after creation.
        </p>
    </div>
</div>
