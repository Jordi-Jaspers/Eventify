import type { ApiKeyResponse, ApiKeyCreationResponse } from '$lib/api/models';
import {
	createOrganizationApiKey,
	revokeOrganizationApiKey
} from '../OrganizationApiKeyController';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

/**
 * Service for managing organization API key operations.
 * Encapsulates create, revoke logic and sheet/dialog state management.
 */
export function createApiKeyManagementService(orgId: number, onMutationSuccess: () => void) {
	// Create sheet state
	let showCreateSheet: boolean = $state(false);
	let isCreating: boolean = $state(false);
	let createdKey: ApiKeyCreationResponse | null = $state(null);

	// Revoke dialog state
	let keyToRevoke: ApiKeyResponse | null = $state(null);
	let showRevokeDialog: boolean = $state(false);
	let isRevoking: boolean = $state(false);

	// Create handler
	async function handleCreate(name: string, expiresAt: string | undefined): Promise<void> {
		isCreating = true;
		try {
			const response: ApiKeyCreationResponse = await createOrganizationApiKey(orgId, { name, expiresAt });
			toast.success(`API key "${name}" created successfully`);
			createdKey = response;
			showCreateSheet = false;
			onMutationSuccess();
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
			await revokeOrganizationApiKey(orgId, keyToRevoke.id);
			toast.success(`API key "${keyToRevoke.name ?? 'Unnamed'}" revoked successfully`);
			keyToRevoke = null;
			showRevokeDialog = false;
			onMutationSuccess();
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

	return {
		// State getters
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
		handleCreate,
		handleRevoke,
		openCreateSheet,
		openRevokeDialog,
		closeCreatedModal,
		setShowCreateSheet,
		setShowRevokeDialog
	};
}

export type ApiKeyManagementService = ReturnType<typeof createApiKeyManagementService>;
