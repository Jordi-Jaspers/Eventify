import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { components } from '$lib/types/api.d.ts';

type ProviderResponse = components['schemas']['ProviderResponse'];

// Mock the controller module
vi.mock('$lib/api/user/UserProviderController.ts', () => ({
    listProviders: vi.fn(),
    unlinkProvider: vi.fn()
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
    aConnectedGoogleProvider,
    aConnectedGithubProvider,
    aProviderList,
    aDisconnectedGithubProvider
} from './fixtures/provider.fixtures';

describe('ConnectedAccountsService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('Factory', () => {
        it('createConnectedAccountsService returns a service object', async () => {
            // Given: The module exists
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );

            // When: The factory is called
            const service = createConnectedAccountsService();

            // Then: A service object with expected shape is returned
            expect(service).toBeDefined();
            expect(typeof service.load).toBe('function');
            expect(typeof service.linkProvider).toBe('function');
            expect(typeof service.openUnlinkDialog).toBe('function');
            expect(typeof service.confirmUnlink).toBe('function');
            expect(typeof service.setShowUnlinkDialog).toBe('function');
        });

        it('initial state has empty providers, loading=false, no dialog open', async () => {
            // Given: A freshly created service
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );

            // When: Service is created
            const service = createConnectedAccountsService();

            // Then: Initial state is correct
            expect(service.providers).toEqual([]);
            expect(service.loading).toBe(false);
            expect(service.unlinkingId).toBeNull();
            expect(service.showUnlinkDialog).toBe(false);
            expect(service.providerToUnlink).toBeNull();
        });
    });

    describe('load()', () => {
        it('sets providers from listProviders on success', async () => {
            // Given: The controller returns a provider list
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            const providers: ProviderResponse[] = aProviderList();
            vi.mocked(listProviders).mockResolvedValueOnce(providers);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() is called
            await service.load();

            // Then: Providers are set and loading is reset
            expect(service.providers).toHaveLength(2);
            expect(service.loading).toBe(false);
        });

        it('resets loading to false after successful load', async () => {
            // Given: The controller returns successfully
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            vi.mocked(listProviders).mockResolvedValueOnce([]);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() completes
            await service.load();

            // Then: loading is false
            expect(service.loading).toBe(false);
        });

        it('calls toast.error and handleError on listProviders failure', async () => {
            // Given: The controller throws an error
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            const apiError = { errorMessage: 'Network error' };
            vi.mocked(listProviders).mockRejectedValueOnce(apiError);

            const { toast } = await import('svelte-sonner');
            const { handleError } = await import('$lib/utils/error-handler.ts');

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() is called and fails
            await service.load();

            // Then: handleError and toast.error are called
            expect(handleError).toHaveBeenCalledWith(apiError, expect.any(String));
            expect(toast.error).toHaveBeenCalled();
        });

        it('keeps providers as empty array when load fails', async () => {
            // Given: The controller throws
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            vi.mocked(listProviders).mockRejectedValueOnce(new Error('fail'));

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: load() fails
            await service.load();

            // Then: providers stays empty and loading is reset
            expect(service.providers).toEqual([]);
            expect(service.loading).toBe(false);
        });
    });

    describe('linkProvider()', () => {
        it('sets location.href to Google OAuth2 URL for google provider', async () => {
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

            // When: linkProvider is called with 'google'
            service.linkProvider('google');

            // Then: location.href is set to the correct OAuth2 URL
            expect(mockLocation.href).toBe(
                'http://localhost:8080/api/v1/oauth2/authorization/google?mode=link'
            );

            Object.defineProperty(globalThis, 'location', {
                value: originalLocation,
                writable: true,
                configurable: true
            });
        });

        it('sets location.href to GitHub OAuth2 URL for github provider', async () => {
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

            // When: linkProvider is called with 'github'
            service.linkProvider('github');

            // Then: location.href is set to the correct OAuth2 URL
            expect(mockLocation.href).toBe(
                'http://localhost:8080/api/v1/oauth2/authorization/github?mode=link'
            );

            Object.defineProperty(globalThis, 'location', {
                value: originalLocation,
                writable: true,
                configurable: true
            });
        });
    });

    describe('openUnlinkDialog()', () => {
        it('sets providerToUnlink and opens dialog', async () => {
            // Given: A service with a provider
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            const provider: ProviderResponse = aConnectedGoogleProvider();

            // When: openUnlinkDialog is called
            service.openUnlinkDialog(provider);

            // Then: Dialog state is set correctly
            expect(service.providerToUnlink).toEqual(provider);
            expect(service.showUnlinkDialog).toBe(true);
        });
    });

    describe('setShowUnlinkDialog()', () => {
        it('sets showUnlinkDialog to true', async () => {
            // Given: A service with dialog closed
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: setShowUnlinkDialog(true) is called
            service.setShowUnlinkDialog(true);

            // Then: Dialog is open
            expect(service.showUnlinkDialog).toBe(true);
        });

        it('resets providerToUnlink when closing dialog', async () => {
            // Given: A service with a staged provider
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            service.openUnlinkDialog(aConnectedGoogleProvider());

            // When: setShowUnlinkDialog(false) is called
            service.setShowUnlinkDialog(false);

            // Then: Dialog is closed and providerToUnlink is reset
            expect(service.showUnlinkDialog).toBe(false);
            expect(service.providerToUnlink).toBeNull();
        });
    });

    describe('confirmUnlink()', () => {
        it('is a no-op when providerToUnlink is null', async () => {
            // Given: A service with no staged provider
            const { unlinkProvider } = await import('$lib/api/user/UserProviderController.ts');
            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();

            // When: confirmUnlink is called with no staged provider
            await service.confirmUnlink();

            // Then: unlinkProvider is never called
            expect(unlinkProvider).not.toHaveBeenCalled();
        });

        it('closes dialog, calls unlinkProvider, shows success toast, reloads list', async () => {
            // Given: A service with a staged provider and successful unlink
            const { listProviders, unlinkProvider } = await import(
                '$lib/api/user/UserProviderController.ts'
            );
            const { toast } = await import('svelte-sonner');
            vi.mocked(unlinkProvider).mockResolvedValueOnce(undefined);
            vi.mocked(listProviders).mockResolvedValueOnce([aDisconnectedGithubProvider()]);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            service.openUnlinkDialog(aConnectedGoogleProvider({ id: 5 }));

            // When: confirmUnlink is called
            await service.confirmUnlink();

            // Then: Dialog is closed, provider unlinked, success toast shown, list reloaded
            expect(service.showUnlinkDialog).toBe(false);
            expect(unlinkProvider).toHaveBeenCalledWith(5);
            expect(toast.success).toHaveBeenCalledWith('Provider unlinked');
            expect(listProviders).toHaveBeenCalled();
        });

        it('calls toast.error via handleError on unlink failure (409 last method)', async () => {
            // Given: The unlink call fails with 409
            const { listProviders, unlinkProvider } = await import(
                '$lib/api/user/UserProviderController.ts'
            );
            const { toast } = await import('svelte-sonner');
            const { handleError } = await import('$lib/utils/error-handler.ts');
            const apiError = { errorMessage: 'Cannot unlink last provider', status: 409 };
            vi.mocked(unlinkProvider).mockRejectedValueOnce(apiError);
            vi.mocked(listProviders).mockResolvedValueOnce([]);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            service.openUnlinkDialog(aConnectedGoogleProvider({ id: 3 }));

            // When: confirmUnlink fails
            await service.confirmUnlink();

            // Then: handleError and toast.error are called
            expect(handleError).toHaveBeenCalledWith(apiError, expect.any(String));
            expect(toast.error).toHaveBeenCalled();
        });

        it('reloads provider list even after 404 unlink failure', async () => {
            // Given: The unlink call fails with 404
            const { listProviders, unlinkProvider } = await import(
                '$lib/api/user/UserProviderController.ts'
            );
            const apiError = { errorMessage: 'Provider not found', status: 404 };
            vi.mocked(unlinkProvider).mockRejectedValueOnce(apiError);
            vi.mocked(listProviders).mockResolvedValueOnce([]);

            const { createConnectedAccountsService } = await import(
                '$lib/api/user/service/ConnectedAccountsService.svelte.ts'
            );
            const service = createConnectedAccountsService();
            service.openUnlinkDialog(aConnectedGithubProvider({ id: 7 }));

            // When: confirmUnlink fails with 404
            await service.confirmUnlink();

            // Then: The provider list is still reloaded
            expect(listProviders).toHaveBeenCalled();
        });
    });
});
