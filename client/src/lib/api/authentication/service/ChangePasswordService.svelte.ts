import { updatePassword } from '$lib/api/authentication/PasswordController';
import { handleError } from '$lib/utils/error-handler';
import { validatePassword } from '$lib/utils/password-validator';
import { toast } from 'svelte-sonner';

export function createChangePasswordService() {
    let currentPassword: string = $state('');
    let newPassword: string = $state('');
    let confirmPassword: string = $state('');
    let submitting: boolean = $state(false);
    let error: string | null = $state(null);

    const passwordsMatch: boolean = $derived(newPassword === confirmPassword);
    const canSubmit: boolean = $derived(
        currentPassword.trim().length > 0 &&
        newPassword.trim().length > 0 &&
        confirmPassword.trim().length > 0 &&
        passwordsMatch &&
        validatePassword(newPassword).isValid
    );

    async function submit(): Promise<void> {
        if (!canSubmit) return;

        submitting = true;
        error = null;

        try {
            await updatePassword({ oldPassword: currentPassword, newPassword, confirmPassword });
            toast.success('Password updated successfully');
            currentPassword = '';
            newPassword = '';
            confirmPassword = '';
        } catch (err: unknown) {
            const { message }: { message: string } = handleError(err, 'Failed to update password');
            error = message;
        } finally {
            submitting = false;
        }
    }

    function reset(): void {
        currentPassword = '';
        newPassword = '';
        confirmPassword = '';
        error = null;
    }

    return {
        get currentPassword(): string { return currentPassword; },
        set currentPassword(v: string) { currentPassword = v; },
        get newPassword(): string { return newPassword; },
        set newPassword(v: string) { newPassword = v; },
        get confirmPassword(): string { return confirmPassword; },
        set confirmPassword(v: string) { confirmPassword = v; },
        get submitting(): boolean { return submitting; },
        get error(): string | null { return error; },
        get passwordsMatch(): boolean { return passwordsMatch; },
        get canSubmit(): boolean { return canSubmit; },
        submit,
        reset
    };
}

export type ChangePasswordService = ReturnType<typeof createChangePasswordService>;
