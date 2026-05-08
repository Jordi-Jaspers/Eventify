import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { components } from '$lib/types/api.d.ts';
import {
    aConnectedGoogleProvider,
    aConnectedGithubProvider,
    aProviderList
} from './fixtures/provider.fixtures';

type ProviderResponse = components['schemas']['ProviderResponse'];

// Mock the API client
vi.mock('$lib/api/client.ts', () => ({
    client: {
        GET: vi.fn(),
        DELETE: vi.fn()
    }
}));

describe('UserProviderController', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('listProviders', () => {
        it('returns provider list on success', async () => {
            // Given: The API returns a list of providers
            const { client } = await import('$lib/api/client.ts');
            const providers: ProviderResponse[] = aProviderList();
            vi.mocked(client.GET).mockResolvedValueOnce({ data: providers, error: undefined } as any);

            // When: listProviders is called
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            const result: ProviderResponse[] = await listProviders();

            // Then: The provider list is returned
            expect(result).toHaveLength(2);
            expect(result[0].provider).toBe('GOOGLE');
            expect(result[1].provider).toBe('GITHUB');
        });

        it('calls GET /v1/user/providers', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.GET).mockResolvedValueOnce({ data: [], error: undefined } as any);

            // When: listProviders is called
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');
            await listProviders();

            // Then: The correct endpoint is called
            expect(client.GET).toHaveBeenCalledWith('/v1/user/providers');
        });

        it('throws when API returns an error', async () => {
            // Given: The API returns an error
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Unauthorized', status: 401 };
            vi.mocked(client.GET).mockResolvedValueOnce({ data: undefined, error: apiError } as any);

            // When: listProviders is called
            const { listProviders } = await import('$lib/api/user/UserProviderController.ts');

            // Then: The error is thrown
            await expect(listProviders()).rejects.toEqual(apiError);
        });
    });

    describe('unlinkProvider', () => {
        it('calls DELETE /v1/user/providers/{id} with correct path param', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.DELETE).mockResolvedValueOnce({ data: undefined, error: undefined });

            // When: unlinkProvider is called with id 42
            const { unlinkProvider } = await import('$lib/api/user/UserProviderController.ts');
            await unlinkProvider(42);

            // Then: The correct endpoint is called with the path param
            expect(client.DELETE).toHaveBeenCalledWith('/v1/user/providers/{id}', {
                params: { path: { id: 42 } }
            });
        });

        it('resolves without returning a value on success', async () => {
            // Given: The API returns successfully
            const { client } = await import('$lib/api/client.ts');
            vi.mocked(client.DELETE).mockResolvedValueOnce({ data: undefined, error: undefined });

            // When: unlinkProvider is called
            const { unlinkProvider } = await import('$lib/api/user/UserProviderController.ts');
            const result: void = await unlinkProvider(1);

            // Then: Nothing is returned
            expect(result).toBeUndefined();
        });

        it('throws when API returns an error', async () => {
            // Given: The API returns a 409 conflict error
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Cannot unlink last provider', status: 409 };
            vi.mocked(client.DELETE).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: unlinkProvider is called
            const { unlinkProvider } = await import('$lib/api/user/UserProviderController.ts');

            // Then: The error is thrown
            await expect(unlinkProvider(1)).rejects.toEqual(apiError);
        });

        it('throws when provider not found (404)', async () => {
            // Given: The API returns a 404 not found error
            const { client } = await import('$lib/api/client.ts');
            const apiError = { errorMessage: 'Provider not found', status: 404 };
            vi.mocked(client.DELETE).mockResolvedValueOnce({ data: undefined, error: apiError });

            // When: unlinkProvider is called with a non-existent id
            const { unlinkProvider } = await import('$lib/api/user/UserProviderController.ts');

            // Then: The error is thrown
            await expect(unlinkProvider(999)).rejects.toEqual(apiError);
        });
    });
});
