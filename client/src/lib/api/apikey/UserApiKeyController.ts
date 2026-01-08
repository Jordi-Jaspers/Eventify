import { client } from '$lib/api/client';
import type { CreateApiKeyRequest, ApiKeyCreationResponse, ApiKeyListResponse } from '$lib/api/models';

/**
 * List all API keys for the authenticated user
 */
export async function listApiKeys(): Promise<ApiKeyListResponse> {
	const { data, error } = await client.GET('/v1/user/api-keys');

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Create a new API key
 */
export async function createApiKey(request: CreateApiKeyRequest): Promise<ApiKeyCreationResponse> {
	const { data, error } = await client.POST('/v1/user/api-keys', {
		body: request
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Revoke an API key
 */
export async function revokeApiKey(keyId: number): Promise<void> {
	const { error } = await client.DELETE('/v1/user/api-keys/{keyId}', {
		params: {
			path: { keyId }
		}
	});

	if (error) {
		throw error;
	}
}
