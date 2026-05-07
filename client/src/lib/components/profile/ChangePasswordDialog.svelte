<script lang="ts">
    import * as Dialog from '$lib/components/ui/dialog';
    import { Button } from '$lib/components/ui/button';
    import { Input } from '$lib/components/ui/input';
    import { Label } from '$lib/components/ui/label';
    import { Alert, AlertDescription } from '$lib/components/ui/alert';
    import PasswordStrengthMeter from '$lib/components/registration/PasswordStrengthMeter.svelte';
    import { createChangePasswordService } from '$lib/api/authentication/service/ChangePasswordService.svelte';
    import { CircleAlert, Eye, EyeOff, KeyRound, LoaderCircle } from '@lucide/svelte';

    interface Props {
        open: boolean;
        onOpenChange: (v: boolean) => void;
    }

    let { open, onOpenChange }: Props = $props();

    const service = createChangePasswordService();

    let showCurrentPassword: boolean = $state(false);
    let showNewPassword: boolean = $state(false);
    let showConfirmPassword: boolean = $state(false);

    function handleOpenChange(v: boolean): void {
        if (!v) {
            service.reset();
            showCurrentPassword = false;
            showNewPassword = false;
            showConfirmPassword = false;
        }
        onOpenChange(v);
    }

    async function handleSubmit(event: SubmitEvent): Promise<void> {
        event.preventDefault();
        await service.submit();
        if (!service.error) {
            handleOpenChange(false);
        }
    }

    const passwordsMatch: boolean = $derived(
        service.confirmPassword.length > 0 && service.passwordsMatch
    );
    const passwordsDontMatch: boolean = $derived(
        service.confirmPassword.length > 0 && !service.passwordsMatch
    );
</script>

<Dialog.Root {open} onOpenChange={handleOpenChange}>
    <Dialog.Content class="bg-card/95 backdrop-blur-xl border-border/50 sm:max-w-md">
        <Dialog.Header>
            <Dialog.Title class="flex items-center gap-2">
                <KeyRound class="h-5 w-5 text-primary" />
                Change Password
            </Dialog.Title>
            <Dialog.Description>
                Enter your current password and choose a new one.
            </Dialog.Description>
        </Dialog.Header>

        <form onsubmit={handleSubmit} class="space-y-4 py-2">
            {#if service.error}
                <Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
                    <CircleAlert class="h-4 w-4" />
                    <AlertDescription>{service.error}</AlertDescription>
                </Alert>
            {/if}

            <!-- Current Password -->
            <div class="space-y-2">
                <Label for="currentPassword">Current Password</Label>
                <div class="relative">
                    <Input
                        id="currentPassword"
                        type={showCurrentPassword ? 'text' : 'password'}
                        placeholder="Enter your current password"
                        bind:value={service.currentPassword}
                        disabled={service.submitting}
                        class="pr-10 bg-background/50 border-border/50"
                        required
                    />
                    <button
                        type="button"
                        onclick={() => { showCurrentPassword = !showCurrentPassword; }}
                        disabled={service.submitting}
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
                        aria-label={showCurrentPassword ? 'Hide password' : 'Show password'}
                    >
                        {#if showCurrentPassword}
                            <EyeOff class="h-4 w-4" />
                        {:else}
                            <Eye class="h-4 w-4" />
                        {/if}
                    </button>
                </div>
            </div>

            <!-- New Password -->
            <div class="space-y-2">
                <Label for="newPassword">New Password</Label>
                <div class="relative">
                    <Input
                        id="newPassword"
                        type={showNewPassword ? 'text' : 'password'}
                        placeholder="Enter your new password"
                        bind:value={service.newPassword}
                        disabled={service.submitting}
                        class="pr-10 bg-background/50 border-border/50"
                        required
                    />
                    <button
                        type="button"
                        onclick={() => { showNewPassword = !showNewPassword; }}
                        disabled={service.submitting}
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
                        aria-label={showNewPassword ? 'Hide password' : 'Show password'}
                    >
                        {#if showNewPassword}
                            <EyeOff class="h-4 w-4" />
                        {:else}
                            <Eye class="h-4 w-4" />
                        {/if}
                    </button>
                </div>
                <PasswordStrengthMeter password={service.newPassword} />
            </div>

            <!-- Confirm Password -->
            <div class="space-y-2">
                <Label for="confirmPassword">Confirm New Password</Label>
                <div class="relative">
                    <Input
                        id="confirmPassword"
                        type={showConfirmPassword ? 'text' : 'password'}
                        placeholder="Confirm your new password"
                        bind:value={service.confirmPassword}
                        disabled={service.submitting}
                        class="pr-10 bg-background/50 border-border/50"
                        required
                    />
                    <button
                        type="button"
                        onclick={() => { showConfirmPassword = !showConfirmPassword; }}
                        disabled={service.submitting}
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded"
                        aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                    >
                        {#if showConfirmPassword}
                            <EyeOff class="h-4 w-4" />
                        {:else}
                            <Eye class="h-4 w-4" />
                        {/if}
                    </button>
                </div>
                {#if passwordsMatch}
                    <p class="text-sm text-green-400 flex items-center gap-1">
                        <span class="inline-block w-1.5 h-1.5 rounded-full bg-green-400"></span>
                        Passwords match
                    </p>
                {:else if passwordsDontMatch}
                    <p class="text-sm text-destructive">Passwords must match</p>
                {/if}
            </div>

            <Dialog.Footer class="pt-2">
                <Button type="button" variant="outline" onclick={() => handleOpenChange(false)} disabled={service.submitting}>
                    Cancel
                </Button>
                <Button type="submit" disabled={!service.canSubmit || service.submitting}>
                    {#if service.submitting}
                        <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                        Updating...
                    {:else}
                        Update Password
                    {/if}
                </Button>
            </Dialog.Footer>
        </form>
    </Dialog.Content>
</Dialog.Root>
