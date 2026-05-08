import { listApiKeys, createApiKey, revokeApiKey } from '../UserApiKeyController';
import type { ApiKeyResponse, CreateApiKeyRequest, ApiKeyCreationResponse } from '$lib/api/models';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

/**
 * Service for managing API keys state and operations
 */
export function createApiKeyService() {
	let keys: ApiKeyResponse[] = $state([]);
	let loading: boolean = $state(false);
	let limit: number = $state(5);
	let createdKey: ApiKeyCreationResponse | null = $state(null);

	/**
	 * Load all API keys
	 */
	async function loadKeys(): Promise<void> {
		loading = true;
		try {
			const response = await listApiKeys();
			keys = response.keys || [];
			limit = response.limit || 5;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load API keys');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	/**
	 * Create a new API key
	 */
	async function createKey(request: CreateApiKeyRequest): Promise<boolean> {
		try {
			const response: ApiKeyCreationResponse = await createApiKey(request);
			createdKey = response;
			await loadKeys();
			return true;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to create API key');
			toast.error(message);
			return false;
		}
	}

	/**
	 * Revoke an API key
	 */
	async function revokeKey(keyId: number): Promise<boolean> {
		try {
			await revokeApiKey(keyId);
			await loadKeys();
			toast.success('API key revoked successfully');
			return true;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to revoke API key');
			toast.error(message);
			return false;
		}
	}

	/**
	 * Clear the created key (after user has seen it)
	 */
	function clearCreatedKey(): void {
		createdKey = null;
	}

	/**
	 * Check if at key limit
	 */
	const atLimit: boolean = $derived(keys.length >= limit);

	return {
		// State
		get keys() { return keys; },
		get loading() { return loading; },
		get limit() { return limit; },
		get createdKey() { return createdKey; },
		get atLimit() { return atLimit; },

		// Actions
		loadKeys,
		createKey,
		revokeKey,
		clearCreatedKey
	};
}

export type ApiKeyService = ReturnType<typeof createApiKeyService>;
