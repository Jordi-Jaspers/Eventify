import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
    aValidUpdatePasswordRequest,
    aMismatchedUpdatePasswordRequest,
    aWeakPasswordRequest
} from './fixtures/password.fixtures';

// Mock the controller module
vi.mock('$lib/api/authentication/PasswordController.ts', () => ({
    updatePassword: vi.fn(),
    requestPasswordReset: vi.fn()
}));

// Mock svelte-sonner toast
vi.mock('svelte-sonner', () => ({
    toast: {
        success: vi.fn(),
        error: vi.fn()
    }
}));

// Mock error handler
vi.mock('$lib/utils/error-handler.ts', () => ({
    handleError: vi.fn((err: unknown, fallback: string) => ({
        message: (err as { errorMessage?: string })?.errorMessage ?? fallback
    }))
}));

describe('ChangePasswordService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('Factory', () => {
        it('createChangePasswordService returns a service object with expected shape', async () => {
            // Given: The module exists
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );

            // When: The factory is called
            const service = createChangePasswordService();

            // Then: A service object with expected shape is returned
            expect(service).toBeDefined();
            expect(typeof service.submit).toBe('function');
            expect(typeof service.reset).toBe('function');
        });

        it('initial state has empty fields, submitting=false, no error', async () => {
            // Given: A freshly created service
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );

            // When: Service is created
            const service = createChangePasswordService();

            // Then: Initial state is correct
            expect(service.currentPassword).toBe('');
            expect(service.newPassword).toBe('');
            expect(service.confirmPassword).toBe('');
            expect(service.submitting).toBe(false);
            expect(service.error).toBeNull();
        });
    });

    describe('Validation', () => {
        it('canSubmit is false when all fields are empty', async () => {
            // Given: A freshly created service with empty fields
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();

            // When: No fields are filled
            // Then: canSubmit is false
            expect(service.canSubmit).toBe(false);
        });

        it('canSubmit is false when new password and confirm password do not match', async () => {
            // Given: A service with mismatched passwords
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aMismatchedUpdatePasswordRequest();

            // When: Fields are set with mismatched passwords
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // Then: canSubmit is false
            expect(service.canSubmit).toBe(false);
        });

        it('canSubmit is false when new password is too weak', async () => {
            // Given: A service with a weak new password
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aWeakPasswordRequest();

            // When: Fields are set with a weak password
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // Then: canSubmit is false (password not strong enough)
            expect(service.canSubmit).toBe(false);
        });

        it('canSubmit is true when all fields are valid and passwords match', async () => {
            // Given: A service with valid, matching passwords
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();

            // When: All fields are set correctly
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // Then: canSubmit is true
            expect(service.canSubmit).toBe(true);
        });

        it('passwordsMatch is false when new and confirm differ', async () => {
            // Given: A service with mismatched passwords
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();

            // When: Passwords are set to different values
            service.newPassword = 'NewPass456!';
            service.confirmPassword = 'DifferentPass789!';

            // Then: passwordsMatch is false
            expect(service.passwordsMatch).toBe(false);
        });

        it('passwordsMatch is true when new and confirm are identical', async () => {
            // Given: A service with matching passwords
            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();

            // When: Passwords are set to the same value
            service.newPassword = 'NewPass456!';
            service.confirmPassword = 'NewPass456!';

            // Then: passwordsMatch is true
            expect(service.passwordsMatch).toBe(true);
        });
    });

    describe('submit()', () => {
        it('calls updatePassword with correct payload on valid form', async () => {
            // Given: A service with valid fields and a successful API call
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            vi.mocked(updatePassword).mockResolvedValueOnce(undefined);

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() is called
            await service.submit();

            // Then: updatePassword is called with the correct payload
            expect(updatePassword).toHaveBeenCalledWith({
                currentPassword: req.currentPassword,
                newPassword: req.newPassword,
                confirmPassword: req.confirmPassword
            });
        });

        it('shows success toast on successful submit', async () => {
            // Given: A service with valid fields and a successful API call
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            vi.mocked(updatePassword).mockResolvedValueOnce(undefined);
            const { toast } = await import('svelte-sonner');

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() succeeds
            await service.submit();

            // Then: Success toast is shown
            expect(toast.success).toHaveBeenCalled();
        });

        it('resets fields after successful submit', async () => {
            // Given: A service with valid fields and a successful API call
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            vi.mocked(updatePassword).mockResolvedValueOnce(undefined);

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() succeeds
            await service.submit();

            // Then: Fields are reset to empty
            expect(service.currentPassword).toBe('');
            expect(service.newPassword).toBe('');
            expect(service.confirmPassword).toBe('');
        });

        it('sets submitting=true during submit and false after', async () => {
            // Given: A service with valid fields
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            let resolveCall!: () => void;
            vi.mocked(updatePassword).mockReturnValueOnce(
                new Promise<void>((resolve) => {
                    resolveCall = resolve;
                })
            );

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() is in progress
            const submitPromise = service.submit();
            expect(service.submitting).toBe(true);

            // Then: After completion, submitting is false
            resolveCall();
            await submitPromise;
            expect(service.submitting).toBe(false);
        });

        it('sets error message when current password is wrong (401)', async () => {
            // Given: The API returns 401 for wrong current password
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            const apiError = { errorMessage: 'Current password is incorrect', status: 401 };
            vi.mocked(updatePassword).mockRejectedValueOnce(apiError);

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() fails with 401
            await service.submit();

            // Then: Error is set and submitting is false
            expect(service.error).toBe('Current password is incorrect');
            expect(service.submitting).toBe(false);
        });

        it('sets error message on network error', async () => {
            // Given: The API call throws a network error
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            vi.mocked(updatePassword).mockRejectedValueOnce(new Error('Network request failed'));

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // When: submit() fails with network error
            await service.submit();

            // Then: Error is set
            expect(service.error).not.toBeNull();
            expect(service.submitting).toBe(false);
        });

        it('does not call updatePassword when canSubmit is false', async () => {
            // Given: A service with empty fields (invalid form)
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();

            // When: submit() is called with empty fields
            await service.submit();

            // Then: updatePassword is never called
            expect(updatePassword).not.toHaveBeenCalled();
        });

        it('clears previous error on new submit attempt', async () => {
            // Given: A service that previously had an error, now with valid fields
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            const apiError = { errorMessage: 'Current password is incorrect', status: 401 };
            vi.mocked(updatePassword)
                .mockRejectedValueOnce(apiError)
                .mockResolvedValueOnce(undefined);

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;

            // First submit fails
            await service.submit();
            expect(service.error).not.toBeNull();

            // When: submit() is called again
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;
            await service.submit();

            // Then: Error is cleared on success
            expect(service.error).toBeNull();
        });
    });

    describe('reset()', () => {
        it('clears all fields and error', async () => {
            // Given: A service with filled fields and an error
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            const apiError = { errorMessage: 'Wrong password', status: 401 };
            vi.mocked(updatePassword).mockRejectedValueOnce(apiError);

            const { createChangePasswordService } = await import(
                '$lib/api/authentication/service/ChangePasswordService.svelte.ts'
            );
            const service = createChangePasswordService();
            const req = aValidUpdatePasswordRequest();
            service.currentPassword = req.currentPassword;
            service.newPassword = req.newPassword;
            service.confirmPassword = req.confirmPassword;
            await service.submit();

            // When: reset() is called
            service.reset();

            // Then: All fields and error are cleared
            expect(service.currentPassword).toBe('');
            expect(service.newPassword).toBe('');
            expect(service.confirmPassword).toBe('');
            expect(service.error).toBeNull();
        });
    });
});
