import type { ApiKeyResponse, ApiKeyCreationResponse } from '$lib/api/models';
import {
	listApiKeys,
	createApiKey,
	revokeApiKey
} from '../UserApiKeyController';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

/**
 * Service for managing user API key operations.
 * Encapsulates create, revoke logic and sheet/dialog state management.
 */
export function createUserApiKeyManagementService() {
	// Keys state
	let keys: ApiKeyResponse[] = $state([]);
	let loading: boolean = $state(false);
	let limit: number = $state(5);

	// Create sheet state
	let showCreateSheet: boolean = $state(false);
	let isCreating: boolean = $state(false);
	let createdKey: ApiKeyCreationResponse | null = $state(null);

	// Revoke dialog state
	let keyToRevoke: ApiKeyResponse | null = $state(null);
	let showRevokeDialog: boolean = $state(false);
	let isRevoking: boolean = $state(false);

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

	// Create handler
	async function handleCreate(name: string, expiresAt: string | undefined): Promise<void> {
		isCreating = true;
		try {
			const response: ApiKeyCreationResponse = await createApiKey({ name, expiresAt });
			toast.success(`API key "${name}" created successfully`);
			createdKey = response;
			showCreateSheet = false;
			await loadKeys();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to create API key');
			toast.error(message);
		} finally {
			isCreating = false;
		}
	}

	// Revoke handler
	async function handleRevoke(): Promise<void> {
		if (!keyToRevoke || !keyToRevoke.id) return;
		isRevoking = true;
		try {
			await revokeApiKey(keyToRevoke.id);
			toast.success(`API key "${keyToRevoke.name ?? 'Unnamed'}" revoked successfully`);
			keyToRevoke = null;
			showRevokeDialog = false;
			await loadKeys();
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to revoke API key');
			toast.error(message);
		} finally {
			isRevoking = false;
		}
	}

	// Dialog/Sheet control
	function openCreateSheet(): void {
		showCreateSheet = true;
	}

	function openRevokeDialog(key: ApiKeyResponse): void {
		keyToRevoke = key;
		showRevokeDialog = true;
	}

	function closeCreatedModal(): void {
		createdKey = null;
	}

	function setShowCreateSheet(open: boolean): void {
		showCreateSheet = open;
	}

	function setShowRevokeDialog(open: boolean): void {
		showRevokeDialog = open;
	}

	/**
	 * Check if at key limit
	 */
	const atLimit: boolean = $derived(keys.length >= limit);

	return {
		// State getters
		get keys(): ApiKeyResponse[] {
			return keys;
		},
		get loading(): boolean {
			return loading;
		},
		get limit(): number {
			return limit;
		},
		get atLimit(): boolean {
			return atLimit;
		},
		get showCreateSheet(): boolean {
			return showCreateSheet;
		},
		get isCreating(): boolean {
			return isCreating;
		},
		get createdKey(): ApiKeyCreationResponse | null {
			return createdKey;
		},
		get keyToRevoke(): ApiKeyResponse | null {
			return keyToRevoke;
		},
		get showRevokeDialog(): boolean {
			return showRevokeDialog;
		},
		get isRevoking(): boolean {
			return isRevoking;
		},

		// Actions
		loadKeys,
		handleCreate,
		handleRevoke,
		openCreateSheet,
		openRevokeDialog,
		closeCreatedModal,
		setShowCreateSheet,
		setShowRevokeDialog
	};
}

export type UserApiKeyManagementService = ReturnType<typeof createUserApiKeyManagementService>;
