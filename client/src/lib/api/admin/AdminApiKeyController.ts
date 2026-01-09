import { client } from '$lib/api/client';
import type {
	AdminApiKeyStatsResponse,
	PageResourceApiKeyResponse,
	PageResourceAdminApiKeyAuditResponse,
	SortablePageInput
} from '$lib/api/models';

/**
 * Get admin API key statistics
 */
export async function getAdminApiKeyStats(): Promise<AdminApiKeyStatsResponse> {
	const { data, error } = await client.GET('/v1/admin/api-keys/stats');

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Search all API keys (Admin)
 */
export async function searchAdminApiKeys(
	input: SortablePageInput
): Promise<PageResourceApiKeyResponse> {
	const { data, error } = await client.POST('/v1/admin/api-keys/search', {
		body: input
	});

	if (error) {
		throw error;
	}

	if (!data) {
		throw new Error('No data returned from search admin API keys');
	}

	return data as PageResourceApiKeyResponse;
}

/**
 * Revoke an API key (Admin)
 */
export async function revokeAdminApiKey(keyId: number): Promise<void> {
	const { error } = await client.DELETE('/v1/admin/api-keys/{keyId}', {
		params: { path: { keyId } }
	});

	if (error) {
		throw error;
	}
}

/**
 * Search API key audit log (Admin)
 */
export async function searchAdminApiKeyAudit(
	input: SortablePageInput
): Promise<PageResourceAdminApiKeyAuditResponse> {
	const { data, error } = await client.POST('/v1/admin/api-keys/audit/search', {
		body: input
	});

	if (error) {
		throw error;
	}

	if (!data) {
		throw new Error('No data returned from search admin API key audit');
	}

	return data as PageResourceAdminApiKeyAuditResponse;
}
