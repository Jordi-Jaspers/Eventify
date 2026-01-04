import {client} from "$lib/api/client.ts";
import type {DevCredentialsResponse} from "$lib/api/models.ts";

/**
 * Get development credentials (only available in dev profile)
 */
export async function getDevCredentials(): Promise<DevCredentialsResponse> {
    const {data, error} = await client.GET('/v1/public/dev/credentials');

    if (error) {
        throw error;
    }

    // Validate required fields exist
    if (!data.email || !data.password) {
        throw new Error('Invalid dev credentials response');
    }

    return {
        email: data.email,
        password: data.password
    };
}
