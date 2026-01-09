import { client } from '$lib/api/client.ts';
import type {
	CreateApiKeyRequest,
	ApiKeyCreationResponse,
	PageResourceApiKeyResponse,
	SortablePageInput
} from '$lib/api/models.ts';

/**
 * Search organization API keys with pagination, filtering, and sorting.
 * Any organization member can view.
 */
export async function searchOrganizationApiKeys(
	orgId: number,
	input: SortablePageInput
): Promise<PageResourceApiKeyResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/api-keys/search', {
		params: {
			path: { orgId }
		},
		body: input
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Create a new organization API key.
 * Full key is only shown once. Requires OWNER or ADMIN role.
 */
export async function createOrganizationApiKey(
	orgId: number,
	request: CreateApiKeyRequest
): Promise<ApiKeyCreationResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/api-keys', {
		params: {
			path: { orgId }
		},
		body: request
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Revoke an organization API key.
 * Requires OWNER or ADMIN role.
 */
export async function revokeOrganizationApiKey(orgId: number, keyId: number): Promise<void> {
	const { error } = await client.DELETE('/v1/organization/{orgId}/api-keys/{keyId}', {
		params: {
			path: { orgId, keyId }
		}
	});

	if (error) {
		throw error;
	}
}
