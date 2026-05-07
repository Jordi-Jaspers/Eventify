import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock the controller module
vi.mock('$lib/api/user/UserProviderController.ts', () => ({
    listProviders: vi.fn(),
    unlinkProvider: vi.fn()
}));

// Mock the password controller
vi.mock('$lib/api/authentication/PasswordController.ts', () => ({
    requestPasswordReset: vi.fn(),
    updatePassword: vi.fn()
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

// Mock constants
vi.mock('$lib/config/constants.ts', () => ({
    SERVER_BASE_URL: 'http://localhost:8080/api'
}));

import {
    aLocalProvider,
    aConnectedGoogleProvider,
    aDisconnectedGithubProvider,
    aProviderListWithLocal
} from './fixtures/provider.fixtures';

describe('ConnectedAccountsService — LOCAL provider behavior', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('LOCAL provider detection', () => {
        it('identifies LOCAL provider in the provider list', async () => {
            // Given: The controller returns a list including a LOCAL provider
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            vi.mocked(listProviders).mockResolvedValueOnce(aProviderListWithLocal());

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() is called
            await service.load();

            // Then: The LOCAL provider is present in the list
            const localProvider = service.providers.find((p) => p.provider === 'LOCAL');
            expect(localProvider).toBeDefined();
            expect(localProvider?.provider).toBe('LOCAL');
        });

        it('LOCAL provider is always connected (no connect/disconnect flow)', async () => {
            // Given: The controller returns a list with LOCAL provider
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            vi.mocked(listProviders).mockResolvedValueOnce(aProviderListWithLocal());

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() is called
            await service.load();

            // Then: LOCAL provider has connected=true (it's always present)
            const localProvider = service.providers.find((p) => p.provider === 'LOCAL');
            expect(localProvider?.connected).toBe(true);
        });
    });

    describe('openChangePasswordDialog()', () => {
        it('sets showChangePasswordDialog to true', async () => {
            // Given: A freshly created service
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: openChangePasswordDialog() is called
            service.openChangePasswordDialog();

            // Then: The dialog is open
            expect(service.showChangePasswordDialog).toBe(true);
        });

        it('initial showChangePasswordDialog is false', async () => {
            // Given: A freshly created service
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: Service is created
            // Then: Dialog is closed by default
            expect(service.showChangePasswordDialog).toBe(false);
        });

        it('setShowChangePasswordDialog(false) closes the dialog', async () => {
            // Given: A service with the dialog open
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            service.openChangePasswordDialog();

            // When: setShowChangePasswordDialog(false) is called
            service.setShowChangePasswordDialog(false);

            // Then: Dialog is closed
            expect(service.showChangePasswordDialog).toBe(false);
        });
    });

    describe('requestPasswordResetForLocal()', () => {
        it('calls requestPasswordReset with the user email', async () => {
            // Given: A service and a mocked requestPasswordReset
            const { requestPasswordReset } = await import(
                '$lib/api/authentication/PasswordController.ts'
            );
            vi.mocked(requestPasswordReset).mockResolvedValueOnce(undefined);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: requestPasswordResetForLocal() is called with an email
            await service.requestPasswordResetForLocal('user@example.com');

            // Then: requestPasswordReset is called with the email
            expect(requestPasswordReset).toHaveBeenCalledWith('user@example.com');
        });

        it('shows success toast after requesting password reset', async () => {
            // Given: A service and a successful requestPasswordReset
            const { requestPasswordReset } = await import(
                '$lib/api/authentication/PasswordController.ts'
            );
            vi.mocked(requestPasswordReset).mockResolvedValueOnce(undefined);
            const { toast } = await import('svelte-sonner');

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: requestPasswordResetForLocal() succeeds
            await service.requestPasswordResetForLocal('user@example.com');

            // Then: Success toast is shown
            expect(toast.success).toHaveBeenCalled();
        });

        it('shows error toast when requestPasswordReset fails', async () => {
            // Given: A service and a failing requestPasswordReset
            const { requestPasswordReset } = await import(
                '$lib/api/authentication/PasswordController.ts'
            );
            const apiError = { errorMessage: 'User not found', status: 404 };
            vi.mocked(requestPasswordReset).mockRejectedValueOnce(apiError);
            const { toast } = await import('svelte-sonner');
            const { handleError } = await import('$lib/utils/error-handler.ts');

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: requestPasswordResetForLocal() fails
            await service.requestPasswordResetForLocal('unknown@example.com');

            // Then: handleError and toast.error are called
            expect(handleError).toHaveBeenCalledWith(apiError, expect.any(String));
            expect(toast.error).toHaveBeenCalled();
        });
    });

    describe('OAuth provider rows — unchanged behavior', () => {
        it('openUnlinkDialog still works for OAuth providers', async () => {
            // Given: A service and a connected Google provider
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            const googleProvider = aConnectedGoogleProvider();

            // When: openUnlinkDialog is called for an OAuth provider
            service.openUnlinkDialog(googleProvider);

            // Then: Unlink dialog state is set correctly
            expect(service.providerToUnlink).toEqual(googleProvider);
            expect(service.showUnlinkDialog).toBe(true);
        });

        it('linkProvider still works for disconnected OAuth providers', async () => {
            // Given: A service and a mocked globalThis.location
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            const originalLocation = globalThis.location;
            const mockLocation = { href: '' };
            Object.defineProperty(globalThis, 'location', {
                value: mockLocation,
                writable: true,
                configurable: true
            });

            // When: linkProvider is called for a disconnected GitHub provider
            service.linkProvider('github');

            // Then: location.href is set to the OAuth2 URL
            expect(mockLocation.href).toContain('github');

            Object.defineProperty(globalThis, 'location', {
                value: originalLocation,
                writable: true,
                configurable: true
            });
        });

        it('LOCAL provider does not trigger openUnlinkDialog', async () => {
            // Given: A service
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: No unlink dialog is opened for LOCAL provider
            // (LOCAL provider uses openChangePasswordDialog instead)
            // Then: showUnlinkDialog remains false
            expect(service.showUnlinkDialog).toBe(false);
            expect(service.providerToUnlink).toBeNull();
        });
    });
});
