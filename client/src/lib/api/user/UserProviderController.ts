import { client } from '$lib/api/client.ts';
import type { ProviderResponse } from '$lib/api/models.ts';

/**
 * List all auth providers for the current user
 */
export async function listProviders(): Promise<ProviderResponse[]> {
    const { data, error } = await client.GET('/v1/user/providers');

    if (error) {
        throw error;
    }

    return data;
}

/**
 * Unlink a provider by its linked record ID
 */
export async function unlinkProvider(id: number): Promise<void> {
    const { error } = await client.DELETE('/v1/user/providers/{id}', {
        params: { path: { id } }
    });

    if (error) {
        throw error;
    }
}
