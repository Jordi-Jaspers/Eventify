import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock the API client
vi.mock('$lib/api/client.ts', () => ({
    client: {
        POST: vi.fn()
    }
}));

describe('PasswordController', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('requestPasswordReset', () => {
        it('calls POST /v1/public/reset-password/request with email query param', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: undefined });

            // When: requestPasswordReset is called
            const { requestPasswordReset } = await import(
                '$lib/api/authentication/PasswordController.ts'
            );
            await requestPasswordReset('user@example.com');

            // Then: The correct endpoint is called with the email as query param
            expect(client.POST).toHaveBeenCalledWith('/v1/public/reset-password/request', {
                params: { query: { email: 'user@example.com' } }
            });
        });

        it('throws when API returns an error', async () => {
            // Given: The API returns an error
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'User not found', status: 404 };
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: requestPasswordReset is called
            const { requestPasswordReset } = await import(
                '$lib/api/authentication/PasswordController.ts'
            );

            // Then: The error is thrown
            await expect(requestPasswordReset('unknown@example.com')).rejects.toEqual(apiError);
        });
    });

    describe('resetPassword', () => {
        it('calls POST /v1/public/reset-password with request body', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: undefined });

            const request = {
                token: 'reset-token-abc',
                newPassword: 'NewPass123!',
                confirmPassword: 'NewPass123!'
            };

            // When: resetPassword is called
            const { resetPassword } = await import('$lib/api/authentication/PasswordController.ts');
            await resetPassword(request);

            // Then: The correct endpoint is called with the body
            expect(client.POST).toHaveBeenCalledWith('/v1/public/reset-password', {
                body: request
            });
        });

        it('throws when API returns an error (invalid token)', async () => {
            // Given: The API returns a 400 error for invalid token
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Invalid or expired token', status: 400 };
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: resetPassword is called with an invalid token
            const { resetPassword } = await import('$lib/api/authentication/PasswordController.ts');

            // Then: The error is thrown
            await expect(
                resetPassword({
                    token: 'expired-token',
                    newPassword: 'NewPass123!',
                    confirmPassword: 'NewPass123!'
                })
            ).rejects.toEqual(apiError);
        });
    });

    describe('updatePassword', () => {
        it('calls POST /v1/password/update-password with correct body', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: undefined });

            const request = {
                currentPassword: 'OldPass123!',
                newPassword: 'NewPass456!',
                confirmPassword: 'NewPass456!'
            };

            // When: updatePassword is called
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            await updatePassword(request);

            // Then: The correct endpoint is called with the body
            expect(client.POST).toHaveBeenCalledWith('/v1/password/update-password', {
                body: request
            });
        });

        it('resolves without returning a value on success', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: undefined });

            // When: updatePassword is called
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');
            const result: void = await updatePassword({
                currentPassword: 'OldPass123!',
                newPassword: 'NewPass456!',
                confirmPassword: 'NewPass456!'
            });

            // Then: Nothing is returned
            expect(result).toBeUndefined();
        });

        it('throws when current password is wrong (401)', async () => {
            // Given: The API returns 401 for wrong current password
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Current password is incorrect', status: 401 };
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: updatePassword is called with wrong current password
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');

            // Then: The error is thrown
            await expect(
                updatePassword({
                    currentPassword: 'WrongPass!',
                    newPassword: 'NewPass456!',
                    confirmPassword: 'NewPass456!'
                })
            ).rejects.toEqual(apiError);
        });

        it('throws when passwords do not match (400)', async () => {
            // Given: The API returns 400 for mismatched passwords
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Passwords do not match', status: 400 };
            vi.mocked(client.POST).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: updatePassword is called with mismatched passwords
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');

            // Then: The error is thrown
            await expect(
                updatePassword({
                    currentPassword: 'OldPass123!',
                    newPassword: 'NewPass456!',
                    confirmPassword: 'DifferentPass789!'
                })
            ).rejects.toEqual(apiError);
        });

        it('throws on network error', async () => {
            // Given: The API call throws a network error
            const { client } = await import('$lib/api/client.ts');
            const networkError = new Error('Network request failed');
            vi.mocked(client.POST).mockRejectedValueOnce(networkError);

            // When: updatePassword is called
            const { updatePassword } = await import('$lib/api/authentication/PasswordController.ts');

            // Then: The network error propagates
            await expect(
                updatePassword({
                    currentPassword: 'OldPass123!',
                    newPassword: 'NewPass456!',
                    confirmPassword: 'NewPass456!'
                })
            ).rejects.toThrow('Network request failed');
        });
    });
});
